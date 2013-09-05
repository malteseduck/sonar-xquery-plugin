/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds basic information about a function call or declaration so that it can
 * be used to make decisions when parsing files.
 * 
 * @author cieslinskice
 * 
 */
public class Function extends Declaration {
    private Map<String, Declaration> parameters;

    public Function(String name, String namespace) {
        super(name, namespace);
        this.parameters = new HashMap<String, Declaration>();
    }

    public void addParameter(Declaration variable) {
        this.parameters.put(variable.getName(), variable);
    }

    public Declaration getParameter(String name) {
        return parameters.get(name);
    }
    
    public Map<String, Declaration> getParameters() {
        return parameters;
    }

    public void setArguments(Map<String, Declaration> parameters) {
        this.parameters = parameters;
    }
}
