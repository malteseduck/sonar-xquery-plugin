/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery

import org.sonar.api.batch.Sensor
import org.sonar.api.batch.SensorContext
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.rule.Checks
import org.sonar.api.checks.AnnotationCheckFactory
import org.sonar.api.component.ResourcePerspectives
import org.sonar.api.issue.Issuable
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.resources.Project
import org.sonar.plugins.xquery.api.XQueryConstants
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.language.XQueryLineCountParser
import org.sonar.plugins.xquery.language.XQuerySourceCode
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor
import org.sonar.plugins.xquery.rules.CheckClasses
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

class XQuerySensor(profile: RulesProfile, private val perspectives: ResourcePerspectives, private val fileSystem: FileSystem) : Sensor {
    private val annotationCheckFactory: AnnotationCheckFactory = AnnotationCheckFactory.create(profile, CheckClasses.REPOSITORY_KEY, CheckClasses.checks)
    private var project: Project? = null

    private val projectMainFiles: Iterable<File>
        get() = fileSystem.files(fileSystem.predicates().hasLanguage(XQueryConstants.XQUERY_LANGUAGE_KEY))

    internal fun getSonarResource(file: java.io.File): org.sonar.api.resources.File {
        return org.sonar.api.resources.File.fromIOFile(file, project!!)!!
    }

    override fun analyse(project: Project, context: SensorContext) {
        this.project = project

        val checks: Collection<XQueryAstVisitor> = annotationCheckFactory.checks as Collection<XQueryAstVisitor>
        val visitors: MutableList<XQueryAstVisitor> = mutableListOf(*checks.toTypedArray())

        // Create a mapper and add it to the visitors so that it can keep track
        // of global declarations and the local declaration stack
        val mapper = DependencyMapper()
        visitors.add(mapper)

        // Do the first pass to map all the global dependencies
        logger.info("Scanning all files to map dependencies")
        for (inputFile in projectMainFiles) {

            try {
                val sonarFile = getSonarResource(inputFile)
                val sourceCode = XQuerySourceCode(sonarFile, inputFile)
                logger.fine("Mapping " + sonarFile.longName)

                val parser = XQueryAstParser(sourceCode, listOf(mapper))
                parser.mapDependencies(parser.parse(), mapper)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Could not map the dependencies in the file " + inputFile.absolutePath, e)
            }

        }

        // Now that the global mappings are done we can change the mode to "local"
        mapper.mode = "local"

        // Do the second pass to process the checks and other metrics
        logger.info("Scanning all files and gathering metrics")
        for (inputFile in projectMainFiles) {

            try {
                val sonarFile = getSonarResource(inputFile)
                val sourceCode = XQuerySourceCode(sonarFile, inputFile)
                logger.fine("Analyzing " + sonarFile.longName)

                val parser = XQueryAstParser(sourceCode, visitors)
                val reporter = ProblemReporter()
                parser.process(parser.parse(reporter), mapper, reporter)

                // Count the lines of code
                XQueryLineCountParser(sourceCode).count()

                // Save all the collected metrics
                saveMetrics(sonarFile, context, sourceCode)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Could not analyze the file " + inputFile.absolutePath, e)
            }

        }
    }

    private fun saveMetrics(sonarFile: org.sonar.api.resources.File, context: SensorContext, sourceCode: SourceCode) {
        for (issue in sourceCode.issues) {
            logger.finer("Saving issue: $issue")
            val issuable = perspectives.`as`(Issuable::class.java, sonarFile)
            if (issuable != null) {
                val sIssue = issuable.newIssueBuilder()
                    .ruleKey(issue.rule())
                    .line(issue.line())
                    .message(issue.message())
                    .build()
                issuable.addIssue(sIssue)
            }
        }
        for (measure in sourceCode.measures) {
            logger.finer("Saving measure: $measure")
            context.saveMeasure(sourceCode.resource, measure)
        }
        for (dependency in sourceCode.dependencies) {
            logger.finer("Saving dependency: $dependency")
            context.saveDependency(dependency)
        }
    }

    override fun shouldExecuteOnProject(project: Project): Boolean =
        fileSystem.files(fileSystem.predicates().hasLanguage(XQueryConstants.XQUERY_LANGUAGE_KEY)).iterator().hasNext()

    companion object {
        private val logger = Logger.getLogger(XQuerySensor::class.java.name)
    }
}
