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
    key = "StrongTypingInFLWOR",
    name = "Use Strong Typing FLWOR Expressions",
    description = "Declare types for FLWOR 'let' and 'for' clauses to increase readability and catch potential bugs.  Also try to scope the types as narrowly as possible (i.e. use 'element()' instead of 'item()' when the value is an element) and include quantifiers on each type. For more information see http://labslcl0274.qalab.ldschurch.org:8403/viewArticle.xqy?id=424",
    priority = Priority.MINOR
)
public class StrongTypingInFLWORCheck extends AbstractCheck {

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        
        // Process FLWOR expressions
        if (XQueryParser.FLOWRExpr == node.getType()) {

            // If we have children we can loop through them and check both the
            // 'let' and 'for' clauses
            if (node.getChildCount() > 0) {
                for (XQueryTree clause : node.getChildren()) {                    

                    // Check any 'for' clauses
                    if ("ForClause".equals(clause.getText())) {
                        if (clause.find("ForType") == null) {
                            createViolation(clause.getLine());
                        }
                    }

                    // Check any 'let' clauses
                    if ("LetClause".equals(clause.getText())) {
                        if (clause.find("LetType") == null) {
                            createViolation(clause.getLine());
                        }
                    }
                }
            }
        }
    }
}
