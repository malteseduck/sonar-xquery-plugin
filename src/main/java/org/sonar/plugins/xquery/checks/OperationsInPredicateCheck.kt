/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Priority
import org.sonar.check.Rule
import org.sonar.plugins.xquery.parser.XQueryParser.*
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for usage of text() XPath steps.
 *
 * @since 1.0
 */
@Rule(key = OperationsInPredicateCheck.RULE_KEY, name = "Avoid Operations in Predicates", description = "Instead of calling functions or performing operations in predicates " + "try assigning the results to a variable before the predicate.", priority = Priority.MAJOR)
class OperationsInPredicateCheck : AbstractPredicateCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        if (node is PredicateContext) {
            // Save the state of being in a predicate
            enterPredicate()
        }

        if (inPredicate() && EXPRESSIONS.contains(node::class)) {
            createIssue(RULE, node.getLine())
        }

        // Function calls in predicates are invalid
        if (inPredicate() && node is FunctionCallContext) {
            var valid = false

            // Look to see if the function is one of the "valid" ones
            if (FUNCTIONS.any { node.functionName()?.text?.endsWith(it) == true }) {
                valid = true
            }

            // If the function call is not "valid" then create a violation
            if (!valid) {
                createIssue(RULE, node.getLine())
            }

        }
    }

    companion object {
        // TODO: Either create a new check for or add to this checks for operations in xdmp:directory()
        // TODO: Support for fn:local-name(), fn:name(), fn:node-name()?

        const val RULE_KEY = "OperationsInPredicate"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)

        private val FUNCTIONS = listOf("data", "last", "not", "exists", "xs:integer", "string", "xs:decimal", "xs:double", "xs:float", "xs:date", "xs:dateTime", "xs:time", "xs:dayTimeDuration", "xs:yearMonthDuration", "xs:duration")
        private val EXPRESSIONS = listOf(AddContext::class, MultContext::class)
    }
}