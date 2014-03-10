/*
 * SonarQube ReSharper Library
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReSharperConfigurationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() {
    ReSharperConfiguration reSharperConf = new ReSharperConfiguration("cs", "cs-resharper", "fooProjectNamePropertyKey", "fooSolutionFilePropertyKey", "fooInspectCodePropertyKey");
    assertThat(reSharperConf.languageKey()).isEqualTo("cs");
    assertThat(reSharperConf.repositoryKey()).isEqualTo("cs-resharper");
    assertThat(reSharperConf.projectNamePropertyKey()).isEqualTo("fooProjectNamePropertyKey");
    assertThat(reSharperConf.solutionFilePropertyKey()).isEqualTo("fooSolutionFilePropertyKey");
    assertThat(reSharperConf.inspectCodePropertyKey()).isEqualTo("fooInspectCodePropertyKey");

    reSharperConf = new ReSharperConfiguration("vbnet", "vbnet-resharper", "barProjectNamePropertyKey", "barSolutionFilePropertyKey", "barInspectCodePropertyKey");
    assertThat(reSharperConf.languageKey()).isEqualTo("vbnet");
    assertThat(reSharperConf.repositoryKey()).isEqualTo("vbnet-resharper");
    assertThat(reSharperConf.projectNamePropertyKey()).isEqualTo("barProjectNamePropertyKey");
    assertThat(reSharperConf.solutionFilePropertyKey()).isEqualTo("barSolutionFilePropertyKey");
    assertThat(reSharperConf.inspectCodePropertyKey()).isEqualTo("barInspectCodePropertyKey");
  }

  @Test
  public void check_properties() {
    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooProjectNamePropertyKey")).thenReturn(true);
    when(settings.hasKey("fooSolutionFilePropertyKey")).thenReturn(true);
    when(settings.hasKey("fooInspectCodePropertyKey")).thenReturn(true);

    new ReSharperConfiguration("", "", "fooProjectNamePropertyKey", "fooSolutionFilePropertyKey", "fooInspectCodePropertyKey").checkProperties(settings);
  }

  @Test
  public void check_properties_project_name_property() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("The property \"fooProjectNamePropertyKey\" must be set.");

    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooProjectNamePropertyKey")).thenReturn(false);

    new ReSharperConfiguration("", "", "fooProjectNamePropertyKey", "fooSolutionFilePropertyKey", "fooInspectCodePropertyKey").checkProperties(settings);
  }

  @Test
  public void check_properties_solution_file_property() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("The property \"fooSolutionFilePropertyKey\" must be set.");

    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooProjectNamePropertyKey")).thenReturn(true);
    when(settings.hasKey("fooSolutionFilePropertyKey")).thenReturn(false);

    new ReSharperConfiguration("", "", "fooProjectNamePropertyKey", "fooSolutionFilePropertyKey", "fooInspectCodePropertyKey").checkProperties(settings);
  }

  @Test
  public void check_properties_inspectcode_property() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("The property \"fooInspectCodePropertyKey\" must be set.");

    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooProjectNamePropertyKey")).thenReturn(true);
    when(settings.hasKey("fooSolutionFilePropertyKey")).thenReturn(true);
    when(settings.hasKey("fooInspectCodePropertyKey")).thenReturn(false);

    new ReSharperConfiguration("", "", "fooProjectNamePropertyKey", "fooSolutionFilePropertyKey", "fooInspectCodePropertyKey").checkProperties(settings);
  }

}
