/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.StrongTypingInFLWORCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.XQueryParser.ForVarContext
import org.sonar.plugins.xquery.parser.XQueryParser.LetVarContext
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for strong typing in module variable declarations
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class StrongTypingInFLWORCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        when (node) {
            is ForVarContext -> if (node.type == null) createIssue(RULE, node.getLine())
            is LetVarContext -> if (node.type == null) createIssue(RULE, node.getLine())
        }
    }

    companion object {
        const val RULE_KEY = "StrongTypingInFLWOR"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}
