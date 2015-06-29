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

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;

import java.io.IOException;
import java.io.Writer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ReSharperProfileExporterTest {

  @org.junit.Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testWriterException() throws Exception {
    Writer writer = mock(Writer.class);
    doThrow(IOException.class).when(writer).write(anyString());

    RulesProfile profile = RulesProfile.create("name", "key");
    profile.activateRule(Rule.create("key", "ruleKey", "ruleName"), null);

    ReSharperProfileExporter exporter = new ReSharperProfileExporter(new ReSharperConfiguration("key", "key", "key"));

    thrown.expectMessage("Failed to export profile [name=name,language=key]");
    thrown.expect(IllegalStateException.class);

    exporter.exportProfile(profile, writer);
  }
}