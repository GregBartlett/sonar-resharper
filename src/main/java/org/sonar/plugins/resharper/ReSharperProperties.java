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

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class ReSharperProperties {

  private static String SUBCATEGORY = "ReSharper";

  public static PropertyDefinition buildProjectName(String key, String category) {
    return PropertyDefinition.builder(key)
      .name("Visual Studio project name")
      .description("Example: MyLibrary")
      .category(category)
      .subCategory(SUBCATEGORY)
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .build();
  }

  public static PropertyDefinition buildSolutionFile(String key, String category) {
    return PropertyDefinition.builder(key)
      .name("Solution file")
      .description("Example: C:/Projects/MyProject/MySolution.sln")
      .category(category)
      .subCategory(SUBCATEGORY)
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .build();
  }

  public static PropertyDefinition buildInspectCode(String key, String category) {
    return PropertyDefinition.builder(key)
      .name("Path to inspectcode.exe")
      .description("Example: C:/Program Files/jb-commandline-8.1.23.523/inspectcode.exe")
      .defaultValue("C:/Program Files/jb-commandline-8.1.23.523/inspectcode.exe")
      .category(category)
      .subCategory(SUBCATEGORY)
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .build();
  }
}
