/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

/**
 * Checks for usage of text() XPath steps.
 * 
 * @since 1.0
 */
@Rule(
    key = "OperationsInPredicate",
    name = "Avoid Operations in Predicates",
    description = "Instead of calling functions or performing operations in predicates " +
            "try assigning the results to a variable before the predicate.",
    priority = Priority.MAJOR
)
public class OperationsInPredicateCheck extends AbstractPredicateCheck {
    // TODO: Either create a new check for or add to this checks for operations in xdmp:directory()
    // TODO: Support for fn:local-name(), fn:name(), fn:node-name()?
    
    private static final String[] FUNCTIONS = new String[] { "data", "last", "not", "exists", "xs:integer", "string", "xs:decimal", "xs:double", "xs:float", "xs:date", "xs:dateTime", "xs:time", "xs:dayTimeDuration", "xs:yearMonthDuration", "xs:duration" };
    private static final String[] EXPRESSIONS = new String[] { "UnaryExpr +", "UnaryExpr -", "UnaryExpr div", "UnaryExpr *", "UnaryExpr mod" };    

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);

        if (XQueryParser.Predicate == node.getType()) {
            // Save the state of being in a predicate            
            enterPredicate();

            // Operators in predicates are invalid, create violations for any
            String value = node.getValue();
            for (String expression : EXPRESSIONS) {
                if (StringUtils.contains(value, expression)) { 
                    createViolation(node.getLine());
                }
            }        
        }        
        
        // Function calls in predicates are invalid
        if (inPredicate() && XQueryParser.FunctionCall == node.getType()) {
            boolean valid = false;
            
            // Look to see if the function is one of the "valid" ones
            String functionName = node.getTextValue("FunctionName.QName");
            for (String function : FUNCTIONS) {
                if (StringUtils.endsWith(functionName, function)) { 
                    valid = true;
                }
            }
            
            // If the function call is not "valid" then create a violation
            if (!valid) {
                createViolation(node.getLine());
            }
        
        } 
    }
}