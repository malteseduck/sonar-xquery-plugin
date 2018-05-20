/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.reporter

data class Problem(val line: Int = 0, val charPositionInLine: Int = 0, val message: String = "") {
    fun getMessageString(): String = " - line $line: $charPositionInLine - $message"
}
