/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.visitor;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.parser.*;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class XQueryAstParser {

    private static final Logger logger = Logger.getLogger(XQueryAstParser.class.getName());

    private SourceCode sourceCode;
    private List<XQueryAstVisitor> visitors;

    public XQueryAstParser(SourceCode code, List<XQueryAstVisitor> visitors) {
        this.sourceCode = code;
        this.visitors = visitors;
    }

    public XQueryTree parse() throws RecognitionException {
        return parse(new ProblemReporter());
    }

    public XQueryTree parse(ProblemReporter reporter) throws RecognitionException {
        logger.fine("Parsing " + sourceCode + ":");
        ANTLRStringStream source = new ANTLRStringStream(sourceCode.getCodeString());
        source.name = sourceCode.toString();
        XQueryLexer lexer = new XQueryLexer(source);
        lexer.setReporter(reporter);
        LazyTokenStream tokenStream = new LazyTokenStream(lexer);
        XQueryParser parser = new XQueryParser(tokenStream);
        parser.setReporter(reporter);
        parser.setCharSource(source);
        parser.setTreeAdaptor(new XQueryTreeAdaptor(reporter.isFailOnError()));
        XQueryTree tree = (XQueryTree) parser.p_Module().getTree();

        logger.fine(tree.toStringTree());
        return tree;
    }

    /**
     * Do a quick pass and map the global declarations for the specified tree
     *
     */
    public void mapDependencies(XQueryTree tree, DependencyMapper mapper) {
        // Since the mapper doesn't use any of the parameters, just pass in
        // nulls
        mapper.enterSource(null, null, null);
        visit(tree, Arrays.asList(new XQueryAstVisitor[] { mapper }));
        mapper.exitSource(null);
    }

    public void process(XQueryTree tree, DependencyMapper mapper, ProblemReporter reporter) {
        for (XQueryAstVisitor visitor : visitors) {
            visitor.enterSource(sourceCode, tree, mapper);
        }
        visit(tree, visitors);
        for (XQueryAstVisitor visitor : visitors) {
            visitor.exitSource(tree);
            visitor.checkReport(reporter);
        }
    }

    private void visit(XQueryTree root, List<XQueryAstVisitor> visitors) {
        for (XQueryAstVisitor visitor : visitors) {
            visitor.enterExpression(root);
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            XQueryTree child = root.getChild(i);
            visit(child, visitors);
        }
        for (XQueryAstVisitor visitor : visitors) {
            visitor.exitExpression(root);
        }
    }
}
