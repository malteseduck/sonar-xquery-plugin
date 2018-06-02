/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.parser.XQueryParser.FunctionCallContext
import org.sonar.plugins.xquery.parser.XQueryParser.ModuleImportContext
import org.sonar.plugins.xquery.parser.getLine
import java.util.*

abstract class AbstractProhibitFunctionCheck : AbstractCheck() {

    var imports: MutableMap<String, String> = HashMap()
    protected abstract val functionNamespace: String?
    protected abstract val functionName: String?
    protected abstract val functionPrefix: String?

    override fun enterExpression(node: ParserRuleContext) {
        when (node) {
            is ModuleImportContext -> {
                val namespace = node.stringLiteral.text
                val prefix = node.prefix.text
                imports[prefix] = namespace
            }
            is FunctionCallContext -> {
                val function: String? = node.functionName()?.text

                // Do not process if for some reason the function name is not there
                if (function?.isNotBlank() == true) {
                    val parts = function.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    // If there is a prefix then set it, otherwise just set the function name
                    val funcPrefix = if (parts.size == 2) parts[0] else ""
                    val functionName = if (parts.size == 2) parts[1] else parts[0]

                    val funcNamespace = imports[funcPrefix]
                    val passPrefix = functionPrefix
                    if ((passPrefix != null && passPrefix == funcPrefix || funcNamespace != null && funcNamespace == functionNamespace) && functionName == functionName) {
                        createIssue(node.getLine())
                    }
                }
            }
        }
    }

    protected abstract fun createIssue(lineNumber: Int)
}