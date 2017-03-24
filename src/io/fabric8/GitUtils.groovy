#!/usr/bin/groovy
package io.fabric8

import se.bjurr.gitchangelog.api.GitChangelogApi
import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def getChangeLog(String buildName,String directory){

  def TEMPLATE = "# ${buildName} Changelog\n" +
          "\n" +
          "{{#tags}}\n" +
          "## {{name}}\n" +
          " {{#issues}}\n" +
          "  {{#hasIssue}}\n" +
          "   {{#hasLink}}\n" +
          "### {{name}} [{{issue}}]({{link}}) {{title}}\n" +
          "   {{/hasLink}}\n" +
          "   {{^hasLink}}\n" +
          "### {{name}} {{issue}} {{title}}\n" +
          "   {{/hasLink}}\n" +
          "  {{/hasIssue}}\n" +
          "  {{^hasIssue}}\n" +
          "### {{name}}\n" +
          "  {{/hasIssue}}\n" +
          "\n" +
          "  {{#commits}}\n" +
          "**{{{messageTitle}}}**\n" +
          "\n" +
          "{{#messageBodyItems}}\n" +
          " * {{.}}\n" +
          "{{/messageBodyItems}}\n" +
          "\n" +
          "[{{hash}}] {{authorName}} *{{commitTime}}*\n" +
          "\n" +
          "  {{/commits}}\n" +
          "\n" +
          " {{/issues}}\n" +
          "{{/tags}}\n";

  GitChangelogApi gitChangelogApi = GitChangelogApi.gitChangelogApiBuilder();
  echo "Getting changelog from directory ${directory}"
  def changeLog = gitChangelogApi.withFromRepo(directory).withTemplateContent(TEMPLATE).render();
  return changeLog
}


return this
