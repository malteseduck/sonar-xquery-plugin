/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.StrongTypingInFunctionDeclarationCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.XQueryParser.FunctionDeclContext
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for strong typing in function declarations
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class StrongTypingInFunctionDeclarationCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        // Process function declarations
        if (node is FunctionDeclContext) {

            // If we have any parameters declared - check them
            node.params?.let {
                it.forEach {
                    if (it.type == null) {
                        createIssue(RULE, it.getLine())
                    }
                }
            }

            // Check the return type
            if (node.type == null) {
                createIssue(RULE, node.getLine())
            }
        }
    }

    companion object {
        const val RULE_KEY = "StrongTypingInFunctionDeclaration"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}
