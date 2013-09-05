/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.antlr.runtime.debug;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.ParseTree;

import java.util.ArrayList;

public class XQueryParseTreeBuilder extends ParseTreeBuilder {
    
    public XQueryParseTreeBuilder(String grammarName) {
        super(grammarName);
    }
    
    public ParseTree create(Object payload) {
        return new XQueryParseTree(payload);
    }
    
    public void consumeToken(Token token) {
        if ( backtracking>0 ) return;
        XQueryParseTree ruleNode = (XQueryParseTree)callStack.peek();
        XQueryParseTree elementNode = (XQueryParseTree) create(token);
        elementNode.hiddenTokens = this.hiddenTokens;
        this.hiddenTokens = new ArrayList<Object>();
        ruleNode.addChild(elementNode);        
    }    
}
