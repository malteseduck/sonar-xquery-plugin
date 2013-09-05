/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

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
    key = "XPathSubExpressionsInPredicate",
    name = "Avoid XPath Sub-expressions in XPath Predicates",
    description = "Watch expressions like '[foo/bar]' or '[foo[bar]]' because they can sometimes be bad for performance.  If the result is static it can be bound to a variable. For more information see http://labslcl0274.qalab.ldschurch.org:8403/viewArticle.xqy?id=424",
    priority = Priority.INFO
)
public class XPathSubExpressionsInPredicateCheck extends AbstractPredicateCheck {
    // TODO: Maybe change this so it only catches nested predicates - unless
    // alternate ways to do sub-expressions is found

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);

        if (inPredicate() && XQueryParser.PathExpr == node.getType() && node.getValue().contains("/")) {
            // Create a violation for any XPath step expressions
            createViolation(node.getLine());

        } else if (inPredicate() && XQueryParser.Predicate == node.getType()) {
            // Create a violation for nested predicates
            createViolation(node.getLine());

        } else if (XQueryParser.Predicate == node.getType()) {
            // If we are entering a predicate then update the state
            enterPredicate();
        }
    }
}