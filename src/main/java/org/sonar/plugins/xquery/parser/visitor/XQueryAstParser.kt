/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.visitor

import org.antlr.runtime.RecognitionException
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.language.XQuerySourceCode
import org.sonar.plugins.xquery.parser.MultiChannelTokenStream
import org.sonar.plugins.xquery.parser.XQueryLexer
import org.sonar.plugins.xquery.parser.XQueryParser
import org.sonar.plugins.xquery.parser.asStringTree
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import java.util.logging.Logger

class XQueryAstParser(private val sourceCode: SourceCode, private val visitors: List<XQueryAstVisitor>) {

    @Throws(RecognitionException::class)
    fun parse(reporter: ProblemReporter = ProblemReporter()): ParserRuleContext {
        logger.fine("Parsing $sourceCode:")
        val source = CharStreams.fromString(sourceCode.codeString, sourceCode.toString())
        val lexer = XQueryLexer(source)
        val tokenStream = MultiChannelTokenStream(lexer)
        val parser = XQueryParser(tokenStream)

        parser.removeErrorListeners()
        parser.addErrorListener(reporter)
        val tree = parser.module()

        logger.fine(parser.module().asStringTree())
        return tree
    }

    /**
     * Do a quick pass and map the global declarations for the specified tree
     *
     */
    fun mapDependencies(tree: ParserRuleContext, mapper: DependencyMapper) {
        // Since the mapper doesn't use any of the parameters, just pass in
        // nulls
        mapper.enterSource(XQuerySourceCode(""), tree, mapper)
        visit(tree, listOf(mapper))
        mapper.exitSource(tree)
    }

    fun process(tree: ParserRuleContext, mapper: DependencyMapper, reporter: ProblemReporter) {
        visitors.forEach {
            it.enterSource(sourceCode, tree, mapper)
        }
        visit(tree, visitors)
        visitors.forEach {
            it.exitSource(tree)
            it.checkReport(reporter)
        }
    }

    private fun visit(root: ParserRuleContext, visitors: List<XQueryAstVisitor>) {
        for (visitor in visitors) {
            visitor.enterExpression(root)
        }
        root.children
            ?.filter { it is ParserRuleContext }
            ?.forEach { child -> visit(child as ParserRuleContext, visitors) }
        for (visitor in visitors) {
            visitor.exitExpression(root)
        }
    }

    companion object {

        private val logger = Logger.getLogger(XQueryAstParser::class.java.name)
    }
}
