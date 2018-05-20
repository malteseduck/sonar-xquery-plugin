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
 * Checks for function mapping usage
 *
 * @since 1.0
 */
@Rule(key = FunctionMappingCheck.RULE_KEY, name = "Function Mapping Usage (Marklogic)", description = "Make sure you are intentionally using and/or understand function mapping " +
        "- otherwise disable it with 'declare option xdmp:mapping \"false\";'. " +
        "If you wish to use it you should explicitly declare 'declare option xdmp:mapping \"true\";' " +
        "for readability/maintainability.\n" +
        "Please note that this check is Marklogic specific.", priority = Priority.MAJOR)
class FunctionMappingCheck : AbstractCheck() {

    private var capable = false
    private var used = false

    override fun enterExpression(node: ParserRuleContext) {
        super.enterExpression(node)
        // Instead of checking when we enter the source we check after the
        // modules so we can handle multi-transaction modules - reset the flags
        // for a new module transaction
        when (node) {
            is ModuleTransactionContext -> {
                capable = false
                used = false
                line = node.getLine()

            }
            is VersionDeclContext -> // Check the version value to see if the script is capable of using
                // function mapping
                if ("1.0-ml" == node.version.unquotedText()) {
                    capable = true
                }
            is OptionDeclContext -> // If the option is set then set the flag
                if ("xdmp:mapping" == node.name.text) {
                    used = true
                }
        }
    }

    override fun exitExpression(node: ParserRuleContext) {
        super.exitExpression(node)
        // Instead of checking when we leave the source we check after the
        // modules so we can handle multi-transaction modules - create a
        // violation if the flags are not correct
        if (node is ModuleTransactionContext) {
            if (capable && !used) {
                createIssue(RULE, line)
            }
        }
    }

    companion object {
        const val RULE_KEY = "FunctionMapping"
        private val RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY)
    }
}