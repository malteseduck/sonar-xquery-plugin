/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;

public abstract class AbstractCheck implements XQueryAstVisitor {

    private Rule rule;

    private SourceCode sourceCode;

    private DependencyMapper mapper;
    private int line = 1;
    
    @Override
    public void checkReport(ProblemReporter reporter) {
        // By default do nothing
    }

    protected final void createViolation(int lineNumber) {
        createViolation(lineNumber, rule.getDescription());
    }

    protected final void createViolation(int lineNumber, String message) {
        if (getViolation(lineNumber) == null) {
            Violation violation = Violation.create(rule, getSourceCode().getResource());
            violation.setMessage(message);
            violation.setLineId(lineNumber);
            getSourceCode().addViolation(violation);
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

    public SourceCode getSourceCode() {
        return sourceCode;
    }

    public Violation getViolation(int lineNumber) {
        for (Violation violation : getSourceCode().getViolations()) {
            if (violation.getRule() == rule && violation.getLineId() == lineNumber) {
                return violation;
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

    public final void setRule(Rule rule) {
        this.rule = rule;
    }

    public void setSourceCode(SourceCode sourceCode) {
        this.sourceCode = sourceCode;
    }
}
