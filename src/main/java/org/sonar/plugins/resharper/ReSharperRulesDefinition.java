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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import java.nio.charset.StandardCharsets;

public abstract class ReSharperRulesDefinition implements RulesDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(ReSharperRulesDefinition.class);
  private static final String REPOSITORY_NAME = "ReSharper";

  private final RulesDefinitionXmlLoader rulesDefinitionXmlLoader = new RulesDefinitionXmlLoader();
  private final ReSharperConfiguration configuration;

  public ReSharperRulesDefinition(ReSharperConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void define(Context context) {
    String languageKey = configuration.languageKey();
    NewRepository repository = context
      .createRepository(configuration.repositoryKey(), languageKey)
      .setName(REPOSITORY_NAME);
    rulesDefinitionXmlLoader.load(repository, getClass().getResourceAsStream("/org/sonar/plugins/resharper/rules.xml"), StandardCharsets.UTF_8.name());
    SqaleXmlLoader.load(repository, "/org/sonar/plugins/resharper/sqale.xml");
    repository.done();
    LOG.info("ReSharper rules for " + languageKey + " imported.");
  }
}
