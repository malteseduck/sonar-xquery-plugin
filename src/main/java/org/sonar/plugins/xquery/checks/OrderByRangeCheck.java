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
        key = OrderByRangeCheck.RULE_KEY,
        name = "Range Evaluation in Order By Clause",
        description = "Order bys or gt/lt checks on large numbers of documents " +
                "might achieve better performance with a range index.",
        priority = Priority.INFO)
public class OrderByRangeCheck extends AbstractCheck {

    public static final String RULE_KEY = "OrderByRange";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        if (XQueryParser.OrderSpec == node.getType()) {
            createIssue(RULE, node.getLine());
        }
    }
}