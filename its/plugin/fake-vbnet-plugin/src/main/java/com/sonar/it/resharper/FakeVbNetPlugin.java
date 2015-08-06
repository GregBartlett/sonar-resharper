/*
 * fake-vbnet-plugin
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
package com.sonar.it.resharper;

import org.sonar.api.SonarPlugin;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.utils.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

public class FakeVbNetPlugin extends SonarPlugin {

  @Override
  public List getExtensions() {
    List extensions = new ArrayList<Object>();

    extensions.add(new AbstractLanguage("vbnet") {

      @Override
      public String[] getFileSuffixes() {
        return new String[] {".vb"};
      }

    });

    extensions.add(new ProfileDefinition() {

      @Override
      public RulesProfile createProfile(ValidationMessages validation) {
        RulesProfile profile = RulesProfile.create("Sonar Way", "vbnet");
        profile.setDefaultProfile(true);
        return profile;
      }

    });

    return extensions;
  }

}
