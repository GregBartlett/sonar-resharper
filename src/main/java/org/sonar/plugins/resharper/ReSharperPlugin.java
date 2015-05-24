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

import com.google.common.collect.ImmutableList;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

public class ReSharperPlugin extends SonarPlugin {

  public static final String PROJECT_NAME_PROPERTY_KEY = "sonar.resharper.projectName";
  public static final String SOLUTION_FILE_PROPERTY_KEY = "sonar.resharper.solutionFile";
  public static final String INSPECTCODE_PATH_PROPERTY_KEY = "sonar.resharper.inspectCodePath";
  public static final String TIMEOUT_MINUTES_PROPERTY_KEY = "sonar.resharper.timeoutMinutes";
  public static final String USE_BUILT_IN_SETTINGS_PROPERTY_KEY = "sonar.resharper.useBuiltInSettings";

  public static final String OLD_INSTALL_DIRECTORY_KEY = "sonar.resharper.installDirectory";

  private static final String CATEGORY = "ReSharper";

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
      PropertyDefinition.builder(PROJECT_NAME_PROPERTY_KEY)
        .name("Visual Studio project name")
        .description("Example: MyLibrary")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),

      PropertyDefinition.builder(SOLUTION_FILE_PROPERTY_KEY)
        .name("Solution file")
        .description("Example: C:/Projects/MyProject/MySolution.sln")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),

      PropertyDefinition.builder(INSPECTCODE_PATH_PROPERTY_KEY)
        .name("Path to inspectcode.exe")
        .description("Example: C:/jetbrains-commandline-tools/inspectcode.exe")
        .defaultValue("C:/jetbrains-commandline-tools/inspectcode.exe")
        .category(CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .deprecatedKey(OLD_INSTALL_DIRECTORY_KEY)
        .build(),

      PropertyDefinition.builder(TIMEOUT_MINUTES_PROPERTY_KEY)
        .name("ReSharper execution timeout")
        .description("Time in minutes after which ReSharper's execution should be interrupted if not finished")
        .defaultValue("60")
        .category(CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build(),

      PropertyDefinition.builder(USE_BUILT_IN_SETTINGS_PROPERTY_KEY)
        .name("Use custom settings layers from solution")
        .description("")
        .defaultValue("false")
        .category(CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.BOOLEAN)
        .build(),

      deprecatedPropertyDefinition(OLD_INSTALL_DIRECTORY_KEY));
  }

  private static PropertyDefinition deprecatedPropertyDefinition(String oldKey) {
    return PropertyDefinition
      .builder(oldKey)
      .name(oldKey)
      .description("This property is deprecated and will be removed in a future version.<br />"
        + "You should stop using it as soon as possible.<br />"
        + "Consult the migration guide for guidance.")
      .category(CATEGORY)
      .subCategory("Deprecated")
      .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
      .build();
  }

}
