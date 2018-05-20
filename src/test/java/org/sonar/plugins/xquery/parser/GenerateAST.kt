/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser

import org.antlr.v4.runtime.*
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
            if (code.length > 0) {
                code.append('\n')
            }
            code.append(string)
        }
        return code.toString()
    }

    private fun printPrettyLispTree(tree: RuleContext, indentation: Int = 1) {

        if (tree is ParserRuleContext) {
            for (i in 0 until indentation) print("  ")
            println(XQueryParser.ruleNames[tree.getRuleIndex()])
            if (tree.children != null)
                tree.children.forEach { child ->
                    if (child is RuleContext)
                        printPrettyLispTree(child, indentation + 1)
                    else {
                        for (i in 0..indentation) print("  ")
                        println(child.text)
                    }

                }
        }
        println()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        //        XQueryTree tree = new XQueryTree();
        //        ParseTreeBuilder builder = new XQueryParseTreeBuilder("RULE_mainModule");
        try {
            val source = code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare function test:add(\$a as xs:integer?, \$b as xs:integer?)",
                "as xs:integer? {",
                "    \$a + \$b",
                "};"
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
            println(tree.asStringTree())

        } catch (e: Exception) {
            e.printStackTrace()
            //        } finally {
            //            if (INCLUDE_PARSE_TREE) {
            //                System.out.print(builder.getTree().toStringTree());
            //            }
            //            if (INCLUDE_AST) {
            //                System.out.print(tree.toStringTree());
            //            }
        }

    }
}
