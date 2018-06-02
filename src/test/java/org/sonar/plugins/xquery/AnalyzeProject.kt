/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery

import org.codehaus.plexus.util.FileUtils
import org.sonar.api.rules.AnnotationRuleParser
import org.sonar.api.rules.Rule
import org.sonar.api.utils.AnnotationUtils.getAnnotation
import org.sonar.api.utils.SonarException
import org.sonar.plugins.xquery.checks.AbstractCheck
import org.sonar.plugins.xquery.language.XQuerySourceCode
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor
import org.sonar.plugins.xquery.rules.CheckClasses
import org.sonar.plugins.xquery.rules.XQueryRulesRepository

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.regex.Pattern

object AnalyzeProject {

    var CODE_ROOT = "C:\\Users\\Chris\\Documents\\Projects\\WealthCounsel\\wealth-counsel-online"
    var BASE_DIR = File(CODE_ROOT)
    var SEP = File.separatorChar

    // Comma-delimited (no spaces) list of includes/excludes for the different phases
    var MAPPING_INCLUDES = "**/*.xqy"
    var MAPPING_EXCLUDES = "**/target/**,test/**,**/scripts/**"
    var PROCESS_INCLUDES = "**/default.xqy"
    var PROCESS_EXCLUDES = "**/target/**,**/shared/**,test/**,**/scripts/**"

    // Regular expression of list of rules to evaluate (.* for all)
    var RULE_INCLUDES = ".*"
    var SOURCE_DIRS = Arrays.asList(File("$CODE_ROOT/src/main/xquery"))

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val start = Date().time
        val directory = File(CODE_ROOT)
        println("Analyzing files in $CODE_ROOT")
        val visitors = ArrayList<XQueryAstVisitor>()

        // Create a mapper and add it to the visitors so that it can keep track
        // of global declarations and the local declaration stack
        val mapper = DependencyMapper()
        visitors.add(mapper)

        // Set up output directories
        val treeDirectory = System.getProperty("user.dir") + SEP + "target" + SEP + "rule-analysis"
        val outputFile = "$treeDirectory/output.txt"
        FileUtils.mkdir(treeDirectory)
        FileUtils.cleanDirectory(treeDirectory)

        // Add all the rules to check
        val rules = XQueryRulesRepository(AnnotationRuleParser()).createRules()
        for (rule in rules) {
            val key = rule.key

            val p = Pattern.compile(RULE_INCLUDES)
            val m = p.matcher(key)
            if (m.matches()) {
                val checkClass = getCheckClass(rule)
                if (checkClass != null) {
                    visitors.add(createCheck(checkClass, rule))
                }
            }
        }

        // Do the first pass to map all the global dependencies
        println("Scanning all files to map dependencies")
        for (file in FileUtils.getFiles(directory, MAPPING_INCLUDES, MAPPING_EXCLUDES) as List<File>) {
            if (file.exists()) {
                try {
                    val sourceCode = XQuerySourceCode(org.sonar.api.resources.File.create(file.absolutePath), file)
                    println("----- Mapping " + file.absolutePath + " -----")
                    FileUtils.fileAppend(outputFile, "\n----- Mapping " + file.absolutePath + " -----")

                    val parser = XQueryAstParser(sourceCode, Arrays.asList(*arrayOf<XQueryAstVisitor>(mapper)))
                    val tree = parser.parse()
                    parser.mapDependencies(tree, mapper)
                } catch (e: Exception) {
                    println("Could not map the dependencies in the file " + file.absolutePath)
                    FileUtils.fileAppend(outputFile, "\nCould not map the dependencies in the file " + file.absolutePath)
                    e.printStackTrace()
                }

            }
        }

        var now = Date().time
        println("Mapping finished in " + (now - start) + " ms")
        FileUtils.fileAppend(outputFile, "\nMapping finished in " + (now - start) + " ms")

        // Now that the global mappings are done we can change the mode to "local"
        mapper.mode = "local"

        // Do the second pass to process the checks and other metrics
        println("Scanning all files and gathering metrics")
        for (file in FileUtils.getFiles(directory, PROCESS_INCLUDES, PROCESS_EXCLUDES) as List<File>) {
            if (file.exists()) {
                try {
                    val sourceCode = XQuerySourceCode(org.sonar.api.resources.File.create(file.absolutePath), file)
                    println("----- Analyzing " + file.absolutePath + " -----")
                    FileUtils.fileAppend(outputFile, "\n----- Analyzing " + file.absolutePath + " -----")

                    val reporter = ProblemReporter()
                    val parser = XQueryAstParser(sourceCode, visitors)
                    val tree = parser.parse(reporter)
                    parser.process(tree, mapper, reporter)

                    // Output the violations
                    for (issue in sourceCode.issues) {
                        println("      - Violation on line " + issue.line() + ": " + issue.rule())
                        FileUtils.fileAppend(outputFile, "\n      - Violation on line " + issue.line() + ": " + issue.rule())
                    }
                } catch (e: Exception) {
                    println("Could not analyze the file " + file.absolutePath)
                    FileUtils.fileAppend(outputFile, "\nCould not analyze the file " + file.absolutePath)
                    e.printStackTrace()
                }

            }
        }

        now = Date().time
        println("File analysis complete in " + (now - start) + " ms")
        FileUtils.fileAppend(outputFile, "\nFile analysis complete in " + (now - start) + " ms")
    }

    private fun getCheckClass(rule: Rule): Class<AbstractCheck>? {
        for (checkClass in CheckClasses.checks) {

            val ruleAnnotation: org.sonar.check.Rule = getAnnotation(checkClass, org.sonar.check.Rule::class.java)
            if (ruleAnnotation.key == rule.configKey) {
                return checkClass as Class<AbstractCheck>
            }
        }
        println("Could not find check class for config key " + rule.configKey)
        return null
    }

    private fun createCheck(checkClass: Class<AbstractCheck>, rule: Rule): AbstractCheck {

        try {
            return checkClass.newInstance()
        } catch (e: IllegalAccessException) {
            throw SonarException(e)
        } catch (e: InstantiationException) {
            throw SonarException(e)
        }

    }

}
