/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.parser.XQueryParser
import org.sonar.plugins.xquery.parser.XQueryParser.*
import org.sonar.plugins.xquery.parser.find
import org.sonar.plugins.xquery.parser.findInOrNull
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of text() XPath steps.
 *
 * @since 1.0
 */
@Rule(key = XPathTextStepsCheck.RULE_KEY, name = "Avoid Using text() in XPath", description = "Generally avoid using /text() in your XPath in favor of using fn:string() or allowing atomization " + "(through strong typing or default atomization).", priority = Priority.MINOR)
class XPathTextStepsCheck : AbstractPathCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        // Only do further checking on path expressions
        if (node is StepExprContext) {
            // text() function calls in path expressions are invalid
            node.findInOrNull<TextTestContext>(NodeTestContext::class, KindTestContext::class)?.let {
                createIssue(RULE, it.getLine())
            }
        }
    }

    companion object {
        const val RULE_KEY = "XpathTextSteps"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}