/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xquery.language.Issue;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;

public abstract class AbstractCheck implements XQueryAstVisitor {

    private SourceCode sourceCode;

    private DependencyMapper mapper;
    private int line = 1;
    
    @Override
    public void checkReport(ProblemReporter reporter) {
        // By default do nothing
    }

    protected final void createIssue(RuleKey rule, int lineNumber) {
        createIssue(rule, lineNumber, rule.rule());
    }

    protected final void createIssue(RuleKey rule, int lineNumber, String message) {
        if (getIssue(rule, lineNumber) == null) {
            getSourceCode().addIssue(new Issue(rule, lineNumber, message));
        }
    }

    @Override
    public void enterExpression(XQueryTree node) {
        // Do nothing by default
    }

    @Override
    public void enterSource(SourceCode sourceCode, XQueryTree node, DependencyMapper mapper) {
        setMapper(mapper);
        setSourceCode(sourceCode);
        enterSource(node);
    }

    public void enterSource(XQueryTree node) {
        // Default implementation doesn't check anything
    }

    @Override
    public void exitExpression(XQueryTree node) {
        // Do nothing by default
    }

    @Override
    public void exitSource(XQueryTree node) {
        // Do nothing by default
    }

    public int getLine() {
        return line;
    }

    public DependencyMapper getMapper() {
        return mapper;
    }

    public SourceCode getSourceCode() { return sourceCode; }

    public Issue getIssue(RuleKey rule, int lineNumber) {
        for (Issue issue : getSourceCode().getIssues()) {
            if (issue.rule() == rule && issue.line() == lineNumber) {
                return issue;
            }
        }
        return null;
    }

    public void setLine(int line) {
        if (line < 1) {
            this.line = 1;
        } else {
            this.line = line;
        }
    }

    public void setMapper(DependencyMapper mapper) {
        this.mapper = mapper;
    }

    public void setSourceCode(SourceCode sourceCode) { this.sourceCode = sourceCode; }
}
