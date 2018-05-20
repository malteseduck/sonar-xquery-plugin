/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.parser.XQueryParser.FunctionCallContext
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of dynamic functions.
 *
 * @since 1.0
 */
@Rule(key = DynamicFunctionCheck.RULE_KEY, name = "Dynamic Function Usage (Marklogic)", description = "Avoid using xdmp:eval() and xdmp:value() where possible. " +
        "Instead use either xdmp:invoke(), xdmp:unpath() " +
        "or if possible assign functions to variables to dynamically evaluate code logic.\n" +
        "Please note that this check is Marklogic specific.", priority = Priority.MAJOR)
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