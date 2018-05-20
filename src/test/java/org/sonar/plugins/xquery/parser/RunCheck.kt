/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser

import org.antlr.runtime.RecognitionException
import org.antlr.v4.runtime.ParserRuleContext
import org.codehaus.plexus.util.FileUtils
import org.sonar.api.rules.AnnotationRuleParser
import org.sonar.api.rules.Rule
import org.sonar.plugins.xquery.checks.AbstractCheck
import org.sonar.plugins.xquery.checks.DynamicFunctionCheck
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.language.XQuerySourceCode
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor
import org.testng.Assert

import java.io.IOException
import java.util.Arrays

/**
 * Created with IntelliJ IDEA.
 * User: cieslinskice
 * Date: 1/10/13
 * Time: 3:47 PM
 */
object RunCheck {

    val PATH = "/Users/cieslinskice/Documents/Code/lds-edit/src/main/xquery/invoke/function-apply.xqy"

    val reporter: ProblemReporter
        get() {
            val defaultReporter = ProblemReporter()
            defaultReporter.isFailOnError = true
            return defaultReporter
        }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val check = DynamicFunctionCheck()
        val mapper = DependencyMapper()
        val code = XQuerySourceCode(FileUtils.fileRead(PATH))
        val parser = XQueryAstParser(code, Arrays.asList(*arrayOf(mapper, check)))

        try {
            // Create the AST for the code
            val tree = parser.parse(reporter)

            // Map the dependencies first so we have any "global" declarations
            // in the stack
            parser.mapDependencies(tree, mapper)
            mapper.mode = "local"

            // Process the check using the supplied check
            parser.process(tree, mapper, reporter)
        } catch (e: RecognitionException) {
            Assert.fail("Failed parsing source code", e)
        }

    }
}
