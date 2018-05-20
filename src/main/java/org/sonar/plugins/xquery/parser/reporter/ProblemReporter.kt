/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.reporter

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.util.*

class ProblemReporter(var isFailOnError: Boolean = false) : BaseErrorListener() {
    var isOutputError: Boolean = false

    val problems: MutableList<Problem>

    init {
        this.isOutputError = true
        problems = ArrayList()
    }

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException?) {
        // Bug with EOF?  Even when adding it to the grammar implicitly it still doesn't match right
        if (!msg.startsWith("mismatched input '<EOF>'")) {
            val problem = Problem(line, charPositionInLine, msg)
            problems.add(problem)
            if (isFailOnError) {
                throw ParseCancellationException(problem.getMessageString())
            } else if (isOutputError) {
                System.err.println(problem.getMessageString())
            }
        }
    }
}
