/*
 * SonarQube ReSharper Plugin
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.resharper;

import com.google.common.base.Strings;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.resharper.VBNetReSharperProvider.VBNetReSharperSensor;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class VBNetReSharperProviderTest {

  @Test
  public void private_constructor() throws Exception {
    Constructor constructor = VBNetReSharperProvider.class.getDeclaredConstructor();
    assertThat(constructor.isAccessible()).isFalse();
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void test() {
    assertThat(VBNetReSharperProvider.extensions()).containsOnly(
      VBNetReSharperProvider.VBNetReSharperRulesDefinition.class,
      VBNetReSharperSensor.class,
      VBNetReSharperProvider.VBNetReSharperProfileExporter.class,
      VBNetReSharperProvider.VBNetReSharperProfileImporter.class);
  }

  @Test
  public void testRulesDefinition() {
    VBNetReSharperProvider.VBNetReSharperRulesDefinition rulesDefinition = new VBNetReSharperProvider.VBNetReSharperRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository repo = context.repositories().get(0);

    assertThat(repo.language()).isEqualTo("vbnet");
    assertThat(repo.key()).isEqualTo("resharper-vbnet");

    List<RulesDefinition.Rule> rules = repo.rules();
    assertThat(rules.size()).isEqualTo(675);
    boolean atLeastOneSqale = false;
    for (RulesDefinition.Rule rule : rules) {
      assertThat(rule.key()).isNotNull();
      assertThat(rule.name()).isNotNull();
      assertThat(rule.htmlDescription()).isNotNull();
      if (!Strings.isNullOrEmpty(rule.debtSubCharacteristic())) {
        atLeastOneSqale = true;
      }
    }
    assertTrue(atLeastOneSqale);
  }

  @Test
  public void testSensorInstantiation() throws Exception {
    VBNetReSharperSensor sensor = new VBNetReSharperSensor(new Settings(), mock(RulesProfile.class), new DefaultFileSystem(), mock(ResourcePerspectives.class));
    ReSharperConfiguration configuration = sensor.getConfiguration();
    assertThat(configuration.languageKey()).isEqualTo("vbnet");
    assertThat(configuration.repositoryKey()).isEqualTo("resharper-vbnet");
    assertThat(configuration.reportPathKey()).isEqualTo("sonar.resharper.vbnet.reportPath");
  }

  @Test
  public void testProfileExporter() throws Exception {
    RulesProfile profile = RulesProfile.create();
    profile.activateRule(Rule.create(VBNetReSharperProvider.RESHARPER_CONF.repositoryKey(), "key1", "key1 name"), null);
    profile.activateRule(Rule.create(VBNetReSharperProvider.RESHARPER_CONF.repositoryKey(), "key2", "key2 name"), null);
    profile.activateRule(Rule.create(CSharpReSharperProvider.RESHARPER_CONF.repositoryKey(), "key3", "key3 name"), null);

    VBNetReSharperProvider.VBNetReSharperProfileExporter profileExporter = new VBNetReSharperProvider.VBNetReSharperProfileExporter();
    StringWriter writer = new StringWriter();
    profileExporter.exportProfile(profile, writer);
    assertThat(writer.toString().replace("\r", "").replace("\n", "")).isEqualTo("<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">" +
      "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key1/@EntryIndexedValue\">WARNING</s:String>" +
      "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key2/@EntryIndexedValue\">WARNING</s:String>" +
      "</wpf:ResourceDictionary>");
  }

  @Test
  public void test_profile_importer() throws Exception {
    String content = "<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">" +
      "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key1/@EntryIndexedValue\">WARNING</s:String>" +
      "</wpf:ResourceDictionary>";
    ReSharperProfileImporter importer = new VBNetReSharperProvider.VBNetReSharperProfileImporter();
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = importer.importProfile(new StringReader(content), messages);
    List<ActiveRule> rules = profile.getActiveRules();
    assertThat(rules).hasSize(1);
    assertThat(messages.getErrors()).isEmpty();
  }
}
