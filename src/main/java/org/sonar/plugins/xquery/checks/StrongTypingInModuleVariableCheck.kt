/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.StrongTypingInModuleVariableCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.XQueryParser
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for strong typing in module variable declarations
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class StrongTypingInModuleVariableCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        // Process variable declarations
        if (node is XQueryParser.VarDeclContext) {

            // If we don't have the variable type then create a violation
            if (node.type == null) {
                createIssue(RULE, node.getLine())
            }
        }
    }

    companion object {
        const val RULE_KEY = "StrongTypingInModuleVariables"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}
