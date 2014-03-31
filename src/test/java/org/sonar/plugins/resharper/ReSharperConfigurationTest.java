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

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ReSharperConfigurationTest {

  @Test
  public void test() {
    ReSharperConfiguration reSharperConf = new ReSharperConfiguration("cs", "cs-resharper");
    assertThat(reSharperConf.languageKey()).isEqualTo("cs");
    assertThat(reSharperConf.repositoryKey()).isEqualTo("cs-resharper");

    reSharperConf = new ReSharperConfiguration("vbnet", "vbnet-resharper");
    assertThat(reSharperConf.languageKey()).isEqualTo("vbnet");
    assertThat(reSharperConf.repositoryKey()).isEqualTo("vbnet-resharper");
  }

}
