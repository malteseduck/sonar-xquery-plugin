/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

/**
 * Checks for function mapping usage
 * 
 * @since 1.0
 */
@Rule(
	key = "FunctionMapping",
	name = "Function Mapping Usage",
	description = "Make sure you are intentionally using and/or understand function mapping - otherwise disable it with 'declare option xdmp:mapping \"false\";'  If you wish to use it you should explicitly declare 'declare option xdmp:mapping \"true\";' for readability/maintainability.",
	priority = Priority.MAJOR
)
public class FunctionMappingCheck extends AbstractCheck {
	
    private boolean capable = false;
    private boolean used = false;

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        // Instead of checking when we enter the source we check after the 
        // modules so we can handle multi-transaction modules - reset the flags
        // for a new module transaction
        if (XQueryParser.MainModule == node.getType() || XQueryParser.LibraryModule == node.getType()) {
            capable = false;
            used = false;
            setLine(node.getLine());
                        
        } else if (XQueryParser.VersionValue == node.getType()) {
            // Check the version value to see if the script is capable of using
            // function mapping
            if("1.0-ml".equals(node.getValue("StringLiteral"))) {
                capable = true;
            }
        } else if (XQueryParser.OptionDecl == node.getType()) {
            // If the option is set then set the flag
            if("xdmp:mapping".equals(node.getTextValue("QName"))) {
                used = true;
            }
        }
    }

    @Override
    public void exitExpression(XQueryTree node) {
        super.exitExpression(node);
        // Instead of checking when we leave the source we check after the 
        // modules so we can handle multi-transaction modules - create a
        // violation if the flags are not correct
        if (XQueryParser.MainModule == node.getType() || XQueryParser.LibraryModule == node.getType()) {
            if (capable && !used) {
                createViolation(getLine());
            }
        }
    }    
}