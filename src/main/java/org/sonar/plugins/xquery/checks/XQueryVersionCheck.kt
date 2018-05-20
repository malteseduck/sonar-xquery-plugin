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
import org.sonar.plugins.xquery.parser.unquotedText
import org.sonar.plugins.xquery.rules.CheckClasses

/**
 * Checks declaration of the XQuery version.
 *
 * @since 1.0
 */
@Rule(key = XQueryVersionCheck.RULE_KEY, name = "XQuery Version", description = "Ensure that you declare the latest XQuery version (1.0-ml/3.0) " +
        "at the top of each of your scripts " +
        "(as opposed to declaring an older version - 0.9-ml - or not declaring a version at all). " +
        "This ensures better compatibility of code after server upgrades " +
        "and consistent behavior in XQuery processing.", priority = Priority.MINOR)
class XQueryVersionCheck : AbstractCheck() {

    private var hasVersion = false

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        // Instead of checking when we enter the source we check after the
        // modules so we can handle multi-transaction modules - reset the flags
        // for a new module transaction
        if (node is ModuleTransactionContext) {
            hasVersion = false
            line = node.getLine()
        } else if (node is VersionDeclContext) {
            // If there is a version then set the flag and check to see if
            // it is the "correct" newer version
            hasVersion = true
            if ("0.9-ml" == node.version.unquotedText()) {
                createIssue(RULE, node.getLine())
            }
        }
    }

    override fun exitExpression(node: ParserRuleContext) {
        super.exitExpression(node)
        // Instead of checking when we enter the source we check after the
        // modules so we can handle multi-transaction modules - create a
        // violation if there is no version set
        if (node is ModuleTransactionContext) {
            if (!hasVersion) {
                createIssue(RULE, line)
            }
        }
    }

    companion object {
        const val RULE_KEY = "XQueryVersion"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}