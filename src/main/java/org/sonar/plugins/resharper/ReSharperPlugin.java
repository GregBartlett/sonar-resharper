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
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

import java.util.List;

@Properties({
  @Property(
    key = ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY,
    name = "Visual Studio project name",
    description = "Example: MyLibrary",
    module = true),
  @Property(
    key = ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY,
    name = "Solution file",
    description = "Example: C:/Projects/MyProject/MySolution.sln",
    module = true),
  @Property(
    key = ReSharperPlugin.INSPECTCODE_PATH_PROPERTY_KEY,
    name = "Path to inspectcode.exe",
    description = "Example: C:/Program Files/jb-commandline-8.1.23.523/inspectcode.exe",
    module = true)
})
public class ReSharperPlugin extends SonarPlugin {

  public static final String PROJECT_NAME_PROPERTY_KEY = "sonar.resharper.project.name";
  public static final String SOLUTION_FILE_PROPERTY_KEY = "sonar.resharper.solution.file";
  public static final String INSPECTCODE_PATH_PROPERTY_KEY = "sonar.resharper.inspectcode.path";

  /**
   * {@inheritDoc}
   */
  @Override
  public List getExtensions() {
    ImmutableList.Builder builder = ImmutableList.builder();

    builder.addAll(CSharpReSharperProvider.extensions());
    builder.addAll(VBNetReSharperProvider.extensions());

    return builder.build();
  }
}
