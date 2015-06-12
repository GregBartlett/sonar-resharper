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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReSharperSensorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldExecuteOnProject() {
    Settings settings = mock(Settings.class);
    RulesProfile profile = mock(RulesProfile.class);
    DefaultFileSystem fileSystem = new DefaultFileSystem();
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    Project project = mock(Project.class);

    ReSharperSensor sensor = new ReSharperSensor(
      new ReSharperConfiguration("lang", "foo-resharper", "fooReportkey"),
      settings, profile, fileSystem, perspectives);

    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    fileSystem.add(new DefaultInputFile("").setAbsolutePath("").setLanguage("foo"));
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(ImmutableList.<ActiveRule>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    fileSystem.add(new DefaultInputFile("").setAbsolutePath("").setLanguage("lang"));
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(ImmutableList.of(mock(ActiveRule.class)));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();

    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(ImmutableList.<ActiveRule>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void analyze_report_path() throws Exception {
    String languageKey = "foo";
    String reportPathKey = "fooReport";
    Settings settings = new Settings();
    settings.setProperty(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY, "CSharpPlayground.sln");
    settings.setProperty(reportPathKey, "src/test/resources/SensorTest/report.xml");

    RulesProfile profile = mock(RulesProfile.class);
    DefaultFileSystem fileSystem = new DefaultFileSystem();
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    ReSharperSensor sensor = new ReSharperSensor(
      new ReSharperConfiguration(languageKey, "foo-resharper", "fooReport"),
      settings, profile, fileSystem, perspectives);

    List<ActiveRule> activeRules = mockActiveRules("RedundantUsingDirective");
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(activeRules);

    File workingDir = new File("src/test/resources/SensorTest");
    fileSystem.setWorkDir(workingDir);

    DefaultInputFile class1Cs = new DefaultInputFile("MyLibrary/Class1.cs").setAbsolutePath("MyLibrary/Class1.cs").setLanguage(languageKey);
    fileSystem.add(class1Cs);
    DefaultInputFile assemblyInfoCs = new DefaultInputFile("MyLibrary/Properties/AssemblyInfo.cs").setAbsolutePath("MyLibrary/Properties/AssemblyInfo.cs").setLanguage(languageKey);
    fileSystem.add(assemblyInfoCs);

    Issue issue1 = mock(Issue.class);
    IssueBuilder issueBuilder1 = mockIssueBuilder();
    when(issueBuilder1.build()).thenReturn(issue1);

    Issuable issuable1 = mock(Issuable.class);
    when(perspectives.as(Issuable.class, class1Cs)).thenReturn(issuable1);
    when(issuable1.newIssueBuilder()).thenReturn(issueBuilder1);

    Issue issue2 = mock(Issue.class);
    IssueBuilder issueBuilder2 = mockIssueBuilder();
    when(issueBuilder2.build()).thenReturn(issue2);

    Issuable issuable2 = mock(Issuable.class);
    when(perspectives.as(Issuable.class, assemblyInfoCs)).thenReturn(issuable2);
    when(issuable2.newIssueBuilder()).thenReturn(issueBuilder2);

    sensor.analyse(mock(Project.class), mock(SensorContext.class));

    verify(issuable1).addIssue(issue1);
    verify(issuable2).addIssue(issue2);

    verify(issueBuilder1).line(1);
    verify(issueBuilder1).message("Using directive is not required by the code and can be safely removed");

    verify(issueBuilder2).line(2);
    verify(issueBuilder2).message("Using directive is not required by the code and can be safely removed");
  }

  @Test
  public void analyze_run_inspect_code() throws Exception {
    Settings settings = createSettings("MyLibrary", "CSharpPlayground.sln", "inspectcode.exe");
    RulesProfile profile = mock(RulesProfile.class);
    DefaultFileSystem fileSystem = new DefaultFileSystem();
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    String languageKey = "foo";
    ReSharperSensor sensor = new ReSharperSensor(
      new ReSharperConfiguration(languageKey, "foo-resharper", "fooReportkey"),
      settings, profile, fileSystem, perspectives);

    List<ActiveRule> activeRules = mockActiveRules("AccessToDisposedClosure", "AccessToForEachVariableInClosure");
    when(profile.getActiveRulesByRepository("foo-resharper")).thenReturn(activeRules);

    FileProvider fileProvider = mock(FileProvider.class);
    ReSharperExecutor executor = mock(ReSharperExecutor.class);

    File workingDir = new File("target/ReSharperSensorTest/working-dir");
    fileSystem.setWorkDir(workingDir);

    File fileNotInSonarQube = mock(File.class);
    when(fileNotInSonarQube.getPath()).thenReturn("fileNotInSonarQube");
    fileSystem.add(new DefaultInputFile("fileNotInSonarQube").setAbsolutePath("fileNotInSonarQube").setLanguage(languageKey));

    File fooFileWithIssuable = mock(File.class);
    when(fooFileWithIssuable.getPath()).thenReturn("fooFileWithIssuable");
    DefaultInputFile inputFileWithIssues = new DefaultInputFile("fooFileWithIssuable").setAbsolutePath("fooFileWithIssuable").setLanguage(languageKey);
    fileSystem.add(inputFileWithIssues);

    File fooFileWithoutIssuable = mock(File.class);
    when(fooFileWithoutIssuable.getPath()).thenReturn("fooFileWithoutIssuable");
    fileSystem.add(new DefaultInputFile("fooFileWithoutIssuable").setAbsolutePath("fooFileWithoutIssuable").setLanguage(languageKey));

    File barFile = mock(File.class);
    when(barFile.getPath()).thenReturn("barFile");
    fileSystem.add(new DefaultInputFile("barFile").setAbsolutePath("barFile"));

    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class3.cs"))).thenReturn(fileNotInSonarQube);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class4.cs"))).thenReturn(fooFileWithIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class5.cs"))).thenReturn(fooFileWithIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class6.cs"))).thenReturn(fooFileWithoutIssuable);
    when(fileProvider.fileInSolution(Mockito.any(File.class), Mockito.eq("Class7.cs"))).thenReturn(barFile);

    Issue issue1 = mock(Issue.class);
    IssueBuilder issueBuilder1 = mockIssueBuilder();
    when(issueBuilder1.build()).thenReturn(issue1);

    Issue issue2 = mock(Issue.class);
    IssueBuilder issueBuilder2 = mockIssueBuilder();
    when(issueBuilder2.build()).thenReturn(issue2);

    Issuable issuable = mock(Issuable.class);
    when(perspectives.as(Issuable.class, inputFileWithIssues)).thenReturn(issuable);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder1, issueBuilder2);

    ReSharperDotSettingsWriter writer = mock(ReSharperDotSettingsWriter.class);

    ReSharperReportParser parser = mock(ReSharperReportParser.class);
    when(parser.parse(new File(workingDir, "resharper-report.xml").getAbsoluteFile())).thenReturn(
      ImmutableList.of(
        new ReSharperIssue(100, "AccessToDisposedClosure", null, 1, "Dummy message"),
        new ReSharperIssue(200, "AccessToDisposedClosure", "Class2.cs", null, "Dummy message"),
        new ReSharperIssue(400, "AccessToDisposedClosure", "Class3.cs", 3, "First message"),
        new ReSharperIssue(500, "AccessToDisposedClosure", "Class4.cs", 4, "Second message"),
        new ReSharperIssue(600, "AccessToForEachVariableInClosure", "Class5.cs", 5, "Third message"),
        new ReSharperIssue(700, "AccessToDisposedClosure", "Class6.cs", 6, "Fourth message"),
        new ReSharperIssue(800, "AccessToDisposedClosure", "Class7.cs", 7, "Fifth message")));

    sensor.analyseRunInspectCode(fileProvider, writer, parser, executor);

    verify(writer).write(ImmutableList.of("AccessToDisposedClosure", "AccessToForEachVariableInClosure"), new File(workingDir, "resharper-sonarqube.DotSettings").getAbsoluteFile());
    verify(executor).execute(
      "inspectcode.exe", "MyLibrary", "CSharpPlayground.sln",
      new File(workingDir, "resharper-sonarqube.DotSettings").getAbsoluteFile(), new File(workingDir, "resharper-report.xml").getAbsoluteFile(), 10);

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

    Settings settings = createSettings(null, "dummy.sln", null);
    createReSharperSensor(settings).analyse(mock(Project.class), mock(SensorContext.class));
  }

  @Test
  public void check_solution_file_property() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(ReSharperPlugin.SOLUTION_FILE_PROPERTY_KEY);

    Settings settings = createSettings("Dummy Project", null, null);
    createReSharperSensor(settings).analyse(mock(Project.class), mock(SensorContext.class));
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

  private static ReSharperSensor createReSharperSensor(Settings settings) {
    ReSharperConfiguration reSharperConf = new ReSharperConfiguration("", "", "");
    return new ReSharperSensor(reSharperConf, settings, mock(RulesProfile.class), mock(FileSystem.class), mock(ResourcePerspectives.class));
  }

  private static Settings createSettings(@Nullable String projectName, @Nullable String solutionFile, @Nullable String inspectcodePath) {
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

    props.put(ReSharperPlugin.TIMEOUT_MINUTES_PROPERTY_KEY, "10");

    settings.addProperties(props);
    return settings;
  }

}
