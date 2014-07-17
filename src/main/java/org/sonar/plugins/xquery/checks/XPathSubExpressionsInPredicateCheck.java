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
 * Checks for usage of text() XPath steps.
 * 
 * @since 1.0
 */
@Rule(
    key = XPathSubExpressionsInPredicateCheck.RULE_KEY,
    name = "Avoid XPath Sub-expressions in XPath Predicates",
    description = "Watch out for expressions like '[foo/bar]' or '[foo[bar]]' " +
            "because they can sometimes be bad for performance. " +
            "If the result is static it can be bound to a variable.",
    priority = Priority.INFO
)
public class XPathSubExpressionsInPredicateCheck extends AbstractPredicateCheck {
    // TODO: Maybe change this so it only catches nested predicates - unless
    // alternate ways to do sub-expressions is found

    public static final String RULE_KEY = "XPathSubExpressionsInPredicate";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);

        if (inPredicate() && XQueryParser.PathExpr == node.getType() && node.getValue().contains("/")) {
            // Create a violation for any XPath step expressions
            createIssue(RULE, node.getLine());

        } else if (inPredicate() && XQueryParser.Predicate == node.getType()) {
            // Create a violation for nested predicates
            createIssue(RULE, node.getLine());

        } else if (XQueryParser.Predicate == node.getType()) {
            // If we are entering a predicate then update the state
            enterPredicate();
        }
    }
}