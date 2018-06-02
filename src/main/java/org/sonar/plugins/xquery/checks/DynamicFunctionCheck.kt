/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.DynamicFunctionCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.XQueryParser.FunctionCallContext
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of dynamic functions.
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class DynamicFunctionCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        if (node is FunctionCallContext) {
            val function = node.functionName().text
            if ("xdmp:eval" == function || "xdmp:value" == function) {
                createIssue(RULE, node.getLine())
            }
        }
    }

    companion object {
        const val RULE_KEY = "DynamicFunction"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}