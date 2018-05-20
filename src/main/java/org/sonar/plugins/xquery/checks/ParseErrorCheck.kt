/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.apache.commons.lang.StringUtils
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.rules.CheckClasses

@Rule(key = ParseErrorCheck.RULE_KEY, name = "Code Parsing Error", description = "This is to catch parsing errors on projects. " + "There may be a potential syntax error, or the parser just may not be able to process certain syntax.", priority = Priority.INFO)
class ParseErrorCheck : AbstractCheck() {

    override fun checkReport(reporter: ProblemReporter) {
        var allowed = false

        for (problem in reporter.problems) {
            val pMessage = problem.message
            val line = problem.line

            // Look at all the "allowed" messages to see if the problem matches
            for (message in MESSAGES) {
                if (StringUtils.contains(pMessage, message)) {
                    allowed = true
                }
            }

            if (!allowed && line > 0) {
                createIssue(RULE, line, problem.message)
            }
        }
    }

    companion object {
        private val MESSAGES = arrayOf("no viable alternative at character 'D'")
        const val RULE_KEY = "ParseError"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}
