/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * Checks for usage of the xdmp:log function.
 *
 * @since 1.1
 */
@Rule(
        key = "LogCheck",
        name = "Log Function Usage",
        description = "Favor using xdmp:trace() instead of xdmp:log()",
        priority = Priority.MINOR)
public class LogCheck extends AbstractProhibitFunctionCheck {
	    
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