/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.reporter.Problem;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;

@Rule(
    key = "ParseError",
    name = "Code Parsing Error",
    description = "This is to catch parsing errors on projects.  There may be a potential syntax error, or the parser just may not be able to process certain syntax.  Technically this rule does not take part in the AST parsing. For more information see http://labslcl0274.qalab.ldschurch.org:8403/viewArticle.xqy?id=424",
    priority = Priority.INFO
)
public class ParseErrorCheck extends AbstractCheck {
    
    private static String[] MESSAGES = new String[] {"no viable alternative at character 'D'"};
        
    @Override
    public void checkReport(ProblemReporter reporter) {
        boolean allowed = false;
        
        for (Problem problem : reporter.getProblems()) {
            String pMessage = problem.getMessage();
            int line = problem.getLine();
            
            // Look at all the "allowed" messages to see if the problem matches
            for (String message : MESSAGES) {
                if (StringUtils.contains(pMessage, message)) {
                    allowed = true;
                }
            }
            
            if (!allowed && line > 0) {
                createViolation(line, problem.getMessageString());
            }
        }
    }
}
