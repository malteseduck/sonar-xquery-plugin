/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.parser.XQueryParser.PredicateContext

/**
 * Abstract class for checking rules inside of XQuery predicates. Keeps track of
 * entering and exiting predicate expressions and keeps track of the nesting
 * levels.
 *
 * @since 1.0
 */
open class AbstractPredicateCheck : AbstractCheck() {

    var level = 0
        protected set

    override fun enterSource(node: ParserRuleContext) {
        level = 0
    }

    override fun exitExpression(node: ParserRuleContext) {
        // Update the state of exiting a predicate
        if (node is PredicateContext) {
            exitPredicate()
        }
    }

    fun inPredicate(): Boolean = level > 0

    protected fun enterPredicate() {
        level++
    }

    protected fun exitPredicate() {
        if (level > 0) level--
    }
}