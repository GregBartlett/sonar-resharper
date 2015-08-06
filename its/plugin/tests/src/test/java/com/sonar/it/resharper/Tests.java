/*
 * tests
 * Copyright (C) 2014 SonarSource
 * sonarqube@googlegroups.com
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
package com.sonar.it.resharper;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  CSharpTest.class,
  VBNetTest.class
})
public class Tests {

  private static final String PLUGIN_KEY = "resharper";
  private static final String CSHARP_PROFILE = "/com/sonar/it/resharper/csharp-profile.xml";
  private static final String VBNET_PROFILE = "/com/sonar/it/resharper/vbnet-profile.xml";

  public static final String SOLUTION_FILE_PROPERTY_KEY = "sonar.resharper.solutionFile";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR;

  static {
    OrchestratorBuilder orchestratorBuilder = Orchestrator.builderEnv()
      .addPlugin(PLUGIN_KEY)
      .setMainPluginKey(PLUGIN_KEY)
      .addPlugin("csharp")
      .addPlugin(MavenLocation.of("org.sonarsource.its.resharper", "fake-vbnet-plugin", "1.0-SNAPSHOT"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(CSHARP_PROFILE))
      .restoreProfileAtStartup(FileLocation.ofClasspath(VBNET_PROFILE));
    ORCHESTRATOR = orchestratorBuilder.build();
  }

}
