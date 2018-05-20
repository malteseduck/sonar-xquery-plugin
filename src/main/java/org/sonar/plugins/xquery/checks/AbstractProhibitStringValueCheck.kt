/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.parser.XQueryParser
import org.sonar.plugins.xquery.parser.getLine
import java.util.regex.Pattern

abstract class AbstractProhibitStringValueCheck : AbstractCheck() {
    protected abstract val pattern: String

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)

        if (node is XQueryParser.StringLiteralContext) {
            val value = node.text
            if (Pattern.matches(pattern, value)) {
                createIssue(node.getLine())
            }
        }
    }

    protected abstract fun createIssue(lineNumber: Int)
}
