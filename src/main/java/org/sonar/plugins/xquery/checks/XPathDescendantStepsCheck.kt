/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.XPathDescendantStepsCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.XQueryParser.DSLASH
import org.sonar.plugins.xquery.parser.XQueryParser.RelativePathExprContext
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of text() XPath steps.
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class XPathDescendantStepsCheck : AbstractPathCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        if (node is RelativePathExprContext) {
            if (node.children?.any { it is TerminalNode && it.symbol.type == DSLASH } == true) {
                createViolations(RULE, node, "//")
            }
        }
    }

    companion object {
        const val RULE_KEY = "XPathDescendantSteps"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}