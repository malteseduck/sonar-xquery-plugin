/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.rules.CheckClasses;

/**
 * Checks for strong typing in function declarations
 * 
 * @since 1.0
 */
@Rule(
    key = StrongTypingInFunctionDeclarationCheck.RULE_KEY,
    name = "Use Strong Typing in Function Declarations",
    description = "Declare types for function parameters and return types " +
            "to increase readability and catch potential bugs. " +
            "Also try to scope the types as narrowly as possible " +
            "(i.e. use 'element()' instead of 'item()' when returning an element) " +
            "and include quantifiers on each type.",
    priority = Priority.CRITICAL
)
public class StrongTypingInFunctionDeclarationCheck extends AbstractCheck {

    public static final String RULE_KEY = "StrongTypingInFunctionDeclaration";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);

        // Process function declarations
        if (XQueryParser.FunctionDecl == node.getType()) {

            // If we have any parameters declared - check them
            XQueryTree params = node.find("ParamList");
            if (params != null && params.getChildCount() > 0) {
                for (XQueryTree param : params.getChildren()) {                    
                    // If the parameter does not have a type declaration it is a violation
                    XQueryTree type = param.find("TypeDeclaration");
                    if (type == null) {
                        createIssue(RULE, param.getLine());
                    }
                }
            }

            // Check the return type
            XQueryTree returnType = node.find("ReturnType");
            if (returnType == null || returnType.getChildCount() == 0) {
                createIssue(RULE, node.getLine());
            }
        }
    }
}
