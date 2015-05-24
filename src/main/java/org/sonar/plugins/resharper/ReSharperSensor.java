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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.List;

public class ReSharperSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(ReSharperSensor.class);

  private final ReSharperConfiguration reSharperConf;
  private final Settings settings;
  private final RulesProfile profile;
  private final ModuleFileSystem fileSystem;
  private final ResourcePerspectives perspectives;

  public ReSharperSensor(ReSharperConfiguration reSharperConf, Settings settings, RulesProfile profile, ModuleFileSystem fileSystem, ResourcePerspectives perspectives) {
    this.reSharperConf = reSharperConf;
    this.settings = settings;
    this.profile = profile;
    this.fileSystem = fileSystem;
    this.perspectives = perspectives;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    boolean shouldExecute;

    if (!hasFilesToAnalyze()) {
      shouldExecute = false;
    } else if (profile.getActiveRulesByRepository(reSharperConf.repositoryKey()).isEmpty()) {
      LOG.info("All ReSharper rules are disabled, skipping its execution.");
      shouldExecute = false;
    } else {
      shouldExecute = true;
    }

    return shouldExecute;
  }

  private boolean hasFilesToAnalyze() {
    return !fileSystem.files(FileQuery.onSource().onLanguage(reSharperConf.languageKey())).isEmpty();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    analyse(context, new FileProvider(project, context), new ReSharperDotSettingsWriter(), new ReSharperReportParser(), new ReSharperExecutor());
  }

  private String getProjectArgument() {
    if(settings.getString(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY) != null &&
      !settings.getString(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY).equals("")) {
        return "/project=" + settings.getString(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY);
    } else {
      return "";
    }      
  }
  
  @VisibleForTesting
  void analyse(SensorContext context, FileProvider fileProvider, ReSharperDotSettingsWriter writer, ReSharperReportParser parser, ReSharperExecutor executor) {
    checkProperties(settings);

    File rulesetFile = new File(fileSystem.workingDir(), "resharper-sonarqube.DotSettings");
    writer.write(enabledRuleKeys(), rulesetFile);

    File reportFile = new File(fileSystem.workingDir(), "resharper-report.xml");
    
    executor.execute(
      settings.getString(ReSharperPlugin.INSPECTCODE_PATH_PROPERTY_KEY), getProjectArgument(),
      settings.getString(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY), rulesetFile, reportFile, settings.getInt(ReSharperPlugin.TIMEOUT_MINUTES_PROPERTY_KEY));

    File solutionFile = new File(settings.getString(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY));
    for (ReSharperIssue issue : parser.parse(reportFile)) {
      if (!hasFileAndLine(issue)) {
        logSkippedIssue(issue, "which has no associated file.");
        continue;
      }

      File file = fileProvider.fileInSolution(solutionFile, issue.filePath());
      org.sonar.api.resources.File sonarFile = fileProvider.fromIOFile(file);
      if (sonarFile == null) {
        logSkippedIssueOutsideOfSonarQube(issue, file);
      } else if (reSharperConf.languageKey().equals(sonarFile.getLanguage().getKey())) {
        Issuable issuable = perspectives.as(Issuable.class, sonarFile);
        if (issuable == null) {
          logSkippedIssueOutsideOfSonarQube(issue, file);
        } else if (!enabledRuleKeys().contains(issue.ruleKey())) {
          logSkippedIssue(issue, "because the rule \"" + issue.ruleKey() + "\" is either missing or inactive in the quality profile.");
        } else {
          issuable.addIssue(
            issuable.newIssueBuilder()
              .ruleKey(RuleKey.of(reSharperConf.repositoryKey(), issue.ruleKey()))
              .line(issue.line())
              .message(issue.message())
              .build());
        }
      }
    }
  }

  private static boolean hasFileAndLine(ReSharperIssue issue) {
    return issue.filePath() != null && issue.line() != null;
  }

  private static void logSkippedIssueOutsideOfSonarQube(ReSharperIssue issue, File file) {
    logSkippedIssue(issue, "whose file \"" + file.getAbsolutePath() + "\" is not in SonarQube.");
  }

  private static void logSkippedIssue(ReSharperIssue issue, String reason) {
    LOG.info("Skipping the ReSharper issue at line " + issue.reportLine() + " " + reason);
  }

  private List<String> enabledRuleKeys() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (ActiveRule activeRule : profile.getActiveRulesByRepository(reSharperConf.repositoryKey())) {
      builder.add(activeRule.getRuleKey());
    }
    return builder.build();
  }

  public void checkProperties(Settings settings) {
    checkProperty(settings, ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY, false);
    checkProperty(settings, ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY, true);
  }

  private static void checkProperty(Settings settings, String property, boolean isMandatory) {
    if (!settings.hasKey(property) && isMandatory) {
      throw new IllegalStateException("The property \"" + property + "\" must be set.");
    } else {
        LOG.info("The property \"" + property + "\" not set. Not using in analysis");
    }
  }
}
