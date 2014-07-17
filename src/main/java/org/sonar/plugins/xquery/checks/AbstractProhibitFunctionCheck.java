/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProhibitFunctionCheck extends AbstractCheck {

    public Map<String, String> imports = new HashMap<String, String>();
       
    @Override
    public void enterExpression(XQueryTree node) {
        switch (node.getType())
        {
               case XQueryParser.ModuleImport:
                   String namespace = node.getValue("ModuleNamespace.StringLiteral");
                   String prefix = node.getValue("ModulePrefix");
                   imports.put(prefix, namespace);
                   break;
               case XQueryParser.FunctionCall:
                   String function = node.getTextValue("FunctionName.QName");

                   // Do not process if for some reason the function name is not there
                   if (StringUtils.isNotBlank(function)) {
                       String[] parts = function.split(":");

                       // If there is a prefix then set it, otherwise just set the function name
                       String funcPrefix = parts.length == 2 ? parts[0] : "";
                       String functionName = parts.length == 2 ? parts[1] : parts[0];

                       String funcNamespace = imports.get(funcPrefix);
                       String passPrefix = getFunctionPrefix();
                       if(
                           ( (passPrefix != null && passPrefix.equals(funcPrefix)) ||
                             (funcNamespace != null && funcNamespace.equals(getFunctionNamespace()) ) ) &&
                             functionName.equals(getFunctionName())) {
                           createIssue(node.getLine());
                       }
                   }
                   break;
        }
    }

    abstract protected void createIssue(int lineNumber);
    abstract protected String getFunctionNamespace();
    abstract protected String getFunctionName();
    abstract protected String getFunctionPrefix();
}