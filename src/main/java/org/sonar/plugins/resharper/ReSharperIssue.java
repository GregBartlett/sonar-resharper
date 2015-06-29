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

import javax.annotation.Nullable;

public class ReSharperIssue {

  private final int reportLine;
  private final String ruleKey;
  private final String filePath;
  private final Integer line;
  private final String message;

  public ReSharperIssue(int reportLine, String ruleKey, @Nullable String filePath, @Nullable Integer line, String message) {
    this.reportLine = reportLine;
    this.ruleKey = ruleKey;
    this.filePath = filePath;
    this.line = line;
    this.message = message;
  }

  public int reportLine() {
    return reportLine;
  }

  public String ruleKey() {
    return ruleKey;
  }

  public String filePath() {
    return filePath;
  }

  @Nullable
  public Integer line() {
    return line;
  }

  public String message() {
    return message;
  }

}
