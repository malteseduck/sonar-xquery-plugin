/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.XPathSubExpressionsInPredicateCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.XQueryParser.PredicateContext
import org.sonar.plugins.xquery.parser.XQueryParser.RelativePathExprContext
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of text() XPath steps.
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class XPathSubExpressionsInPredicateCheck : AbstractPredicateCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        if (inPredicate() && node is RelativePathExprContext) {
            // Create a violation for any XPath step expressions
            if (node.children?.any { it is TerminalNode } == true) {
                createIssue(RULE, node.getLine())
            }

        } else if (inPredicate() && node is PredicateContext) {
            // Create a violation for nested predicates
            createIssue(RULE, node.getLine())

        } else if (node is PredicateContext) {
            // If we are entering a predicate then update the state
            enterPredicate()
        }
    }

    companion object {
        // TODO: Maybe change this so it only catches nested predicates - unless
        // alternate ways to do sub-expressions is found

        const val RULE_KEY = "XPathSubExpressionsInPredicate"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}