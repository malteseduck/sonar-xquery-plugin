/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser

import org.antlr.v4.runtime.*
import org.sonar.plugins.xquery.parser.XQueryParser.DirElemContentContext
import org.sonar.plugins.xquery.parser.XQueryParser.ModuleContext

import java.io.IOException

import java.util.Arrays.asList

/**
 * Class that can be used to generate an AST for a specified string that can be
 * used to see how it would look.
 *
 *
 * It can also generate the parse tree as well if the member is changed to
 * "true" and change the parser creation to use the builder. This can only be
 * done if the parser was generated with the "-debug" option
 *
 *
 * For use in manual testing.
 */
object GenerateAST {

    val INCLUDE_AST = true
    val INCLUDE_PARSE_TREE = false
    //    public static final String PATH = "/Users/cieslinskice/Documents/Code/lds-edit/src/main/xquery/invoke/function-apply.xqy";
    val PATH = ""

    fun code(vararg strings: String): String {
        val code = StringBuffer()
        for (string in strings) {
            if (code.isNotEmpty()) {
                code.append('\n')
            }
            code.append(string)
        }
        return code.toString()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val source = code(
                """map:entry("id", '{{ quicklinks.id }}')"""
            )

            val lexer = XQueryLexer(CharStreams.fromString(source))
            val tokens = MultiChannelTokenStream(lexer)
            tokens.fill()
            println("\n[TOKENS]")
            for (t in tokens.tokens) {
                val symbolicName = lexer.vocabulary.getSymbolicName(t.type)
                val literalName = lexer.vocabulary.getLiteralName(t.type)
                System.out.printf("  %-20s '%s'\n",
                    symbolicName ?: literalName,
                    t.text.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t"))
            }
            println("\n[PARSE-TREE]")
            val parser = XQueryParser(tokens)
            val tree = parser.module()
            println(tree.asDebugTree())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
