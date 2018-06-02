/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.sonar.api.measures.CoreMetrics
import org.sonar.api.utils.SonarException
import org.sonar.plugins.xquery.checks.AbstractCheck
import java.util.*
import java.util.logging.Logger

/**
 * Count lines of code in Xquery files.
 *
 * @since 1.0
 */
class XQueryLineCountParser(override var sourceCode: SourceCode?) : AbstractCheck() {

    private var blankLines = 0
    private var commentLines = 0
    private var linesOfCode = 0
    private var functions = 0

    fun count() {
        logger.fine("Count comment in " + sourceCode?.resource?.longName)

        val code = sourceCode?.code as ArrayList<String>

        if (code.size > 0) {
            try {
                var commenting = false
                for (line in code) {
                    val trimmed = line.trim()
                    if (trimmed.isBlank()) {
                        blankLines++
                    } else if (trimmed.startsWith("(:") && trimmed.endsWith(":)")) {
                        commentLines++
                    } else if (trimmed.startsWith("(:") && !trimmed.contains(":)") && !trimmed.endsWith(":)")) {
                        commenting = true
                        commentLines++
                    } else if (commenting && trimmed.contains(":)")) {
                        commenting = false
                        commentLines++
                    } else if (commenting) {
                        commentLines++
                    } else {
                        if (trimmed.contains("declare function")) {
                            functions++
                        }
                    }

                    linesOfCode++
                }
            } catch (e: Exception) {
                throw SonarException(e)
            }

        }

        sourceCode?.addMeasure(CoreMetrics.FUNCTIONS, functions.toDouble())
//        sourceCode?.addMeasure(CoreMetrics.LINES, linesOfCode.toDouble())
        sourceCode?.addMeasure(CoreMetrics.COMMENT_LINES, commentLines.toDouble())
        sourceCode?.addMeasure(CoreMetrics.NCLOC, linesOfCode.toDouble() - blankLines.toDouble() - commentLines.toDouble())
    }

    companion object {
        private val logger = Logger.getLogger(XQueryLineCountParser::class.java.name)
    }
}
