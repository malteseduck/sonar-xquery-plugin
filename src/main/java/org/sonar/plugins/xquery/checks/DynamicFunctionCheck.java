/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.rules.CheckClasses;

/**
 * Checks for usage of dynamic functions.
 * 
 * @since 1.0
 */
@Rule(
    key = DynamicFunctionCheck.RULE_KEY,
    name = "Dynamic Function Usage (Marklogic)",
    description = "Avoid using xdmp:eval() and xdmp:value() where possible. " +
            "Instead use either xdmp:invoke(), xdmp:unpath() " +
            "or if possible assign functions to variables to dynamically evaluate code logic.\n" +
            "Please note that this check is Marklogic specific.",
    priority = Priority.MAJOR)
public class DynamicFunctionCheck extends AbstractCheck {

    public static final String RULE_KEY = "DynamicFunction";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        if (XQueryParser.FunctionCall == node.getType()) {
            String function = node.getValue("FunctionName.QName");
            if ("xdmp : eval".equals(function) || "xdmp : value".equals(function)) {
                createIssue(RULE, node.getLine());
            }
        }
    }
}