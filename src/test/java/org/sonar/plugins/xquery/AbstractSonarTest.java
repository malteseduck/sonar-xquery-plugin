/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.RecognitionException;
import org.sonar.plugins.xquery.checks.AbstractCheck;
import org.sonar.plugins.xquery.language.Issue;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.language.XQuerySourceCode;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;
import org.testng.Assert;
import org.testng.TestNG;

public class AbstractSonarTest {

    private static final Logger logger = Logger.getLogger(AbstractSonarTest.class.getName());
    public static void profileTest(Class<?> cls) {
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { cls });
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        testng.run();
    }

    private ProblemReporter reporter;

    public ProblemReporter getReporter() {
        if (reporter == null) {
            ProblemReporter defaultReporter = new ProblemReporter();
            defaultReporter.setFailOnError(true);
            return defaultReporter;
        }
        return reporter;
    }

    public void setReporter(ProblemReporter reporter) {
        this.reporter = reporter;
    }

    private boolean tracing = false;

    /**
     * Checks to see if the issues are on the specified line. If the
     * issues are on a different line or there is more than one issue
     * then this fails the test.
     * 
     * @param check
     *            The check that was processed
     * @param line
     *            The line number where the issues should be
     */
    public void assertIssueLine(AbstractCheck check, int line) {
        assertIssueLines(check, new int[]{line});
    }

    /**
     * Checks to see if the issues are on the specified line. If the
     * issues are on a different line or there is more than one issue
     * then this fails the test.
     * 
     * @param check
     *            The check that was processed
     * @param lines
     *            The line numbers where the issues should be
     */
    public void assertIssueLines(AbstractCheck check, int[] lines) {
        int index = 0;
        Assert.assertNotNull(lines, "Issue lines");
        for (Issue issue : getIssues(check, lines.length)) {
            Assert.assertEquals(issue.line(), lines[index], "Issue " + (index + 1) + " line number");
            index++;
        }
    }

        /**
        * Performs the specified check against the specified source code. If there
        * are any problems parsing the code then fails the test.
        * 
        * @param check
        *            A code check to process
        * @param code
        *            A snippet of source code
        */
   protected void check(AbstractCheck check, SourceCode code, DependencyMapper mapper) {
        // Set up the rule and parser for checking
        XQueryAstParser parser = new XQueryAstParser(code, Arrays.asList(new XQueryAstVisitor[] { mapper, check }));

        try {
            // Create the AST for the code
            logger.fine(code.getCodeString());
            XQueryTree tree = parser.parse(getReporter());
            logger.fine(tree.toStringTree());

            // Map the dependencies first so we have any "global" declarations
            // in the stack
            parser.mapDependencies(tree, mapper);
            mapper.setMode("local");

            // Process the check using the supplied check
            parser.process(tree, mapper, getReporter());
        } catch (RecognitionException e) {
            Assert.fail("Failed parsing source code", e);
        }        
   }

    public void checkInvalid(AbstractCheck check, SourceCode code) {
        checkInvalid(check, code, new DependencyMapper(), 1);
    }

    
    /**
     * Checks to see if the source code will fail the check. Fails the test if
     * it does not fail. Assumes there will be only 1 check issue.
     * 
     * @param check
     *            A code check to process
     * @param code
     *            A snippet of source code
     * @param mapper TODO
     */
    public void checkInvalid(AbstractCheck check, SourceCode code, DependencyMapper mapper) {
        checkInvalid(check, code, mapper, 1);
    }
    
    /**
     * Checks to see if the source code will fail the check. Fails the test if
     * it does not fail. You can specify the number of issues that are
     * expected.
     * 
     * @param check
     *            A code check to process
     * @param code
     *            A snippet of source code
     * @param mapper TODO
     * @param issues
     *            The number of issues that are expected
     */
    public void checkInvalid(AbstractCheck check, SourceCode code, DependencyMapper mapper, int issues) {
        check(check, code, mapper);
        Assert.assertEquals(code.getIssues().size(), issues, "Invalid - there should have been issues marked on the code");
    }

    
    public void checkInvalid(AbstractCheck check, SourceCode code, int issues) {
        checkInvalid(check, code, new DependencyMapper(), issues);
    }

    public void checkValid(AbstractCheck check, SourceCode code) {
        checkValid(check, code, new DependencyMapper());
    }
    
    
    /**
     * Checks to see if the source code will fail the check. Fails the test if
     * it does not fail. You can specify the number of violations that are
     * expected.
     * 
     * @param check
     *            A code check to process
     * @param code
     *            A snippet of source code
     * @param mapper TODO
     * @param mapper TODO
     *            The number of violations that are expected
     */
    public void checkValid(AbstractCheck check, SourceCode code, DependencyMapper mapper) {
        check(check, code, mapper);

        if (code.getIssues().size() > 0) {
            StringBuffer error = new StringBuffer("Code is valid, expected 0 issues but got:");
            for (Issue issue : code.getIssues()) {
                error.append("\n    - Issue on line ").append(issue.line() + ": " + issue.message());
            }
            Assert.fail(error.toString());
        }
    }

    /**
     * Creates a SourceCode object from the list of strings
     * 
     * @param strings
     *            A list of lines of code
     * @return The SourceCode for the strings
     */
    public SourceCode code(String... strings) {
        List<String> list = new ArrayList<String>();
        for (String string : strings) {
            list.add(string);
        }
        return new XQuerySourceCode(list);
    }

    /**
     * Returns the issue for the source code in the specified check at the
     * specified index. If no issue exists at that index then the test will
     * fail.
     * 
     * @param check
     *            The check that was processed
     * @param index
     *            The index of the issue for which to look
     * @return The violation
     */
    public Issue getIssue(AbstractCheck check, int index) {
        SourceCode sourceCode = check.getSourceCode();
        Assert.assertNotNull(sourceCode, "Source code from check");
        List<Issue> issues = sourceCode.getIssues();
        int size = issues.size();
        Assert.assertTrue(size > 0, "Number of issues");
        Assert.assertTrue(size > index, "Requested index doesn't exist");
        return issues.get(index);
    }

    /**
     * Returns the issues for the source code in the specified check at the
     * specified index. If no issues exists then the test will fail.
     * 
     * @param check
     *            The check that was processed
     * @param count
     *            The number of issues that are expected
     * @return A list of all the issues
     */
    public List<Issue> getIssues(AbstractCheck check, int count) {
        SourceCode sourceCode = check.getSourceCode();
        Assert.assertNotNull(sourceCode, "Source code from check");
        List<Issue> issues = sourceCode.getIssues();
        Assert.assertEquals(issues.size(), count, "Number of issues");
        return issues;
    }

    /**
     * Does a "module import" and populates the appropriate global declarations
     * in the internal mapper object. This mapper is used when doing checks.
     * 
     * @param code
     *            The source code to "import"
     */
    protected DependencyMapper importModule(SourceCode code) {
        return importModule(code, new DependencyMapper());
    }
    
    protected DependencyMapper importModule(SourceCode code, DependencyMapper mapper) {
        XQueryAstParser parser = new XQueryAstParser(code, null);

        try {
            logger.fine(code.getCodeString());
            XQueryTree tree = parser.parse(getReporter());
            logger.fine(tree.toStringTree());
            parser.mapDependencies(tree, mapper);
        } catch (RecognitionException e) {
            Assert.fail("Failed parsing 'imported' module source code", e);
        }
        
        return mapper;
    }

    /**
     * Checks to see if tracing is turned on
     * 
     * @return "true" if tracing is enabled, "false" if it isn't
     */
    public boolean isTracing() {
        return tracing;
    }

    /**
     * Logs the supplied message (at a fine level). For debugging parsing.
     * 
     * @param msg
     *            The message to output
     */
    public void log(String msg) {
        logger.fine(msg);
    }

    /**
     * Parses the supplied code to get the AST.
     * 
     * @param code
     *            The code to parse
     * @return An AST for the code if it was successfully parsed
     * @throws RecognitionException
     */
    public XQueryTree parse(SourceCode code) throws RecognitionException {
        // TODO: Make this reflection or something so I don't have to comment/uncomment things
        // This section requires -debug be turned on when compiling the grammar
        // so it has to be commented out unless debugging the grammar.
//        ANTLRStringStream source = new ANTLRStringStream(code.getCodeString());
//        XQueryLexer lexer = new XQueryLexer(source);
//        ProblemReporter reporter = new ProblemReporter();
//        reporter.setFailOnError(true);
//        LazyTokenStream tokenStream = new LazyTokenStream(lexer);
//        ParseTreeBuilder builder = new XQueryParseTreeBuilder("MainModule");
//        XQueryParser parser = new XQueryParser(tokenStream, builder);
//        parser.setReporter(reporter);
//        parser.setCharSource(source);
//        parser.setTreeAdaptor(new XQueryTreeAdaptor(true));
//        XQueryTree tree = (XQueryTree) parser.p_Module().getTree();
//
//        try {
//            String treeDirectory = System.getProperty("java.io.tmpdir") + "/parsetrees";
//            FileUtils.mkdir(treeDirectory);
//            FileUtils.fileWrite(treeDirectory + "/AST.txt", tree.toStringTree());
//            FileUtils.fileWrite(treeDirectory + "/parsetree.txt", builder.getTree().toStringTree());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        logger.fine(tree.toStringTree());
//        return tree;

        // If the section above is uncommented then comment this section
        List<XQueryAstVisitor> visitors = new ArrayList<XQueryAstVisitor>();
        XQueryAstParser parser = new XQueryAstParser(code, visitors);
        XQueryTree tree = null;

        try {
            logger.fine(code.getCodeString());
            tree = parser.parse(getReporter());
            logger.fine(tree.toStringTree());
        } catch (RecognitionException e) {
            Assert.fail("Failed parsing source code", e);
        }

        return tree;
    }

    public void trace() {
        trace(true);
    }

    /**
     * Enables (or disabled) tracing for a test or set of tests. This function
     * can be placed in any test or before all tests are run and can be used to
     * see the AST output of parsing.
     * 
     * @param enable
     *            Whether or not to enable tracing - "true" or "false"
     */
    public void trace(boolean enable) {
        if (enable) {
            tracing = true;
            Handler handler = new ConsoleHandler();
            handler.setFormatter(new TestLogFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } else {
            tracing = false;
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }
            logger.setLevel(Level.SEVERE);
        }
    }
}
