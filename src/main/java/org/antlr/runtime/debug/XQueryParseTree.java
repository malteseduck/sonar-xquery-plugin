/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.antlr.runtime.debug;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.ParseTree;

public class XQueryParseTree extends ParseTree {

    public static int CURRENT_LINE = 1;
    
    public XQueryParseTree(Object label) {
        super(label);
    }

    @Override
    public String toStringTree() {
        return toStringTree(0);
    }

    private String toStringTree(int level) {
        StringBuffer buf = new StringBuffer();
        if (children == null || children.size() == 0) {
            if (payload instanceof Token) {
                buf.append(((Token) this.payload).getLine()).append(": ");
            }
            buf.append(this.toString());
        } else {
            if (!isNil()) {
                buf.append(this.toString());            
                buf.append(' ');
            }
            buf.append('\n');
            for (int i = 0; i <= level; i++) {
                buf.append("-");
            }
            for (int i = 0; children != null && i < children.size(); i++) {
                XQueryParseTree t = (XQueryParseTree) children.get(i);
                if (i > 0) {
                    buf.append('\n');
                    for (int j = 0; j <= level; j++) {
                        buf.append("-");
                    }
                }
                buf.append(t.toStringTree(level + 1));
            }
        }
        return buf.toString();
    }          
}
