/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.test

import org.sonar.api.BatchExtension
import org.sonar.api.config.Settings
import org.sonar.api.resources.Resource
import org.sonar.api.utils.log.Loggers
import org.sonar.plugins.surefire.api.AbstractSurefireParser
import org.sonar.plugins.xquery.api.XQueryConstants

class SurefireXQueryParser(private val settings: Settings) : AbstractSurefireParser(), BatchExtension {

    override fun getUnitTestResource(classKey: String): Resource? {
        return org.sonar.api.resources.File.create(settings.getString(XQueryConstants.SOURCE_DIRECTORY_KEY) + classKey)
    }

    companion object {

        private val LOGGER = Loggers.get(SurefireXQueryParser::class.java)
    }
}