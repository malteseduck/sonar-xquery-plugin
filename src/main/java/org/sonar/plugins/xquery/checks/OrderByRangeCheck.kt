/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.parser.XQueryParser.OrderSpecContext
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of dynamic functions.
 *
 * @since 1.0
 */
@Rule(key = OrderByRangeCheck.RULE_KEY, name = "Range Evaluation in Order By Clause", description = "Order bys or gt/lt checks on large numbers of documents " + "might achieve better performance with a range index.", priority = Priority.INFO)
class OrderByRangeCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        if (node is OrderSpecContext) {
            createIssue(RULE, node.getLine())
        }
    }

    companion object {
        const val RULE_KEY = "OrderByRange"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}