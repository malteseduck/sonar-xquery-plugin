/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.apache.commons.lang.StringUtils.substringAfter
import org.apache.commons.lang.StringUtils.substringBefore
import org.codehaus.plexus.util.FileUtils
import org.sonar.plugins.xquery.AbstractSonarTest
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.testng.annotations.Test
import java.io.File
import java.io.IOException

/**
 * Class that can be used to test many external files to see how the parser
 * handles different types of query files. A good "full" test is the XQuery Test
 * Suite at http://dev.w3.org/2006/xquery-test-suite/PublicPagesStagingArea/.
 * Download the latest test, extract it, then point the parser to the directory
 * with that contains the queries (and change the file filter to "xq" since that
 * is their file extension).
 */
class TestParser: AbstractSonarTest() {

    var CODE_FILTER = "**/*.xq"
    var SEP = File.separatorChar
    var CODE_ROOT = "QT3_1_0"

    @Test
    @Throws(IOException::class)
    fun `Should test all XQuery files in the 3_1 specification tests`() {
        var tree: ParserRuleContext? = null
        val classLoader = Thread.currentThread().contextClassLoader
        val directory = File(classLoader.getResource(CODE_ROOT)!!.file)

        println("Parsing files in $CODE_ROOT")
        val files: MutableList<File> = FileUtils.getFiles(directory, CODE_FILTER, "")

        val treeDirectory = System.getProperty("user.dir") + SEP + "target" + SEP + "parse-trees"
        FileUtils.mkdir(treeDirectory)
        FileUtils.cleanDirectory(treeDirectory)

        files.forEach { file ->
            println("Analyzing " + file.getPath() + ":")
            try {
                val source = CharStreams.fromFileName(file.absolutePath)
                val lexer = XQueryLexer(source)
                val tokenStream = CommonTokenStream(lexer)
                val parser = XQueryParser(tokenStream)
                tree = parser.module()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val fileName = substringBefore(file.getName(), ".")
                val outputDirectory = substringBefore(substringAfter(file.path, CODE_ROOT), fileName)
                FileUtils.mkdir(treeDirectory + outputDirectory)
                val parseName = "$treeDirectory$outputDirectory$SEP$fileName-parsetree.txt"
                val treeName = "$treeDirectory$outputDirectory$SEP$fileName-AST.txt"
                println("Writing AST to $parseName")
                if (tree != null) FileUtils.fileWrite(treeName, tree!!.asStringTree())
            }
        }

        println("File parsing complete")
    }
}
