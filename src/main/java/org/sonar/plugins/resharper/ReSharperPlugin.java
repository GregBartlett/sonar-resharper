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

import com.google.common.collect.ImmutableList;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

public class ReSharperPlugin extends SonarPlugin {

  public static final String PROJECT_NAME_SONAR_PROPERTY_KEY = "sonar.projectName";
  public static final String PROJECT_NAME_PROPERTY_KEY = "sonar.resharper.projectName";
  public static final String SOLUTION_FILE_PROPERTY_KEY = "sonar.resharper.solutionFile";
  public static final String INSPECTCODE_PATH_PROPERTY_KEY = "sonar.resharper.inspectCodePath";
  public static final String TIMEOUT_MINUTES_PROPERTY_KEY = "sonar.resharper.timeoutMinutes";

  public static final String CS_REPORT_PATH_KEY = "sonar.resharper.cs.reportPath";
  public static final String VBNET_REPORT_PATH_KEY = "sonar.resharper.vbnet.reportPath";

  public static final String OLD_INSTALL_DIRECTORY_KEY = "sonar.resharper.installDirectory";

  private static final String CATEGORY = "ReSharper";
  private static final String DEPRECATED_SUBCATEGORY = "Deprecated";
  private static final String DEPRECATED_DESCRIPTION = "This property is deprecated and will be removed in a future version.<br />"
    + "You should stop using it as soon as possible.<br />"
    + "Consult the migration guide for guidance.";

  /**
   * {@inheritDoc}
   */
  @Override
  public List getExtensions() {
    ImmutableList.Builder builder = ImmutableList.builder();

    builder.addAll(CSharpReSharperProvider.extensions());
    builder.addAll(VBNetReSharperProvider.extensions());

    builder.addAll(pluginProperties());

    return builder.build();
  }

  private static ImmutableList<PropertyDefinition> pluginProperties() {
    return ImmutableList.of(
      PropertyDefinition.builder(CS_REPORT_PATH_KEY)
        .name("ReSharper report path for C#")
        .description("Path to the ReSharper report for C#, i.e. reports/cs-report.xml")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),

      PropertyDefinition.builder(VBNET_REPORT_PATH_KEY)
        .name("ReSharper report path for VB.NET")
        .description("Path to the ReSharper report for VB.NET, i.e. reports/vbnet-report.xml")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),

      PropertyDefinition.builder(SOLUTION_FILE_PROPERTY_KEY)
        .name("Solution file")
        .description("The absolute path to the solution or project file given as input to inspectcode.exe. Example: C:/Projects/MyProject/MySolution.sln.")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),

      PropertyDefinition.builder(PROJECT_NAME_PROPERTY_KEY)
        .name(deprecatedName("Visual Studio project name"))
        .description(deprecatedDescription("Example: MyLibrary."))
        .category(CATEGORY)
        .subCategory(DEPRECATED_SUBCATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),

      PropertyDefinition.builder(INSPECTCODE_PATH_PROPERTY_KEY)
        .name(deprecatedName("Path to inspectcode.exe"))
        .description(deprecatedDescription("Example: C:/jetbrains-commandline-tools/inspectcode.exe."))
        .defaultValue("C:/jetbrains-commandline-tools/inspectcode.exe")
        .category(CATEGORY)
        .subCategory(DEPRECATED_SUBCATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .deprecatedKey(OLD_INSTALL_DIRECTORY_KEY)
        .build(),

      PropertyDefinition.builder(TIMEOUT_MINUTES_PROPERTY_KEY)
        .name(deprecatedName("ReSharper execution timeout"))
        .description(deprecatedDescription("Time in minutes after which ReSharper's execution should be interrupted if not finished."))
        .defaultValue("60")
        .category(CATEGORY)
        .subCategory(DEPRECATED_SUBCATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build(),

      deprecatedPropertyDefinition(OLD_INSTALL_DIRECTORY_KEY));
  }

  private static String deprecatedDescription(String description) {
    return description + "<br /><br />" + DEPRECATED_DESCRIPTION;
  }

  private static String deprecatedName(String name) {
    return "Deprecated - " + name;
  }

  private static PropertyDefinition deprecatedPropertyDefinition(String oldKey) {
    return PropertyDefinition
      .builder(oldKey)
      .name(deprecatedName(oldKey))
      .description(DEPRECATED_DESCRIPTION)
      .category(CATEGORY)
      .subCategory(DEPRECATED_SUBCATEGORY)
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .build();
  }

}
