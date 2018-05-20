/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.api.rule.RuleKey
import org.sonar.plugins.xquery.language.Issue
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor

abstract class AbstractCheck : XQueryAstVisitor {

    open var sourceCode: SourceCode? = null

    var mapper: DependencyMapper? = null
    var line = 1
        set(line) = if (line < 1) {
            field = 1
        } else {
            field = line
        }

    override fun checkReport(reporter: ProblemReporter) {
        // By default do nothing
    }

    @JvmOverloads
    protected fun createIssue(rule: RuleKey, lineNumber: Int, message: String = rule.rule()) {
        if (getIssue(rule, lineNumber) == null) {
            sourceCode!!.addIssue(Issue(rule, lineNumber, message))
        }
    }

    override fun enterExpression(node: ParserRuleContext) {
        // Do nothing by default
    }

    override fun enterSource(code: SourceCode, node: ParserRuleContext, mapper: DependencyMapper) {
        this.mapper = mapper
        this.sourceCode = code
        enterSource(node)
    }

    open fun enterSource(node: ParserRuleContext) {
        // Default implementation doesn't check anything
    }

    override fun exitExpression(node: ParserRuleContext) {
        // Do nothing by default
    }

    override fun exitSource(node: ParserRuleContext) {
        // Do nothing by default
    }

    fun getIssue(rule: RuleKey, lineNumber: Int): Issue? {
        for (issue in sourceCode!!.issues) {
            if (issue.rule() === rule && issue.line() == lineNumber) {
                return issue
            }
        }
        return null
    }
}
