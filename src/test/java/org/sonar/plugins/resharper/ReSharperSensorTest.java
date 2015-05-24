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
import com.google.common.collect.Maps;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import javax.annotation.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ReSharperSensorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldExecuteOnProject() {
    Settings settings = mock(Settings.class);
    RulesProfile profile = mock(RulesProfile.class);
    ModuleFileSystem fileSystem = mock(ModuleFileSystem.class);
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    Project project = mock(Project.class);

    ReSharperSensor sensor = new ReSharperSensor(
      new ReSharperConfiguration("", "foo-resharper"),
      settings, profile, fileSystem, perspectives);

    when(fileSystem.files(Mockito.any(FileQuery.class))).thenReturn(ImmutableList.<File>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    when(fileSystem.files(Mockito.any(FileQuery.class))).thenReturn(ImmutableList.of(mock(File.class)));
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(ImmutableList.<ActiveRule>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    when(fileSystem.files(Mockito.any(FileQuery.class))).thenReturn(ImmutableList.of(mock(File.class)));
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(ImmutableList.of(mock(ActiveRule.class)));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void analyze() throws Exception {
    Settings settings = mockSettings("MyLibrary", "CSharpPlayground.sln", "inspectcode.exe", null);
    RulesProfile profile = mock(RulesProfile.class);
    ModuleFileSystem fileSystem = mock(ModuleFileSystem.class);
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    ReSharperSensor sensor = new ReSharperSensor(
      new ReSharperConfiguration("foo", "foo-resharper"),
      settings, profile, fileSystem, perspectives);

    List<ActiveRule> activeRules = mockActiveRules("AccessToDisposedClosure", "AccessToForEachVariableInClosure");
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(activeRules);

    SensorContext context = mock(SensorContext.class);
    FileProvider fileProvider = mock(FileProvider.class);
    ReSharperExecutor executor = mock(ReSharperExecutor.class);

    File workingDir = new File("target/ReSharperSensorTest/working-dir");
    when(fileSystem.workingDir()).thenReturn(workingDir);

    File fileNotInSonarQube = mock(File.class);
    File fooFileWithIssuable = mock(File.class);
    File fooFileWithoutIssuable = mock(File.class);
    File barFile = mock(File.class);

    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class3.cs"))).thenReturn(fileNotInSonarQube);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class4.cs"))).thenReturn(fooFileWithIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class5.cs"))).thenReturn(fooFileWithIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class6.cs"))).thenReturn(fooFileWithoutIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class7.cs"))).thenReturn(barFile);

    org.sonar.api.resources.File fooSonarFileWithIssuable = mockSonarFile("foo");
    org.sonar.api.resources.File fooSonarFileWithoutIssuable = mockSonarFile("foo");
    org.sonar.api.resources.File barSonarFile = mockSonarFile("bar");

    when(fileProvider.fromIOFile(fileNotInSonarQube)).thenReturn(null);
    when(fileProvider.fromIOFile(fooFileWithIssuable)).thenReturn(fooSonarFileWithIssuable);
    when(fileProvider.fromIOFile(fooFileWithoutIssuable)).thenReturn(fooSonarFileWithoutIssuable);
    when(fileProvider.fromIOFile(barFile)).thenReturn(barSonarFile);

    Issue issue1 = mock(Issue.class);
    IssueBuilder issueBuilder1 = mockIssueBuilder();
    when(issueBuilder1.build()).thenReturn(issue1);

    Issue issue2 = mock(Issue.class);
    IssueBuilder issueBuilder2 = mockIssueBuilder();
    when(issueBuilder2.build()).thenReturn(issue2);

    Issuable issuable = mock(Issuable.class);
    when(perspectives.as(Issuable.class, fooSonarFileWithIssuable)).thenReturn(issuable);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder1, issueBuilder2);

    ReSharperDotSettingsWriter writer = mock(ReSharperDotSettingsWriter.class);

    ReSharperReportParser parser = mock(ReSharperReportParser.class);
    when(parser.parse(new File(workingDir, "resharper-report.xml"))).thenReturn(
      ImmutableList.of(
        new ReSharperIssue(100, "AccessToDisposedClosure", null, 1, "Dummy message"),
        new ReSharperIssue(200, "AccessToDisposedClosure", "Class2.cs", null, "Dummy message"),
        new ReSharperIssue(400, "AccessToDisposedClosure", "Class3.cs", 3, "First message"),
        new ReSharperIssue(500, "AccessToDisposedClosure", "Class4.cs", 4, "Second message"),
        new ReSharperIssue(600, "AccessToForEachVariableInClosure", "Class5.cs", 5, "Third message"),
        new ReSharperIssue(700, "AccessToDisposedClosure", "Class6.cs", 6, "Fourth message"),
        new ReSharperIssue(800, "AccessToDisposedClosure", "Class7.cs", 7, "Fifth message")));

    sensor.analyse(context, fileProvider, writer, parser, executor);

    verify(writer).write(ImmutableList.of("AccessToDisposedClosure", "AccessToForEachVariableInClosure"), new File(workingDir, "resharper-sonarqube.DotSettings"));
    verify(executor).execute(
      "inspectcode.exe", "MyLibrary", "CSharpPlayground.sln",
      new File(workingDir, "resharper-sonarqube.DotSettings"), new File(workingDir, "resharper-report.xml"), 10);

    verify(issuable).addIssue(issue1);
    verify(issuable).addIssue(issue2);

    verify(issueBuilder1).line(4);
    verify(issueBuilder1).message("Second message");

    verify(issueBuilder2).line(5);
    verify(issueBuilder2).message("Third message");
  }

  @Test
  public void analyzeWithReportFileInSettings() throws Exception {
    ModuleFileSystem fileSystem = mock(ModuleFileSystem.class);

    File workingDir = new File("target/ReSharperSensorTest/working-dir");
    when(fileSystem.workingDir()).thenReturn(workingDir);

    File reportFile = new File(workingDir, "resharper-report.xml");

    Settings settings = mockSettings("MyLibrary", "CSharpPlayground.sln", "inspectcode.exe", reportFile.getAbsolutePath());
    RulesProfile profile = mock(RulesProfile.class);
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    ReSharperSensor sensor = new ReSharperSensor(
            new ReSharperConfiguration("foo", "foo-resharper"),
            settings, profile, fileSystem, perspectives);

    List<ActiveRule> activeRules = mockActiveRules("AccessToDisposedClosure", "AccessToForEachVariableInClosure");
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(activeRules);

    SensorContext context = mock(SensorContext.class);
    FileProvider fileProvider = mock(FileProvider.class);
    ReSharperExecutor executor = mock(ReSharperExecutor.class);

    File fileNotInSonarQube = mock(File.class);
    File fooFileWithIssuable = mock(File.class);
    File fooFileWithoutIssuable = mock(File.class);
    File barFile = mock(File.class);

    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class3.cs"))).thenReturn(fileNotInSonarQube);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class4.cs"))).thenReturn(fooFileWithIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class5.cs"))).thenReturn(fooFileWithIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class6.cs"))).thenReturn(fooFileWithoutIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class7.cs"))).thenReturn(barFile);

    org.sonar.api.resources.File fooSonarFileWithIssuable = mockSonarFile("foo");
    org.sonar.api.resources.File fooSonarFileWithoutIssuable = mockSonarFile("foo");
    org.sonar.api.resources.File barSonarFile = mockSonarFile("bar");

    when(fileProvider.fromIOFile(fileNotInSonarQube)).thenReturn(null);
    when(fileProvider.fromIOFile(fooFileWithIssuable)).thenReturn(fooSonarFileWithIssuable);
    when(fileProvider.fromIOFile(fooFileWithoutIssuable)).thenReturn(fooSonarFileWithoutIssuable);
    when(fileProvider.fromIOFile(barFile)).thenReturn(barSonarFile);

    Issue issue1 = mock(Issue.class);
    IssueBuilder issueBuilder1 = mockIssueBuilder();
    when(issueBuilder1.build()).thenReturn(issue1);

    Issue issue2 = mock(Issue.class);
    IssueBuilder issueBuilder2 = mockIssueBuilder();
    when(issueBuilder2.build()).thenReturn(issue2);

    Issuable issuable = mock(Issuable.class);
    when(perspectives.as(Issuable.class, fooSonarFileWithIssuable)).thenReturn(issuable);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder1, issueBuilder2);

    ReSharperDotSettingsWriter writer = mock(ReSharperDotSettingsWriter.class);

    ReSharperReportParser parser = mock(ReSharperReportParser.class);
    when(parser.parse(reportFile.getAbsoluteFile())).thenReturn(
            ImmutableList.of(
                    new ReSharperIssue(100, "AccessToDisposedClosure", null, 1, "Dummy message"),
                    new ReSharperIssue(200, "AccessToDisposedClosure", "Class2.cs", null, "Dummy message"),
                    new ReSharperIssue(400, "AccessToDisposedClosure", "Class3.cs", 3, "First message"),
                    new ReSharperIssue(500, "AccessToDisposedClosure", "Class4.cs", 4, "Second message"),
                    new ReSharperIssue(600, "AccessToForEachVariableInClosure", "Class5.cs", 5, "Third message"),
                    new ReSharperIssue(700, "AccessToDisposedClosure", "Class6.cs", 6, "Fourth message"),
                    new ReSharperIssue(800, "AccessToDisposedClosure", "Class7.cs", 7, "Fifth message")));

    sensor.analyse(context, fileProvider, writer, parser, executor);

    verifyZeroInteractions(executor);

    verify(issuable).addIssue(issue1);
    verify(issuable).addIssue(issue2);

    verify(issueBuilder1).line(4);
    verify(issueBuilder1).message("Second message");

    verify(issueBuilder2).line(5);
    verify(issueBuilder2).message("Third message");
  }

  @Test
  public void check_project_name_property() {
    thrown.expectMessage(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY);
    thrown.expect(IllegalStateException.class);

    Settings settings = mockSettings(null, "dummy.sln", null, null);
    mockReSharperSensor(settings).analyse(mock(Project.class), mock(SensorContext.class));
  }

  @Test
  public void check_solution_file_property() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY);

    Settings settings = mockSettings("Dummy Project", null, null, null);
    mockReSharperSensor(settings).analyse(mock(Project.class), mock(SensorContext.class));
  }

  private static org.sonar.api.resources.File mockSonarFile(String languageKey) {
    Language language = mock(Language.class);
    when(language.getKey()).thenReturn(languageKey);
    org.sonar.api.resources.File sonarFile = mock(org.sonar.api.resources.File.class);
    when(sonarFile.getLanguage()).thenReturn(language);
    return sonarFile;
  }

  private static IssueBuilder mockIssueBuilder() {
    IssueBuilder issueBuilder = mock(IssueBuilder.class);
    when(issueBuilder.ruleKey(Mockito.any(RuleKey.class))).thenReturn(issueBuilder);
    when(issueBuilder.line(Mockito.anyInt())).thenReturn(issueBuilder);
    when(issueBuilder.message(Mockito.anyString())).thenReturn(issueBuilder);
    return issueBuilder;
  }

  private static List<ActiveRule> mockActiveRules(String... activeRuleKeys) {
    ImmutableList.Builder<ActiveRule> builder = ImmutableList.builder();
    for (String activeRuleKey : activeRuleKeys) {
      ActiveRule activeRule = mock(ActiveRule.class);
      when(activeRule.getRuleKey()).thenReturn(activeRuleKey);
      builder.add(activeRule);
    }
    return builder.build();
  }

  private static ReSharperSensor mockReSharperSensor(Settings settings) {
    ReSharperConfiguration reSharperConf = new ReSharperConfiguration("", "");
    return new ReSharperSensor(reSharperConf, settings, mock(RulesProfile.class), mock(ModuleFileSystem.class), mock(ResourcePerspectives.class));
  }

  private static Settings mockSettings(@Nullable String projectName, @Nullable String solutionFile, @Nullable String inspectcodePath, @Nullable String reportFile) {
    Settings settings = new Settings();
    Map<String, String> props = Maps.newHashMap();

    if (projectName != null) {
      props.put(ReSharperPlugin.PROJECT_NAME_PROPERTY_KEY, projectName);
    }
    if (solutionFile != null) {
      props.put(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY, solutionFile);
    }
    if (inspectcodePath != null) {
      props.put(ReSharperPlugin.INSPECTCODE_PATH_PROPERTY_KEY, inspectcodePath);
    }
    if (reportFile != null) {
      props.put(ReSharperPlugin.REPORT_FILE_PROPERTY_KEY, reportFile);
    }

    props.put(ReSharperPlugin.TIMEOUT_MINUTES_PROPERTY_KEY, "10");

    settings.addProperties(props);
    return settings;
  }

}
