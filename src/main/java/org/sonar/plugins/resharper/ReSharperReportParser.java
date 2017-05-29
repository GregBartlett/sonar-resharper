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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;

import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReSharperReportParser {

  public List<ReSharperIssue> parse(File file, String projectName) {
    return new Parser().parse(file, projectName);
  }

  private static class Parser {
    private File file;
    private String projectName;
    private XMLStreamReader stream;
    private final ImmutableList.Builder<ReSharperIssue> filesBuilder = ImmutableList.builder();

    public List<ReSharperIssue> parse(File file, String projectName) {
      this.file = file;
      this.projectName = projectName;

      InputStreamReader reader = null;
      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

      try {
        reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        stream = xmlFactory.createXMLStreamReader(reader);

        while (stream.hasNext()) {
          if (stream.next() == XMLStreamConstants.START_ELEMENT) {
            String tagName = stream.getLocalName();

            if ("Project".equals(tagName)) {
              if(handleProjectTag()) {
                // break after project tag has been handled.
                break;
              };              
            }
          }
        }
      } catch (IOException | XMLStreamException e) {
        throw Throwables.propagate(e);
      } finally {
        closeXmlStream();
        Closeables.closeQuietly(reader);
      }

      return filesBuilder.build();
    }

    private void closeXmlStream() {
      if (stream != null) {
        try {
          stream.close();
        } catch (XMLStreamException e) {
          throw new IllegalStateException(e);
        }
      }
    }
    
    private boolean handleProjectTag() throws XMLStreamException {
      boolean handled = false;
      
      String projectName = getAttribute("Name");
      if(this.projectName.equals(projectName)) {
        handled = true;
        while(stream.hasNext()) {
          int nextTag = stream.next();
          if(nextTag == XMLStreamConstants.START_ELEMENT) {
            String tagName = stream.getLocalName();

            if ("Issue".equals(tagName)) {
              handleIssueTag();
            }
          }
          else {
            if(nextTag == XMLStreamConstants.END_ELEMENT) {
              String tagName = stream.getLocalName();
              
              if("Project".equals(tagName)) {
                return handled;
              }                  
            }
          }
        }
      }
      return handled;
    }

    private void handleIssueTag() throws XMLStreamException {
      String typeId = getRequiredAttribute("TypeId");
      String filePath = getAttribute("File");
      Integer line = getIntAttribute("Line");
      String message = getRequiredAttribute("Message");
      ReSharperIssue uniqueIssue = new ReSharperIssue(stream.getLocation().getLineNumber(), typeId, filePath, line, message);
      for(ReSharperIssue issue : filesBuilder.build()) {
        if (issue.ruleKey().equals(issue.ruleKey())
                && issue.filePath().equals(uniqueIssue.filePath())
                && issue.message().equals(uniqueIssue.message())) {          
          if(issue.line() == null) {           
            return;
          } else {
            if(issue.line().equals(uniqueIssue.line())) {
              return;
            }          
          }          
        }
      }
      
      filesBuilder.add(uniqueIssue);
    }

    private String getRequiredAttribute(String name) {
      String value = getAttribute(name);
      if (value == null) {
        throw parseError("Missing attribute \"" + name + "\" in element <" + stream.getLocalName() + ">");
      }

      return value;
    }

    @Nullable
    private Integer getIntAttribute(String name) {
      String value = getAttribute(name);

      if (value == null) {
        return null;
      }

      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw parseError("Expected an integer instead of \"" + value + "\" for the attribute \"" + name + "\"");
      }
    }

    @Nullable
    private String getAttribute(String name) {
      for (int i = 0; i < stream.getAttributeCount(); i++) {
        if (name.equals(stream.getAttributeLocalName(i))) {
          return stream.getAttributeValue(i);
        }
      }

      return null;
    }

    private ParseErrorException parseError(String message) {
      return new ParseErrorException(
          message + " in " + file.getAbsolutePath() + " at line " + stream.getLocation().getLineNumber());
    }

  }

  private static class ParseErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParseErrorException(String message) {
      super(message);
    }

  }

}
