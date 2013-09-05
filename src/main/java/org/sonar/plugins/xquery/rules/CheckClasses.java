/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules;

import org.sonar.plugins.xquery.checks.*;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a list of available checks.
 *
 * @since 1.0
 */
public final class CheckClasses {

    private static final Class[] CLASSES = new Class[]{
            DynamicFunctionCheck.class,
            EffectiveBooleanCheck.class,
            FunctionMappingCheck.class,
            OperationsInPredicateCheck.class,
            OrderByRangeCheck.class,
            ParseErrorCheck.class,
            StrongTypingInFLWORCheck.class,
            StrongTypingInFunctionDeclarationCheck.class,
            StrongTypingInModuleVariableCheck.class,
            XPathDecendantStepsCheck.class,
            XPathSubExpressionsInPredicateCheck.class,
            XPathTextStepsCheck.class,
            XQueryVersionCheck.class,
    };

    public static List<Class> getDefaultCheckClasses() {
        return Arrays.asList(CLASSES);
    }

    public static List<Class> getCheckClasses() {
        return Arrays.asList(CLASSES);
    }
}
