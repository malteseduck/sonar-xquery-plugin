/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.visitor;

import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;

public interface XQueryAstVisitor
{
    public abstract void enterSource(SourceCode code, XQueryTree node, DependencyMapper mapper);

    public abstract void exitSource(XQueryTree node);

    public abstract void enterExpression(XQueryTree node);

    public abstract void exitExpression(XQueryTree node);

    public abstract void checkReport(ProblemReporter reporter);
}