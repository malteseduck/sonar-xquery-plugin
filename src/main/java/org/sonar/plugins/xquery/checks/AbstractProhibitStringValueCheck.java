/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

import java.util.regex.Pattern;

public abstract class AbstractProhibitStringValueCheck extends AbstractCheck {
      
    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        
        if (XQueryParser.StringLiteral == node.getType())
        {
    	   String value = node.getValue();
    	   if (Pattern.matches(getPattern(), value)) {
    		   createIssue(node.getLine());
    	   }
        }
    }

    abstract protected void createIssue(int lineNumber);
    abstract protected String getPattern();
}
