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

import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ReSharperProfileExporter extends ProfileExporter {
  private final ReSharperConfiguration configuration;

  public ReSharperProfileExporter(ReSharperConfiguration configuration) {
    super(configuration.repositoryKey(), "ReSharper DotSetting");
    this.configuration = configuration;
    setSupportedLanguages(configuration.languageKey());
  }

  @Override
  public void exportProfile(RulesProfile profile, Writer writer) {
    ReSharperDotSettingsWriter reSharperDotSettingsWriter = new ReSharperDotSettingsWriter();
    List<ActiveRule> activeRules = profile.getActiveRulesByRepository(configuration.repositoryKey());
    List<String> activeRuleKeys = new ArrayList<>();
    for (ActiveRule activeRule : activeRules) {
      activeRuleKeys.add(activeRule.getRuleKey());
    }
    try {
      reSharperDotSettingsWriter.write(activeRuleKeys, writer);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to export profile " + profile, e);
    }
  }
}
