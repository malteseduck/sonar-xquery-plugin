/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.parser.XQueryParser
import org.sonar.plugins.xquery.parser.XQueryParser.*
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of text() XPath steps.
 *
 * @since 1.0
 */
@Rule(key = XPathDescendantStepsCheck.RULE_KEY, name = "Avoid Using '//' in XPath", description = "Favor fully-qualified paths in XPath " + "for readability and to avoid potential performance problems.", priority = Priority.MINOR)
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
        const val RULE_KEY = "XpathDescendantSteps"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}