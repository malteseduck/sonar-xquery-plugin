/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.reporter.Problem;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.rules.CheckClasses;

@Rule(
    key = ParseErrorCheck.RULE_KEY,
    name = "Code Parsing Error",
    description = "This is to catch parsing errors on projects. " +
            "There may be a potential syntax error, or the parser just may not be able to process certain syntax.",
    priority = Priority.INFO
)
public class ParseErrorCheck extends AbstractCheck {
    
    private static String[] MESSAGES = new String[] {"no viable alternative at character 'D'"};

    public static final String RULE_KEY = "ParseError";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

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
                createIssue(RULE, line, problem.getMessage());
            }
        }
    }
}
