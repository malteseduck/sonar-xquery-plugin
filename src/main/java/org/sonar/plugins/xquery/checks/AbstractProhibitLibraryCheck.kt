/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.parser.XQueryParser.ModuleDeclContext
import org.sonar.plugins.xquery.parser.getLine

abstract class AbstractProhibitLibraryCheck : AbstractCheck() {
    protected abstract val namespace: String

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        if (node is ModuleDeclContext) {
            val namespace = node.uri.text
            if (namespace == namespace) {
                createIssue(node.getLine())
            }
        }
    }

    protected abstract fun createIssue(lineNumber: Int)
}
