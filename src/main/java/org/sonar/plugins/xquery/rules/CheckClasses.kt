/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules

import org.sonar.plugins.xquery.checks.*

/**
 * Provides a list of available checks.
 *
 * @since 1.0
 */
object CheckClasses {

    const val REPOSITORY_KEY = "xquery"

    val checks: List<Class<*>> =
        listOf(
            DynamicFunctionCheck::class.java,
            EffectiveBooleanCheck::class.java,
            FunctionMappingCheck::class.java,
            OperationsInPredicateCheck::class.java,
            OrderByRangeCheck::class.java,
            ParseErrorCheck::class.java,
            StrongTypingInFLWORCheck::class.java,
            StrongTypingInFunctionDeclarationCheck::class.java,
            StrongTypingInModuleVariableCheck::class.java,
            XPathDescendantStepsCheck::class.java,
            XPathSubExpressionsInPredicateCheck::class.java,
            XPathTextStepsCheck::class.java,
            XQueryVersionCheck::class.java
        )
}
