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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReSharperDotSettingsWriter {

  public void write(List<String> ruleKeys, File file) {
    StringBuilder sb = new StringBuilder();

    sb.append("<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\"");
    sb.append(" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\"");
    appendLine(sb, " xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">");

    for (String ruleKey : ruleKeys) {
      String escapedRuleKey = escapeRuleKey(ruleKey);
      appendLine(sb, "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=" + escapedRuleKey + "/@EntryIndexedValue\">WARNING</s:String>");
    }

    appendLine(sb, "</wpf:ResourceDictionary>");

    try {
      Files.write(sb.toString().getBytes(Charsets.UTF_8), file);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String escapeRuleKey(String ruleKey) {
    return ruleKey.replace(".", "_002E").replace(":", "_003A");
  }

  private static void appendLine(StringBuilder sb, String s) {
    sb.append(s);
    sb.append(IOUtils.LINE_SEPARATOR);
  }

}
