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
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks for reliance on effective boolean values in conditionals.
 *
 * @since 1.0
 */
@Rule(key = EffectiveBooleanCheck.RULE_KEY, name = "Effective Boolean in Conditional Predicate", description = "Unless the value in the conditional is of type xs:boolean it is recommended you use " + "fn:exists(), fn:empty(), or other boolean functions inside of conditional predicates to check values.", priority = Priority.MINOR)
class EffectiveBooleanCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

//        if (node is IfExprContext) {
//            val primary = node.conditionExpr.find("primaryExpr") as PrimaryExprContext


//        if (node is IfExprContext) {
//            var valid = false
//
//            // Function calls are path expressions, make sure they are the only
//            // node in the path expression
//            val pathExpr = node.find("IfPredicate.UnaryExpr.PathExpr", false)
//            if (pathExpr != null && pathExpr!!.getChildCount() === 1) {
//
//                // Check for valid function calls
//                val functionName = pathExpr!!.getChildTextValue("PathExpr.FunctionCall.FunctionName.QName")
//                val decl = mapper!!.getFunctionDeclaration(functionName, mapper!!.resolvePrefixNamespace(functionName))
//                if (decl != null && "xs:boolean" == decl.type) {
//                    valid = true
//                } else {
//                    for (function in FUNCTIONS) {
//                        if (StringUtils.endsWith(functionName, function)) {
//                            valid = true
//                        }
//                    }
//                }
//            }
//
//            // Check to see if there is a boolean expression
//            var predicate = node.getValue()
//            for (expression in EXPRESSIONS) {
//                if (StringUtils.contains(predicate, expression)) {
//                    valid = true
//                }
//            }
//
//            // Check to see if there is a variable reference and if the variable
//            // is a boolean
//            predicate = node.getTextValue("PathExpr")
//            if ("\$QName" == predicate) {
//                val varName = node.getTextValue("PathExpr.QName")
//                // TODO: do a "resolve namespace" function on the qname against registered prefixes
//                val decl = mapper!!.getVariableDeclaration(varName, mapper!!.resolvePrefixNamespace(varName))
//                if (decl != null && "xs:boolean" == decl.type) {
//                    valid = true
//                }
//            }
//
//            if (!valid) {
//                createIssue(RULE, node.getLine())
//            }
//        }
    }

    companion object {

        const val RULE_KEY = "EffectiveBoolean"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)

        private val FUNCTIONS = listOf("exists", "empty", "contains", "starts-with", "ends-with", "boolean", "not", "true", "false", "matches")
        private val EXPRESSIONS = listOf(ComparisonContext::class, CastableContext::class, TreatContext::class, InstanceOfContext::class)

    }
}