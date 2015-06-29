/*
 * SonarQube ReSharper Plugin
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
package org.sonar.plugins.resharper;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ReSharperIssueTest {

  @Test
  public void test() {
    ReSharperIssue issue = new ReSharperIssue(0, "S007", "foo.cs", 1, "message1");
    assertThat(issue.reportLine()).isEqualTo(0);
    assertThat(issue.ruleKey()).isEqualTo("S007");
    assertThat(issue.filePath()).isEqualTo("foo.cs");
    assertThat(issue.line()).isEqualTo(1);
    assertThat(issue.message()).isEqualTo("message1");

    issue = new ReSharperIssue(42, "AccessToDisposedClosure", "bar.vb", 42, "message2");
    assertThat(issue.reportLine()).isEqualTo(42);
    assertThat(issue.ruleKey()).isEqualTo("AccessToDisposedClosure");
    assertThat(issue.filePath()).isEqualTo("bar.vb");
    assertThat(issue.line()).isEqualTo(42);
    assertThat(issue.message()).isEqualTo("message2");
  }

}
