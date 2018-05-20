/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.test

import org.sonar.api.batch.Sensor
import org.sonar.api.batch.SensorContext
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.config.Settings
import org.sonar.api.resources.Project
import org.sonar.api.utils.log.Loggers
import org.sonar.plugins.xquery.api.XQueryConstants
import java.io.File

class XQueryTestSensor(private val surefireXQueryParser: SurefireXQueryParser, private val settings: Settings, private val fileSystem: FileSystem) : Sensor {

    override fun shouldExecuteOnProject(project: Project): Boolean {
        return fileSystem.files(fileSystem.predicates().hasLanguage(XQueryConstants.XQUERY_LANGUAGE_KEY)).iterator().hasNext()
    }

    override fun analyse(project: Project, context: SensorContext) {
        val dir = File(settings.getString(XQueryConstants.XQTEST_REPORTS_DIRECTORY_KEY))
        collect(project, context, dir)
    }

    protected fun collect(project: Project, context: SensorContext, reportsDir: File) {
        LOGGER.info("parsing {}", reportsDir)
        surefireXQueryParser.collect(project, context, reportsDir)
    }

    override fun toString(): String {
        return javaClass.simpleName
    }

    companion object {

        private val LOGGER = Loggers.get(XQueryTestSensor::class.java)
    }
}
