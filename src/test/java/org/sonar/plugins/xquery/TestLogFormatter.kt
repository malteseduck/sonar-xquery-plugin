/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery

import java.util.logging.Formatter
import java.util.logging.LogRecord

class TestLogFormatter : Formatter() {

    override fun format(record: LogRecord): String {
        val output = StringBuilder()
            .append(record.message).append(' ')
            .append(lineSep)
        return output.toString()
    }

    companion object {

        private val lineSep = System.getProperty("line.separator")
    }

}