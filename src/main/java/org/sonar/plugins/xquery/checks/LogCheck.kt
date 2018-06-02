/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.LogCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of the xdmp:log function.
 *
 * @since 1.1
 */
@Rule(key = RULE_KEY)
class LogCheck : AbstractProhibitFunctionCheck() {

    override val functionNamespace: String? = null
    override val functionName: String? = "log"
    override val functionPrefix: String? ="xdmp"

    override fun createIssue(lineNumber: Int) {
        createIssue(RULE, lineNumber)
    }

    companion object {

        const val RULE_KEY = "LogCheck"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}