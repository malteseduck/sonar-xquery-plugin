/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.visitor

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter

interface XQueryAstVisitor {
    fun enterSource(code: SourceCode, node: ParserRuleContext, mapper: DependencyMapper)

    fun exitSource(node: ParserRuleContext)

    fun enterExpression(node: ParserRuleContext)

    fun exitExpression(node: ParserRuleContext)

    fun checkReport(reporter: ProblemReporter)
}