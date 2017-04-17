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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;

import java.io.File;
import java.util.List;

public class ReSharperSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(ReSharperSensor.class);

  private final ReSharperConfiguration reSharperConf;
  private final Settings settings;
  private final RulesProfile profile;
  private final FileSystem fileSystem;
  private final ResourcePerspectives perspectives;

  public ReSharperSensor(ReSharperConfiguration reSharperConf, Settings settings, RulesProfile profile, FileSystem fileSystem, ResourcePerspectives perspectives) {
    this.reSharperConf = reSharperConf;
    this.settings = settings;
    this.profile = profile;
    this.fileSystem = fileSystem;
    this.perspectives = perspectives;
  }

  @VisibleForTesting
  protected ReSharperConfiguration getConfiguration() {
    return reSharperConf;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    boolean shouldExecute;

    if (!hasFilesToAnalyze()) {
      shouldExecute = false;
    } else if (profile.getActiveRulesByRepository(getConfiguration().repositoryKey()).isEmpty()) {
      LOG.info("All ReSharper rules are disabled, skipping its execution.");
      shouldExecute = false;
    } else {
      shouldExecute = true;
    }

    return shouldExecute;
  }

  private boolean hasFilesToAnalyze() {
    return fileSystem.files(fileSystem.predicates().hasLanguage(reSharperConf.languageKey())).iterator().hasNext();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    FileProvider fileProvider = new FileProvider();
    ReSharperReportParser parser = new ReSharperReportParser();
    if (!settings.hasKey(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY) || settings.hasKey(ReSharperPlugin.CS_REPORT_PATH_KEY)) {
      logMessageIfLegacySettingsDefined();
      analyseReportPath(fileProvider, parser);
    } else {
      analyseRunInspectCode(fileProvider, new ReSharperDotSettingsWriter(), parser, new ReSharperExecutor());
    }
  }

  private void logMessageIfLegacySettingsDefined() {
    if (settings.hasKey(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY) ||
      settings.hasKey(ReSharperPlugin.INSPECTCODE_PATH_PROPERTY_KEY)) {
      LOG.warn("ReSharper plugin is running in reportPath mode, other properties other than reportPath and solutionFile can be undefined");
    }
  }

  private void analyseReportPath(FileProvider fileProvider, ReSharperReportParser parser) {
    checkProperty(settings, reSharperConf.reportPathKey());
    checkProperty(settings, ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY);
    File reportFile = new File(settings.getString(reSharperConf.reportPathKey()));
    parseReport(fileProvider, parser, reportFile);
  }

  @VisibleForTesting
  void analyseRunInspectCode(FileProvider fileProvider, ReSharperDotSettingsWriter writer, ReSharperReportParser parser, ReSharperExecutor executor) {
    LOG.warn("ReSharper plugin is running in deprecated mode. inspectcode.exe should be ran outside the " +
      "plugin and the report imported through " + reSharperConf.reportPathKey() + " property.");
    checkProperty(settings, ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY);
    checkProperty(settings, ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY);

    File rulesetFile = new File(fileSystem.workDir(), "resharper-sonarqube.DotSettings");
    writer.write(enabledRuleKeys(), rulesetFile);

    File reportFile = new File(fileSystem.workDir(), "resharper-report.xml");

    executor.execute(
      settings.getString(ReSharperPlugin.INSPECTCODE_PATH_PROPERTY_KEY), settings.getString(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY),
      settings.getString(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY), rulesetFile, reportFile, settings.getInt(ReSharperPlugin.TIMEOUT_MINUTES_PROPERTY_KEY));

    parseReport(fileProvider, parser, reportFile);
  }

  private void parseReport(FileProvider fileProvider, ReSharperReportParser parser, File reportFile) {
    LOG.info("Parsing ReSharper report: " + reportFile);
    File solutionFile = new File(settings.getString(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY));
    String projectName = settings.getString(ReSharperPlugin.PROJECT_NAME_SONAR_PROPERTY_KEY);
    LOG.info(ReSharperPlugin.PROJECT_NAME_SONAR_PROPERTY_KEY + " " + projectName);
    List<ReSharperIssue> parse = parser.parse(reportFile, projectName);
    for (ReSharperIssue issue : parse) {
      if (!hasFileAndLine(issue)) {
        logSkippedIssue(issue, "which has no associated file.");
        continue;
      }

      File file = fileProvider.fileInSolution(solutionFile, issue.filePath());

      //The below code is implemented to try to resolve an issue where something is passing a wonky file path i.e.
      // "D:\\vssrc\\GregsProject\\\"D:\\vssrc\\GregsProject\\Common Libraries\\Common Libraries\\Class1.cs\"";
      //The below condition will attempt to detect this type of string and rip out only the full path to the file
      if(file.getAbsolutePath().contains("\\\"") && file.getAbsolutePath().endsWith("\"")){
        String badFilePath = file.getAbsolutePath();
        int position = badFilePath.indexOf("\\\"");
        String newPath = file.getAbsolutePath().substring(position + 2,file.getAbsolutePath().length()-1);

        //change the file variable to the new filePath.
        file = new File(newPath);
      }

      InputFile inputFile;
      try {
        inputFile = fileSystem.inputFile(
                fileSystem.predicates().and(
                        fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath()),
                        fileSystem.predicates().hasType(InputFile.Type.MAIN))
        );
      }catch (Exception ex){
        logSkippedIssue(issue, "Failed to get input file: \"" + ex.getMessage() +"\"");
        continue;
      }
      if (inputFile == null) {
        logSkippedIssueOutsideOfSonarQube(issue, file);
      } else if (reSharperConf.languageKey().equals(inputFile.language())) {
        Issuable issuable = perspectives.as(Issuable.class, inputFile);
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
    LOG.debug("Skipping the ReSharper issue at line " + issue.reportLine() + " " + reason);
  }

  private List<String> enabledRuleKeys() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (ActiveRule activeRule : profile.getActiveRulesByRepository(reSharperConf.repositoryKey())) {
      builder.add(activeRule.getRuleKey());
    }
    return builder.build();
  }

  private static void checkProperty(Settings settings, String property) {
    if (!settings.hasKey(property) || settings.getString(property).isEmpty()) {
      throw new IllegalStateException("The property \"" + property + "\" must be set.");
    }
  }
}
