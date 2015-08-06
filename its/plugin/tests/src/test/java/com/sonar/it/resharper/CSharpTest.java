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
import com.sonar.orchestrator.build.SonarRunner;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueQuery;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class CSharpTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT_KEY = "Csharp";
  private static final String FILE_KEY = PROJECT_KEY + ":Csharp:Program.cs";

  @BeforeClass
  public static void init() {
    orchestrator.resetData();
  }

  @Test
  public void test_report_path() {
    SonarRunner build = SonarRunner.create()
      .setProjectDir(new File("projects/Csharp/"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0")
      .setSourceDirs(".")
      .setProperty("sonar.modules", "Csharp")
      .setProperty(Tests.SOLUTION_FILE_PROPERTY_KEY, "Csharp.sln")
      .setProperty("sonar.resharper.cs.reportPath", "report/report.xml")
      .setProperty("sonar.sourceEncoding", "UTF-8")
      .setProfile("csharp-it-profile");

    orchestrator.executeBuild(build);

    List<Issue> issues = orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create()).list();

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).ruleKey()).isEqualTo("resharper-cs:UnusedVariable.Compiler");
    assertThat(issues.get(0).componentKey()).isEqualTo(FILE_KEY);
  }

}
