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

import org.sonar.api.config.Settings;

public class ReSharperConfiguration {

  private final String languageKey;
  private final String repositoryKey;
  private final String projectNamePropertyKey;
  private final String solutionFilePropertyKey;
  private final String inspectCodePropertyKey;

  public ReSharperConfiguration(String languageKey, String repositoryKey, String projectNamePropertyKey, String solutionFilePropertyKey, String inspectCodePropertyKey) {
    this.languageKey = languageKey;
    this.repositoryKey = repositoryKey;
    this.projectNamePropertyKey = projectNamePropertyKey;
    this.solutionFilePropertyKey = solutionFilePropertyKey;
    this.inspectCodePropertyKey = inspectCodePropertyKey;
  }

  public String languageKey() {
    return languageKey;
  }

  public String repositoryKey() {
    return repositoryKey;
  }

  public String projectNamePropertyKey() {
    return projectNamePropertyKey;
  }

  public String solutionFilePropertyKey() {
    return solutionFilePropertyKey;
  }

  public String inspectCodePropertyKey() {
    return inspectCodePropertyKey;
  }

  public void checkProperties(Settings settings) {
    checkProperty(settings, projectNamePropertyKey);
    checkProperty(settings, solutionFilePropertyKey);
    checkProperty(settings, inspectCodePropertyKey);
  }

  private static void checkProperty(Settings settings, String property) {
    if (!settings.hasKey(property)) {
      throw new IllegalStateException("The property \"" + property + "\" must be set.");
    }
  }

}
