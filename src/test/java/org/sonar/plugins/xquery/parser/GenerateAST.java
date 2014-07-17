/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.debug.ParseTreeBuilder;
import org.antlr.runtime.debug.XQueryParseTreeBuilder;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;

/**
 * Class that can be used to generate an AST for a specified string that can be
 * used to see how it would look.
 * 
 * It can also generate the parse tree as well if the member is changed to
 * "true" and change the parser creation to use the builder. This can only be
 * done if the parser was generated with the "-debug" option
 * 
 * For use in manual testing.
 */
public class GenerateAST {

    public static final boolean INCLUDE_AST = true;
    public static final boolean INCLUDE_PARSE_TREE = false;
//    public static final String PATH = "/Users/cieslinskice/Documents/Code/lds-edit/src/main/xquery/invoke/function-apply.xqy";
    public static final String PATH = "";

    public static String code(String... strings) {
        StringBuffer code = new StringBuffer();
        for (String string : strings) {
            if (code.length() > 0) {
                code.append('\n');
            }
            code.append(string);
        }
        return code.toString();
    }

    public static void main(String[] args) throws IOException {
        XQueryTree tree = new XQueryTree();
        ParseTreeBuilder builder = new XQueryParseTreeBuilder("MainModule");
        try {
            String code =
                code(
                    "xquery version '1.0-ml';",
                    "declare variable $null as element(func:null) := ''",
                    "$null"
                );
            
            ANTLRStringStream source;
            if (StringUtils.isNotBlank(PATH)) {
                source = new ANTLRStringStream(FileUtils.fileRead(PATH));
            } else {
                source = new ANTLRStringStream(code);
                source.name = code.toString();
            }
            XQueryLexer lexer = new XQueryLexer(source);
            TokenStream tokenStream = new LazyTokenStream(lexer);
//             XQueryParser parser = new XQueryParser(tokenStream, builder);
            XQueryParser parser = new XQueryParser(tokenStream);
            parser.setCharSource(source);
            parser.setTreeAdaptor(new XQueryTreeAdaptor());
            tree = (XQueryTree) parser.p_Module().getTree();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (INCLUDE_PARSE_TREE) {
                System.out.print(builder.getTree().toStringTree());
            }
            if (INCLUDE_AST) {
                System.out.print(tree.toStringTree());
            }
        }
    }
}
