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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.ValidationMessages;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

public class ReSharperProfileImporterTest {

  @Test
  public void test_invalid_xml() throws Exception {
    String content = "<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">\n" +
      "  <s:String x:Key=\"/Defa";
    ReSharperProfileImporter importer = new ReSharperProfileImporter(new ReSharperConfiguration("key", "key", "key"));
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = importer.importProfile(new StringReader(content), messages);
    List<ActiveRule> rules = profile.getActiveRules();
    assertThat(rules).isEmpty();
    List<String> errors = messages.getErrors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).startsWith("Error parsing content: com.ctc.wstx.exc.WstxEOFException: Unexpected EOF ");
  }

  @Test
  public void test_invalid_root_element() throws Exception {
    String content = "<bad xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">\n" +
      "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key1/@EntryIndexedValue\">WARNING</s:String>\n" +
      "</bad>";
    ReSharperProfileImporter importer = new ReSharperProfileImporter(new ReSharperConfiguration("key", "key", "key"));
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = importer.importProfile(new StringReader(content), messages);
    List<ActiveRule> rules = profile.getActiveRules();
    assertThat(rules).isEmpty();
    assertThat(messages.getErrors()).containsExactly("Expected element: wpf:ResourceDictionary, actual: bad");
  }

  @Test
  public void test_profile_importer() throws Exception {
    String content = "<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">" +
      "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key1/@EntryIndexedValue\">WARNING</s:String>" +
      "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key2/@EntryIndexedValue\">ERROR</s:String>" +
      "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=UnusedVariable_002ECompiler/@EntryIndexedValue\">HINT</s:String>" +
      "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key2/@EntryIndexedValue\">DO_NOT_SHOW</s:String>" +
      "<s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=key2/@EntryIndexedValue\">INVALID_SEVERITY</s:String>" +
      "<s:String x:Key=\"/other/entry\">value</s:String>" +
      "<s:String>value</s:String>" +
      "<s:other>value</s:other>" +
      "</wpf:ResourceDictionary>";
    ReSharperProfileImporter importer = new ReSharperProfileImporter(new ReSharperConfiguration("key", "key", "key"));
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = importer.importProfile(new StringReader(content), messages);
    List<ActiveRule> rules = profile.getActiveRules();
    assertThat(rules).hasSize(3);
    Map<String, String> ruleKeys = getKeysWithSeverities(rules);
    assertThat(ruleKeys.keySet()).containsOnly("key1", "key2", "UnusedVariable.Compiler");
    assertThat(ruleKeys.get("key1")).isEqualTo("MAJOR");
    assertThat(ruleKeys.get("key2")).isEqualTo("CRITICAL");
    assertThat(ruleKeys.get("UnusedVariable.Compiler")).isEqualTo("INFO");
    assertThat(messages.getErrors()).containsExactly("Skipping rule key2 because has an unexpected severity: INVALID_SEVERITY");
  }

  private static Map<String, String> getKeysWithSeverities(List<ActiveRule> rules) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    for (ActiveRule rule : rules) {
      builder.put(rule.getRuleKey(), rule.getSeverity().name());
    }
    return builder.build();
  }
}