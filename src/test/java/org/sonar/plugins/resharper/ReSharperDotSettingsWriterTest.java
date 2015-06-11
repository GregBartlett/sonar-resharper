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
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class ReSharperDotSettingsWriterTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test() throws Exception {
    File file1 = tmp.newFile();
    new ReSharperDotSettingsWriter().write(ImmutableList.of("foo", "bar"), file1);
    String contents1 = Files.toString(file1, Charsets.UTF_8);

    assertThat(contents1.replace("\r", "").replace("\n", ""))
      .isEqualTo(
        "<wpf:ResourceDictionary xml:space=\"preserve\" xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\" xmlns:s=\"clr-namespace:System;assembly=mscorlib\" xmlns:ss=\"urn:shemas-jetbrains-com:settings-storage-xaml\" xmlns:wpf=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\">"
          + "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=foo/@EntryIndexedValue\">WARNING</s:String>"
          + "  <s:String x:Key=\"/Default/CodeInspection/Highlighting/InspectionSeverities/=bar/@EntryIndexedValue\">WARNING</s:String>"
          + "</wpf:ResourceDictionary>");

    File file2 = tmp.newFile();
    new ReSharperDotSettingsWriter().write(ImmutableList.of("foo.bar", "foo:bar", "baz"), file2);
    String contents2 = Files.toString(file2, Charsets.UTF_8);
    assertThat(contents2)
      .contains("foo_002Ebar")
      .contains("foo_003Abar")
      .contains("baz");
  }

  @Test
  public void testWriteException() throws Exception {
    File file = tmp.newFile();
    file.setReadOnly();

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("java.io.FileNotFoundException: ");

    new ReSharperDotSettingsWriter().write(ImmutableList.of("foo", "bar"), file);
  }



}
