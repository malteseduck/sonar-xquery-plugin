/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node;

import java.util.ArrayList;
import java.util.List;

public class Import extends Declaration {

    private List<String> atHints;

    public Import(String name, String namespace) {
        super(name, namespace);
        atHints = new ArrayList<String>();
    }

    public void addAtHint(String hint) {
        atHints.add(hint);
    }
    
    public String getAtHint() {
        return getAtHint(0);
    }
    
    public String getAtHint(int index) {
        if (atHints.size() > index) {
            return atHints.get(index);
        }
        return null;
    }

    public List<String> getAtHints() {
        return atHints;
    }

    public void setAtHints(List<String> atHints) {
        this.atHints = atHints;
    }
}
