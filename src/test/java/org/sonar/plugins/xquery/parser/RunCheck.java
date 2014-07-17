/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser;

import org.antlr.runtime.RecognitionException;
import org.codehaus.plexus.util.FileUtils;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.plugins.xquery.checks.AbstractCheck;
import org.sonar.plugins.xquery.checks.DynamicFunctionCheck;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.language.XQuerySourceCode;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;
import org.testng.Assert;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: cieslinskice
 * Date: 1/10/13
 * Time: 3:47 PM
 */
public class RunCheck {

    public static final String PATH = "/Users/cieslinskice/Documents/Code/lds-edit/src/main/xquery/invoke/function-apply.xqy";

    public static ProblemReporter getReporter() {
        ProblemReporter defaultReporter = new ProblemReporter();
        defaultReporter.setFailOnError(true);
        return defaultReporter;
    }

    public static void main(String[] args) throws IOException {
        AbstractCheck check = new DynamicFunctionCheck();
        DependencyMapper mapper = new DependencyMapper();
        SourceCode code = new XQuerySourceCode(FileUtils.fileRead(PATH));
        XQueryAstParser parser = new XQueryAstParser(code, Arrays.asList(new XQueryAstVisitor[] { mapper, check }));

        try {
            // Create the AST for the code
            XQueryTree tree = parser.parse(getReporter());

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
}
