/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

public abstract class AbstractProhibitLibraryCheck extends AbstractCheck {
      
    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        
        if (XQueryParser.ModuleNamespace == node.getType()) {
        	String namespace = node.getValue("StringLiteral");
        	if (getNamespace().equals(namespace)) {
        		createViolation(node.getLine());
        	}
        }
    }

   abstract protected String getNamespace();
}
