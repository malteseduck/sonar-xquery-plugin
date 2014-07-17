/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.rules.CheckClasses;

/**
 * Checks for usage of the xdmp:log function.
 *
 * @since 1.1
 */
@Rule(
        key = LogCheck.RULE_KEY,
        name = "Log Function Usage (Marklogic)",
        description = "Favor using xdmp:trace() instead of xdmp:log().\n" +
                "Please note that this check is Marklogic specific.",
        priority = Priority.MINOR)
public class LogCheck extends AbstractProhibitFunctionCheck {

    public static final String RULE_KEY = "LogCheck";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    @Override
    protected void createIssue(int lineNumber) {
        createIssue(RULE, lineNumber);
    }

    @Override
	protected String getFunctionNamespace() {
		return null;
	}
	@Override
	protected String getFunctionName() {
		return "log";
	}

	@Override
	protected String getFunctionPrefix() {
		return "xdmp";
	}
}