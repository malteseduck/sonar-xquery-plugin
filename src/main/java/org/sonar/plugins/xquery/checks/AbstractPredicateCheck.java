/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;

/**
 * Abstract class for checking rules inside of XQuery predicates. Keeps track of
 * entering and exiting predicate expressions and keeps track of the nesting
 * levels.
 * 
 * @since 1.0
 */
public class AbstractPredicateCheck extends AbstractCheck {

    protected int level = 0;

    @Override
    public void enterSource(XQueryTree node) {
        level = 0;
    }

    @Override
    public void exitExpression(XQueryTree node) {
        // Update the state of exiting a predicate
        if (XQueryParser.Predicate == node.getType()) {
            exitPredicate();
        }
    }

    public int getLevel() {
        return level;
    }
    
    public boolean inPredicate() {
        return level > 0;
    }

    protected void enterPredicate() {
        level++;
    }

    protected void exitPredicate() {
        if (level > 0) {
            level--;
        }
    }
}