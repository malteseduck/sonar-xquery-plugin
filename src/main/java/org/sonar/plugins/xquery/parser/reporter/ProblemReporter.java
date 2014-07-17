/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.reporter;

import org.antlr.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ProblemReporter {
    
    private boolean failOnError;
    private boolean outputError;

    private List<Problem> problems;

    public ProblemReporter() {
        this(false);
    }

    public ProblemReporter(boolean failOnError) {
        this.failOnError = failOnError;
        this.outputError = true;
        problems = new ArrayList<Problem>();
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public boolean isFailOnError() {
        return failOnError;
    }
    
    public boolean isOutputError() {
        return outputError;
    }
    
    public void reportError(String id, String message, Token token) {
        Problem problem = new Problem(id, message, token);
        problems.add(problem);
        if (failOnError) {
            throw new RuntimeException(problem.getMessageString());
        } else if (outputError) {
            System.err.println(problem.getId() + problem.getMessageString());
        }
    }
        
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
    
    public void setOutputError(boolean outputError) {
        this.outputError = outputError;
    }
}
