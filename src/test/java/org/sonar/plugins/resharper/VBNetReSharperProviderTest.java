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
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.resharper.VBNetReSharperProvider.VBNetReSharperSensor;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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
      VBNetReSharperSensor.class);
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

}
