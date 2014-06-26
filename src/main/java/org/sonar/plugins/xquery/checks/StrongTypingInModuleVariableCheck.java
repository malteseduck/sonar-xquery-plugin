/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

/**
 * Checks for strong typing in module variable declarations
 * 
 * @since 1.0
 */
@Rule(
    key = "StrongTypingInModuleVariables",
    name = "Use Strong Typing when Declaring Module Variable",
    description = "Declare types for declared variables to increase readability and catch potential bugs. " +
            "Also try to scope the types as narrowly as possible " +
            "(i.e. use 'element()' instead of 'item()' when the value is an element) " +
            "and include quantifiers on each type.",
    priority = Priority.CRITICAL
)
public class StrongTypingInModuleVariableCheck extends AbstractCheck {

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        
        // Process variable declarations
        if (XQueryParser.VarDecl == node.getType()) {
            
            // If we don't have the variable type then create a violation
            XQueryTree type = node.find("VarType");
            if (type == null || type.getChildCount() == 0) {
                createViolation(node.getLine());
            }
        }
    }
}
