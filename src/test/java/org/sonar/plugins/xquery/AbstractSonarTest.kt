/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery

import org.antlr.runtime.RecognitionException
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.sonar.plugins.xquery.checks.AbstractCheck
import org.sonar.plugins.xquery.language.Issue
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.language.XQuerySourceCode
import org.sonar.plugins.xquery.parser.XQueryParser
import org.sonar.plugins.xquery.parser.asDebugTree
import org.sonar.plugins.xquery.parser.asStringTree
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor
import org.testng.Assert
import org.testng.TestNG

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.logging.ConsoleHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.Logger

import java.util.Arrays.asList
import java.util.Collections.emptyList

open class AbstractSonarTest {

    var reporter: ProblemReporter? = null
        get() {
            if (field == null) {
                val defaultReporter = ProblemReporter()
                defaultReporter.isFailOnError = true
                return defaultReporter
            }
            return field
        }

    /**
     * Checks to see if tracing is turned on
     *
     * @return "true" if tracing is enabled, "false" if it isn't
     */
    var isTracing = false
        private set

    /**
     * Checks to see if the issues are on the specified line. If the
     * issues are on a different line or there is more than one issue
     * then this fails the test.
     *
     * @param check
     * The check that was processed
     * @param line
     * The line number where the issues should be
     */
    fun assertIssueLine(check: AbstractCheck, line: Int) {
        assertIssueLines(check, intArrayOf(line))
    }

    /**
     * Checks to see if the issues are on the specified line. If the
     * issues are on a different line or there is more than one issue
     * then this fails the test.
     *
     * @param check
     * The check that was processed
     * @param lines
     * The line numbers where the issues should be
     */
    fun assertIssueLines(check: AbstractCheck, lines: IntArray) {
        var index = 0
        Assert.assertNotNull(lines, "Issue lines")
        for (issue in getIssues(check, lines.size)) {
            Assert.assertEquals(issue.line(), lines[index], "Issue " + (index + 1) + " line number")
            index++
        }
    }

    /**
     * Performs the specified check against the specified source code. If there
     * are any problems parsing the code then fails the test.
     *
     * @param check
     * A code check to process
     * @param code
     * A snippet of source code
     */
    protected fun check(check: AbstractCheck, code: SourceCode, mapper: DependencyMapper) {
        // Set up the rule and parser for checking
        val parser = XQueryAstParser(code, Arrays.asList(mapper, check))
        try {
            // Create the AST for the code
            logger.fine(code.codeString)
            val tree = parser.parse(reporter!!)
            logger.fine(tree.asDebugTree())

            // Map the dependencies first so we have any "global" declarations
            // in the stack
            parser.mapDependencies(tree, mapper)
            mapper.mode = "local"

            // Process the check using the supplied check
            parser.process(tree, mapper, reporter!!)
        } catch (e: RecognitionException) {
            Assert.fail("Failed parsing source code", e)
        }

    }

    /**
     * Checks to see if the source code will fail the check. Fails the test if
     * it does not fail. You can specify the number of issues that are
     * expected.
     *
     * @param check
     * A code check to process
     * @param code
     * A snippet of source code
     * @param mapper TODO
     * @param issues
     * The number of issues that are expected
     */
    fun checkInvalid(check: AbstractCheck, code: SourceCode, mapper: DependencyMapper = DependencyMapper(), issues: Int = 1) {
        check(check, code, mapper)
        Assert.assertEquals(code.issues.size, issues, "Invalid - there should have been issues marked on the code")
    }

    fun checkInvalid(check: AbstractCheck, code: SourceCode, issues: Int) {
        checkInvalid(check, code, DependencyMapper(), issues)
    }

    /**
     * Checks to see if the source code will fail the check. Fails the test if
     * it does not fail. You can specify the number of violations that are
     * expected.
     *
     * @param check
     * A code check to process
     * @param code
     * A snippet of source code
     * @param mapper TODO
     * @param mapper TODO
     * The number of violations that are expected
     */
    fun checkValid(check: AbstractCheck, code: SourceCode, mapper: DependencyMapper = DependencyMapper()) {
        check(check, code, mapper)

        if (code.issues.isNotEmpty()) {
            val error = StringBuffer("Code is valid, expected 0 issues but got:")
            for (issue in code.issues) {
                error.append("\n    - Issue on line ").append(issue.line().toString() + ": " + issue.message())
            }
            Assert.fail(error.toString())
        }
    }

    /**
     * Creates a SourceCode object from the list of strings
     *
     * @param strings
     * A list of lines of code
     * @return The SourceCode for the strings
     */
    fun code(vararg strings: String): SourceCode {
        val list = ArrayList<String>()
        for (string in strings) {
            list.add(string)
        }
        return XQuerySourceCode(list)
    }

    /**
     * Returns the issue for the source code in the specified check at the
     * specified index. If no issue exists at that index then the test will
     * fail.
     *
     * @param check
     * The check that was processed
     * @param index
     * The index of the issue for which to look
     * @return The violation
     */
    fun getIssue(check: AbstractCheck, index: Int): Issue {
        val sourceCode = check.sourceCode
        Assert.assertNotNull(sourceCode, "Source code from check")
        val issues = sourceCode!!.issues
        val size = issues.size
        Assert.assertTrue(size > 0, "Number of issues")
        Assert.assertTrue(size > index, "Requested index doesn't exist")
        return issues[index]
    }

    /**
     * Returns the issues for the source code in the specified check at the
     * specified index. If no issues exists then the test will fail.
     *
     * @param check
     * The check that was processed
     * @param count
     * The number of issues that are expected
     * @return A list of all the issues
     */
    fun getIssues(check: AbstractCheck, count: Int): List<Issue> {
        val sourceCode = check.sourceCode
        Assert.assertNotNull(sourceCode, "Source code from check")
        val issues = sourceCode!!.issues
        Assert.assertEquals(issues.size, count, "Number of issues")
        return issues
    }

    protected fun importModule(code: SourceCode, mapper: DependencyMapper = DependencyMapper()): DependencyMapper {
        val parser = XQueryAstParser(code, emptyList())

        try {
            logger.fine(code.codeString)
            val tree = parser.parse(reporter!!)
            logger.fine(tree.asDebugTree())
            parser.mapDependencies(tree, mapper)
        } catch (e: RecognitionException) {
            Assert.fail("Failed parsing 'imported' module source code", e)
        }

        return mapper
    }

    /**
     * Logs the supplied message (at a fine level). For debugging parsing.
     *
     * @param msg
     * The message to output
     */
    fun log(msg: String) {
        logger.fine(msg)
    }

    /**
     * Parses the supplied code to get the AST.
     *
     * @param code
     * The code to parse
     * @return An AST for the code if it was successfully parsed
     * @throws RecognitionException
     */
    @Throws(ParseCancellationException::class)
    fun parse(code: SourceCode): ParserRuleContext {
        val visitors = ArrayList<XQueryAstVisitor>()
        val parser = XQueryAstParser(code, visitors)
        logger.fine(code.codeString)
        val tree = parser.parse(reporter!!)
        logger.fine(tree.asDebugTree())
        return tree
    }

    /**
     * Enables (or disabled) tracing for a test or set of tests. This function
     * can be placed in any test or before all tests are run and can be used to
     * see the AST output of parsing.
     *
     * @param enable
     * Whether or not to enable tracing - "true" or "false"
     */
    fun trace(enable: Boolean = true) {
        if (enable) {
            reporter = ProblemReporter(false)
            isTracing = true
            val handler = ConsoleHandler()
            handler.formatter = TestLogFormatter()
            handler.level = Level.ALL
            logger.addHandler(handler)
            logger.level = Level.ALL
        } else {
            reporter = null
            isTracing = false
            for (handler in logger.handlers) {
                logger.removeHandler(handler)
            }
            logger.level = Level.SEVERE
        }
    }

    companion object {
        private val logger = Logger.getLogger(AbstractSonarTest::class.java.name)
        fun profileTest(cls: Class<*>) {
            val testng = TestNG()
            testng.setTestClasses(arrayOf(cls))
            val handler = ConsoleHandler()
            handler.level = Level.ALL
            logger.addHandler(handler)
            logger.level = Level.ALL
            testng.run()
        }
    }
}