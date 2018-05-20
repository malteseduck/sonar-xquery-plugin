/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.sonar.api.rule.RuleKey
import org.sonar.plugins.xquery.parser.getLine

/**
 * Abstract class for checking rules inside of XPath expressions. Keeps track of
 * entering and exiting path expressions.
 *
 * @since 1.0
 */
open class AbstractPathCheck : AbstractCheck() {

    protected fun createViolations(rule: RuleKey, expr: ParserRuleContext, line: Int) {
        createIssue(rule, line)
    }

        /**
     * Find the index of the "offending" steps (on multi-line expressions the
     * line number could be higher)
     *
     * @param expr
     * The path expression node
     * @param expression
     * The expression for which to check
     * @return the line number of the expression, or of the node if the
     * expression wasn't found
     */
    protected fun createViolations(rule: RuleKey, expr: ParserRuleContext, expression: String) {
        expr.children
                .forEach { child ->
                    val value = child.text
                    if (expression == value) {
                        val line = when (child) {
                            is TerminalNode -> child.symbol.line
                            else -> child.getLine()
                        }
                        createIssue(rule, line)
                    }
                }
//        val size = children.size()
//        for (i in 0 until size) {
//
//            // Check the text of the current node
//            val value = children.get(i).getText()
//            if (expression == value) {
//                var index = i
//                // We want the node after the steps to get the line
//                if (index < size + 1) {
//                    index++
//                }
//                createIssue(rule, children.get(index).getLine())
//            }
//        }
    }
}