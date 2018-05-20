/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.apache.commons.lang.StringUtils
import org.sonar.plugins.xquery.parser.*
import org.sonar.plugins.xquery.parser.XQueryParser.*

import java.util.ArrayList
import java.util.regex.Pattern

abstract class AbstractProhibitVariableCheck : AbstractCheck() {

    protected abstract val pattern: String

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        val names = ArrayList<String>()

        when (node) {
            is ForVarContext -> {
                names.add(node.name.text)
                names.add(node.pvar.text)
//                names.add(node.getValue("qName"))
            }
            is ParamContext -> names.add(node.name.text)
            is VarDeclContext -> names.add(node.name.text)
            is LetVarContext -> names.add(node.name.text)
        }

        if (names.stream()
                .filter { it.isNotBlank() }
                .filter { name -> Pattern.matches(pattern, name) }
                .count() > 0) {
            createIssue(node.getLine())
        }

    }

    protected abstract fun createIssue(lineNumber: Int)
}
