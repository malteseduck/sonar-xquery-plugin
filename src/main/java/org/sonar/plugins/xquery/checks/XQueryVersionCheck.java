/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.language.XQuery;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.rules.CheckClasses;

/**
 * Checks declaration of the XQuery version.
 * 
 * @since 1.0
 */
@Rule(
    key = XQueryVersionCheck.RULE_KEY,
    name = "XQuery Version",
    description = "Ensure that you declare the latest XQuery version (1.0-ml/3.0) " +
            "at the top of each of your scripts " +
            "(as opposed to declaring an older version - 0.9-ml - or not declaring a version at all). " +
            "This ensures better compatibility of code after server upgrades " +
            "and consistent behavior in XQuery processing.",
    priority = Priority.MINOR
)
public class XQueryVersionCheck extends AbstractCheck {

    private boolean hasVersion = false;

    public static final String RULE_KEY = "XQueryVersion";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        // Instead of checking when we enter the source we check after the 
        // modules so we can handle multi-transaction modules - reset the flags
        // for a new module transaction
        if (XQueryParser.MainModule == node.getType() || XQueryParser.LibraryModule == node.getType()) {
            hasVersion = false;
            setLine(node.getLine());
        } else if (XQueryParser.VersionValue == node.getType()) {
            // If there is a version then set the flag and check to see if
            // it is the "correct" newer version
            hasVersion = true;
            if ("0.9-ml".equals(node.getValue("StringLiteral"))) {
                createIssue(RULE, node.getLine());
            }
        }
    }

    @Override
    public void exitExpression(XQueryTree node) {
        super.exitExpression(node);
        // Instead of checking when we enter the source we check after the 
        // modules so we can handle multi-transaction modules - create a
        // violation if there is no version set
        if (XQueryParser.MainModule == node.getType() || XQueryParser.LibraryModule == node.getType()) {
            if (!hasVersion) {
                createIssue(RULE, getLine());
            }
        }
    }
}