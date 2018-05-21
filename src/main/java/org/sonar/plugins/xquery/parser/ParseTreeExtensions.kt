package org.sonar.plugins.xquery.parser

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

fun ParseTree.getLine(): Int = when(this) {
    is ParserRuleContext -> start.line
    else -> -1
}