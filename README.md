SonarQube ReSharper Plugin
==========================

[![Build status](https://ci.appveyor.com/api/projects/status/ddgsh9ulybd1c2km/branch/master?svg=true)](https://ci.appveyor.com/project/SonarSource/sonar-resharper/branch/master)

## Description
This plugin enables the analysis of C# and VB.NET source files contained in .NET projects with [JetBrains ReSharper Command Line Tools](http://www.jetbrains.com/resharper/features/command-line.html)
* Supports [JetBrains ReSharper Command Line Tools](http://www.jetbrains.com/resharper/features/command-line.html) (at least version 8.2.0.2151)
* Compatible with the [C# Plugin v4.1+](http://docs.sonarqube.org/display/PLUG/C%23+Plugin)
* Compatible with the [Visual Basic.NET Plugin v2.4+](http://docs.sonarqube.org/display/PLUG/Visual+Basic+.NET+Plugin)

## Steps to Analyze a Project with ReSharper Command Line Tools
Visual Basic .NET support
The example below is for C#. VB.NET Plugin users should use the property "sonar.resharper.vbnet.reportPath" instead of "sonar.resharper.cs.reportPath".

Here are the exact steps to import ReSharper Command Line Tools results into SonarQube, using the [SonarQube Scanner for MSBuild](http://docs.sonarqube.org/display/SONAR/Analyzing+with+SonarQube+Scanner+for+MSBuild) from the command line:

1. Enable some ReSharper rules in your quality profile (see [Quality Profiles](http://docs.sonarqube.org/display/SONAR/Quality+Profiles) for more details)
2. Open a Developer Command Prompt for Visual Studio
3. Put yourself in the root folder of the project you want to analyze (a sample project, available on GitHub, can be [browsed](https://github.com/SonarSource/sonar-examples/tree/master/projects/languages/csharp) or [downloaded](https://github.com/SonarSource/sonar-examples/zipball/master): projects\languages\csharp)
4. Run the following commands:

* Begin the SonarQube Analysis and provide all required properties, including "sonar.resharper.solutionFile" and "sonar.resharper.cs.reportPath"

> MSBuild.SonarQube.Runner.exe begin /k:"sonarqube_project_key" /n:"sonarqube_project_name" /v:"sonarqube_project_version" /d:sonar.resharper.cs.reportPath="%CD%\resharper.xml" /d:sonar.resharper.solutionFile="%CD%\ConsoleApplication1.sln"

* Build the project, for example:

> msbuild "%CD%\ConsoleApplication1.sln"

* Run ReSharper's Command Line Tool inspectcode.exe

> "C:\jetbrains-commandline-tools\inspectcode.exe" /output="%CD%\resharper.xml" "%CD%\ConsoleApplication1.sln"

* End the SonarQube Analysis and upload it to the SonarQube server

> MSBuild.SonarQube.Runner.exe end

##### "Skipping the ReSharper issue at..." log messages during the MSBuild.SonarQube.Runner end command

Many "Skipping the ReSharper issue at line..." messages can be shown in the logs at INFO level even if ReSharper issues are correctly imported into SonarQube: These messages are too verbose and can be safely ignored.

See Issue: Too many "Skipping the ReSharper issue at line ..." messages Open (SONARRSHPR-21)
