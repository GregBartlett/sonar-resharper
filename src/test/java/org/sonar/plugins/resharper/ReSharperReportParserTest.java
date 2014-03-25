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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ReSharperReportParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void valid() {
    List<ReSharperIssue> issues = new ReSharperReportParser().parse(new File("src/test/resources/ReSharperReportParserTest/valid.xml"));

    assertThat(issues).hasSize(3);

    ReSharperIssue issue = issues.get(0);
    assertThat(issue.reportLine()).isEqualTo(16);
    assertThat(issue.ruleKey()).isEqualTo("RedundantUsingDirective");
    assertThat(issue.filePath()).isEqualTo("MyLibrary\\Class1.cs");
    assertThat(issue.line()).isNull();
    assertThat(issue.message()).isEqualTo("Using directive is not required by the code and can be safely removed");

    issue = issues.get(1);
    assertThat(issue.reportLine()).isEqualTo(17);
    assertThat(issue.ruleKey()).isEqualTo("JoinDeclarationAndInitializer");
    assertThat(issue.filePath()).isEqualTo("MyLibrary\\Class1.cs");
    assertThat(issue.line()).isEqualTo(9);
    assertThat(issue.message()).isEqualTo("Join declaration and assignment");

    issue = issues.get(2);
    assertThat(issue.reportLine()).isEqualTo(18);
    assertThat(issue.ruleKey()).isEqualTo("RedundantUsingDirective");
    assertThat(issue.filePath()).isEqualTo("MyLibrary\\Properties\\AssemblyInfo.cs");
    assertThat(issue.line()).isEqualTo(2);
    assertThat(issue.message()).isEqualTo("Using directive is not required by the code and can be safely removed");
  }

  @Test
  public void invalid_line() {
    thrown.expectMessage("Expected an integer instead of \"foo\" for the attribute \"Line\"");
    thrown.expectMessage("invalid_line.xml at line 14");

    new ReSharperReportParser().parse(new File("src/test/resources/ReSharperReportParserTest/invalid_line.xml"));
  }

  @Test
  public void missing_typeid() {
    thrown.expectMessage("Missing attribute \"TypeId\" in element <Issue>");
    thrown.expectMessage("missing_typeid.xml at line 14");

    new ReSharperReportParser().parse(new File("src/test/resources/ReSharperReportParserTest/missing_typeid.xml"));
  }

  @Test
  public void non_existing() {
    thrown.expectMessage("java.io.FileNotFoundException");
    thrown.expectMessage("non_existing.xml");

    new ReSharperReportParser().parse(new File("src/test/resources/ReSharperReportParserTest/non_existing.xml"));
  }

}
