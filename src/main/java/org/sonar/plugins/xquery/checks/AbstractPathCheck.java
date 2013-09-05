/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.parser.XQueryTree;

import java.util.List;

/**
 * Abstract class for checking rules inside of XPath expressions. Keeps track of
 * entering and exiting path expressions.
 * 
 * @since 1.0
 */
public class AbstractPathCheck extends AbstractCheck {

    /**
     * Find the index of the "offending" steps (on multi-line expressions the
     * line number could be higher)
     * 
     * @param expr
     *            The path expression node
     * @param expression
     *            The expression for which to check
     * @return the line number of the expression, or of the node if the
     *         expression wasn't found
     */
    protected void createViolations(XQueryTree expr, String expression) {
        List<XQueryTree> children = expr.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {

            // Check the text of the current node
            String value = children.get(i).getText();
            if (expression.equals(value)) {
                int index = i;
                // We want the node after the steps to get the line
                if (index < size + 1) {
                    index++;
                }
                createViolation(children.get(index).getLine());
            }
        }
    }
}