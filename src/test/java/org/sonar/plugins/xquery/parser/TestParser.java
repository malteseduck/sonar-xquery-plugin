/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.debug.ParseTreeBuilder;
import org.antlr.runtime.debug.XQueryParseTreeBuilder;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * Class that can be used to test many external files to see how the parser
 * handles different types of query files. A good "full" test is the XQuery Test
 * Suite at http://dev.w3.org/2006/xquery-test-suite/PublicPagesStagingArea/.
 * Download the latest test, extract it, then point the parser to the directory
 * with that contains the queries (and change the file filter to "xq" since that
 * is their file extension).
 * 
 * For use in manual testing.
 */
public class TestParser {

    public static String CODE_ROOT = "/Users/cieslinskice/Documents/XQTS_1_0_3";
    public static String CODE_FILTER = "**/*.xq";

    public static void main(String[] args) throws IOException {
        XQueryTree tree = new XQueryTree();
        ParseTreeBuilder builder = new XQueryParseTreeBuilder("MainModule");
        File directory = new File(CODE_ROOT);

        System.out.println("Parsing files in " + CODE_ROOT);
        List<File> files = FileUtils.getFiles(directory, CODE_FILTER, "");

        String treeDirectory = System.getProperty("java.io.tmpdir") + "/parse-trees";
        FileUtils.mkdir(treeDirectory);
        FileUtils.cleanDirectory(treeDirectory);

        // files = Arrays.asList(new File[]{ new
        // File("C:\\Users\\cieslinskice\\Documents\\Code\\xqyShared\\common\\src\\main\\xquery\\http\\http.xqy")});
        for (File file : files) {
            System.out.println("Analyzing " + file.getPath() + ":");
            try {
                ANTLRStringStream source = new ANTLRFileStream(file.getAbsolutePath());
                source.name = file.getPath();
                XQueryLexer lexer = new XQueryLexer(source);
                TokenStream tokenStream = new LazyTokenStream(lexer);
                // XQueryParser parser = new XQueryParser(tokenStream, builder);
                XQueryParser parser = new XQueryParser(tokenStream);
                parser.setCharSource(source);
                parser.setTreeAdaptor(new XQueryTreeAdaptor());
                tree = (XQueryTree) parser.p_Module().getTree();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                String outputDirectory = StringUtils.substringAfter(file.getPath(), CODE_ROOT);
                FileUtils.mkdir(treeDirectory + outputDirectory);
                String parseName = treeDirectory + outputDirectory + "/" + StringUtils.substringBefore(file.getName(), ".") + "-parsetree.txt";
                String treeName = treeDirectory + outputDirectory + "/" + StringUtils.substringBefore(file.getName(), ".") + "-AST.txt";
//                System.out.println("Writing parse tree to " + parseName);
//                FileUtils.fileWrite(parseName, builder.getTree().toStringTree());
                System.out.println("Writing AST to " + parseName);
                FileUtils.fileWrite(treeName, tree.toStringTree());
            }
        }

        System.out.println("File parsing complete");
    }
}
