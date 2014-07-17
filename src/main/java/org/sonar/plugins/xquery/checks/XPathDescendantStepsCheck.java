/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
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
        key = XPathDescendantStepsCheck.RULE_KEY,
        name = "Avoid Using '//' in XPath",
        description = "Favor fully-qualified paths in XPath " +
                "for readability and to avoid potential performance problems.",
        priority = Priority.MINOR)
public class XPathDescendantStepsCheck extends AbstractPathCheck {

    public static final String RULE_KEY = "XpathDescendantSteps";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        if (XQueryParser.PathExpr == node.getType()) {
            String expr = node.getValue();
            if (StringUtils.contains(expr, "//")) {
                createViolations(RULE, node, "//");
            }
        }
    }
}