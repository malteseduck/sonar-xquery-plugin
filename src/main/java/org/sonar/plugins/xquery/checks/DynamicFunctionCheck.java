/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

/**
 * Checks for usage of dynamic functions.
 * 
 * @since 1.0
 */
@Rule(
		key = "DynamicFunction",
		name = "Dynamic Function Usage",
		description = "Avoid using xdmp:eval() and xdmp:value() where possible.  Instead use xdmp:invoke() or xdmp:unpath() or, if possible, function values to dynamically evaluate code logic. For more information see http://labslcl0274.qalab.ldschurch.org:8403/viewArticle.xqy?id=424",
		priority = Priority.MAJOR)
public class DynamicFunctionCheck extends AbstractCheck {

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        if (XQueryParser.FunctionCall == node.getType()) {
            String function = node.getValue("FunctionName.QName");
            if ("xdmp : eval".equals(function) || "xdmp : value".equals(function)) {
                createViolation(node.getLine());
            }
        }
    }
}