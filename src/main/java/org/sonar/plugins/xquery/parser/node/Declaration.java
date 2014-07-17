/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node;

import org.apache.commons.lang.StringUtils;

/**
 * Holds basic information about a declaration (by default, a variable) so that
 * it can be used for further reference in parsing files. References are also
 * treated as "declarations" since they "declare" a use of a function or
 * variable.
 * 
 * @author cieslinskice
 * 
 */
public class Declaration {
    private String name;

    private String type;

    private String namespace;

    private int line;

    public Declaration(String name, String namespace) {
        setName(name);
        setNamespace(namespace);
        setLine(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.hashCode() == this.hashCode()) {
            return true;
        }
        return false;
    }

    public int getLine() {
        return line;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (StringUtils.isNotBlank(name)) {
            hash += name.hashCode();
        }
        if (StringUtils.isNotBlank(namespace)) {
            hash += namespace.hashCode();
        }
        return hash;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setName(String name) {
        if (StringUtils.contains(name, ":")) {
            name = StringUtils.substringAfter(name, ":");
        }
        this.name = name;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("Declaration [");
        if (StringUtils.isNotBlank(namespace)) {
            buffer.append(namespace).append(":");
        }
        buffer.append(name);
        if (StringUtils.isNotBlank(type)) {
            buffer.append("=").append(type);
        }
        buffer.append(" (").append(line).append(")]");
        return buffer.toString();
    };

}
