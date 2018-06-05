/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import jdk.nashorn.internal.ir.FunctionCall
import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.check.Rule
import org.sonar.plugins.xquery.checks.EffectiveBooleanCheck.Companion.RULE_KEY
import org.sonar.plugins.xquery.parser.*
import org.sonar.plugins.xquery.parser.XQueryParser.*
import org.sonar.plugins.xquery.rules.CheckClasses
import javax.sound.sampled.AudioSystem.getLine
import kotlin.reflect.KClass


/**
 * Checks for reliance on effective boolean values in conditionals.
 *
 * @since 1.0
 */
@Rule(key = RULE_KEY)
class EffectiveBooleanCheck : AbstractCheck() {

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        var failureLine = node.getLine()
        if (node is IfExprContext) {

            var valid = false

            // Function calls are path expressions, make sure they are the only
            // node in the path expression
            val exprContext: ExprSingleContext? = node.conditionExpr?.findInOrNull(false, ExprContext::class)
            val pathExpr: RelativePathExprContext? = exprContext?.orExpr()?.find()
            if (pathExpr != null && pathExpr.stepExpr().size == 1) {

                // Check for valid function calls
                val functionCall: FunctionCallContext? = pathExpr.findOrNull()
                if (functionCall != null) {
                    val functionName = functionCall.functionName().text
                    val decl = mapper!!.getFunctionDeclaration(functionName.substringAfter(":"), mapper!!.resolvePrefixNamespace(functionName))
                    failureLine = functionCall.getLine()
                    if (decl != null && "xs:boolean" == decl.type) {
                        valid = true
                    } else {
                        for (function in FUNCTIONS) {
                            if (functionName.endsWith(function)) {
                                valid = true
                            }
                        }
                    }
                }
            }

            // Check to see if there is a boolean expression
            var predicate = node.conditionExpr
            failureLine = predicate.getLine()
            if (predicate.exists<ComparisonContext>()
                || predicate.exists<CastableContext>()
                || predicate.exists<TreatContext>()
                || predicate.exists<InstanceOfContext>()) {
                valid = true
            }

            // Check to see if there is a variable reference and if the variable
            // is a boolean
            val varRef: VarContext? = pathExpr?.findOrNull()
            if (varRef != null) {
                val varName = varRef.qName().text
                failureLine = varRef.getLine()
                // TODO: do a "resolve namespace" function on the qname against registered prefixes
                val decl = mapper!!.getVariableDeclaration(varName.substringAfter(":"), mapper!!.resolvePrefixNamespace(varName))
                if (decl != null && "xs:boolean" == decl.type) {
                    valid = true
                }
            }

            if (!valid) {
                createIssue(RULE, failureLine)
            }
        }
    }

    companion object {
        const val RULE_KEY = "EffectiveBoolean"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
        private val FUNCTIONS = listOf("exists", "empty", "contains", "starts-with", "ends-with", "boolean", "not", "true", "false", "matches")
    }
}