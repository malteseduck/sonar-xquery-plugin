/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules;

import org.sonar.plugins.xquery.checks.*;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Provides a list of available checks.
 *
 * @since 1.0
 */
public final class CheckClasses {

    public static final String REPOSITORY_KEY = "xquery";

    private CheckClasses() {
    }

    public static List<Class> getChecks() {
        return ImmutableList.<Class>of(
            DynamicFunctionCheck.class,
            EffectiveBooleanCheck.class,
            FunctionMappingCheck.class,
            OperationsInPredicateCheck.class,
            OrderByRangeCheck.class,
            ParseErrorCheck.class,
            StrongTypingInFLWORCheck.class,
            StrongTypingInFunctionDeclarationCheck.class,
            StrongTypingInModuleVariableCheck.class,
            XPathDescendantStepsCheck.class,
            XPathSubExpressionsInPredicateCheck.class,
            XPathTextStepsCheck.class,
            XQueryVersionCheck.class
        );
    }
}
