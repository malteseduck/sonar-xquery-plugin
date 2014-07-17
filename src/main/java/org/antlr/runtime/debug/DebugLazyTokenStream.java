/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.antlr.runtime.debug;

import org.sonar.plugins.xquery.parser.AbstractXQueryLexer;
import org.sonar.plugins.xquery.parser.LazyTokenStream;

public class DebugLazyTokenStream extends LazyTokenStream {

    protected DebugEventListener dbg;

    public DebugLazyTokenStream(AbstractXQueryLexer tokenSource, DebugEventListener dbg) {
        super();
        setDebugListener(dbg);        
        setTokenSource(tokenSource);
    }

    public void setDebugListener(DebugEventListener dbg) {
        this.dbg = dbg;
    }        
}
