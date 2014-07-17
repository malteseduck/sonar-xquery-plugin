/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

import java.util.regex.Pattern;

public abstract class AbstractProhibitVariableCheck extends AbstractCheck {
      
    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        
        switch (node.getType())
        {
           case XQueryParser.ParamName:
           case XQueryParser.VarDecl:
           case XQueryParser.ForName:
           case XQueryParser.ForAt:
           case XQueryParser.LetName:
        	   String name = node.getValue("QName");
        	   if (!StringUtils.isBlank(name) && Pattern.matches(getPattern(), name)) {
        		   createIssue(node.getLine());
        	   }
        	   break;
        }
    }

    abstract protected void createIssue(int lineNumber);
    abstract protected String getPattern();
}
