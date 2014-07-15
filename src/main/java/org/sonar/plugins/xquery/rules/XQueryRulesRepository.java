/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.*;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.plugins.xquery.checks.AbstractCheck;
import org.sonar.plugins.xquery.language.XQuery;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repository for XQuery rules.
 * 
 * @since 1.0
 */
public final class XQueryRulesRepository extends RuleRepository {

    private static final String REPOSITORY_NAME = "XQuery";

    private final AnnotationRuleParser annotationRuleParser;

    public XQueryRulesRepository(AnnotationRuleParser annotationRuleParser) {
        super(CheckClasses.REPOSITORY_KEY, XQueryConstants.XQUERY_LANGUAGE_KEY);
        setName(REPOSITORY_NAME);
        this.annotationRuleParser = annotationRuleParser;
    }

    @Override
    public List<Rule> createRules() {
        return annotationRuleParser.parse(CheckClasses.REPOSITORY_KEY, CheckClasses.getChecks());
    }
}