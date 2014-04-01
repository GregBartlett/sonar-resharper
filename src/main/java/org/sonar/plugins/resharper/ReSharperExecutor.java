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

import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandException;
import org.sonar.api.utils.command.CommandExecutor;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ReSharperExecutor {

  private static final int INSPECTCODE_TIMEOUT_MINUTES = 120;

  public void execute(String executable, String project, String solutionFile, File rulesetFile, File reportFile) {
    Command cmd = Command.create(executable)
      .addArgument("/output=" + reportFile.getAbsolutePath())
      .addArgument("/no-swea")
      .addArgument("/project=" + project)
      .addArgument("/profile=" + rulesetFile.getAbsolutePath())
      .addArgument("/no-buildin-settings")
      .addArgument(solutionFile);

    int exitCode = CommandExecutor.create().execute(cmd, TimeUnit.MINUTES.toMillis(INSPECTCODE_TIMEOUT_MINUTES));

    if (exitCode != 0) {
      throw new CommandException(cmd, "ReSharper execution failed with exit code: " + exitCode, null);
    }
  }

}
