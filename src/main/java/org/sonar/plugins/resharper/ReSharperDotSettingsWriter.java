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

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReSharperDotSettingsWriter {

  public void write(List<String> ruleKeys, File file) {
    try {
      BufferedWriter writer = Files.newWriter(file, StandardCharsets.UTF_8);
      write(ruleKeys, writer);
      writer.flush();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public void write(List<String> ruleKeys, Writer writer) throws IOException {
    writer.write("<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\"");
    writer.write(" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\"");
    appendLine(writer, " xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">");

    for (String ruleKey : ruleKeys) {
      String escapedRuleKey = escapeRuleKey(ruleKey);
      appendLine(writer, "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=" + escapedRuleKey + "/@EntryIndexedValue\">WARNING</s:String>");
    }
    appendLine(writer, "</wpf:ResourceDictionary>");
  }

  private static String escapeRuleKey(String ruleKey) {
    return ruleKey.replace(".", "_002E").replace(":", "_003A");
  }

  private static void appendLine(Writer writer, String s) throws IOException {
    writer.write(s);
    writer.write(IOUtils.LINE_SEPARATOR);
  }

}
