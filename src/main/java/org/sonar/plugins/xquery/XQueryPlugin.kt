/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery

import org.sonar.api.Properties
import org.sonar.api.Property
import org.sonar.api.SonarPlugin
import org.sonar.plugins.xquery.api.XQueryConstants
import org.sonar.plugins.xquery.language.XQuery
import org.sonar.plugins.xquery.language.XQueryCodeColorizerFormat
import org.sonar.plugins.xquery.language.XQuerySourceImporter
import org.sonar.plugins.xquery.rules.XQueryProfile
import org.sonar.plugins.xquery.rules.XQueryRulesDefinition
import org.sonar.plugins.xquery.rules.XQueryRulesRepository
import org.sonar.plugins.xquery.test.SurefireXQueryParser
import org.sonar.plugins.xquery.test.XQueryTestSensor

@Properties(
    Property(
        key = XQueryConstants.FILE_EXTENSIONS_KEY,
        name = "File extensions",
        description = "List of file extensions that will be scanned.",
        defaultValue = XQueryConstants.DEFAULT_FILE_EXTENSIONS_STRING,
        global = true,
        project = true
    ),
    Property(
        key = XQueryConstants.SOURCE_DIRECTORY_KEY,
        name = "Source directory",
        description = "Source directory that will be scanned.",
        defaultValue = XQueryConstants.DEFAULT_SOURCE_DIRECTORY,
        global = false,
        project = true
    ),
    Property(
        key = XQueryConstants.XQTEST_REPORTS_DIRECTORY_KEY,
        name = "Reports path",
        description = "Path (absolute or relative) to XML report files.",
        defaultValue = XQueryConstants.DEFAULT_XQTEST_DIRECTORY,
        project = true,
        global = false
    )
)
class XQueryPlugin : SonarPlugin() {

    override fun getExtensions(): List<*> =
        listOf(
            // Core classes
            XQuery::class.java,
            XQuerySourceImporter::class.java,
            XQueryCodeColorizerFormat::class.java,

            // XQuery rules/profiles
            XQueryProfile::class.java,
            XQueryRulesDefinition::class.java,

            // XQuery sensors
            XQuerySensor::class.java,
            XQueryTestSensor::class.java,

            // XQuery Test parser
            SurefireXQueryParser::class.java
        )

    override fun toString(): String {
        return javaClass.simpleName
    }
}
