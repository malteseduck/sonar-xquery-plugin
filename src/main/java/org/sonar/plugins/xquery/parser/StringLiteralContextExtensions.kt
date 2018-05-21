package org.sonar.plugins.xquery.parser

import org.sonar.plugins.xquery.parser.XQueryParser.StringLiteralContext

fun StringLiteralContext.unquotedText(): String =
    children
        .drop(1)
        .dropLast(1)
        .joinToString(separator = "") { it.text }