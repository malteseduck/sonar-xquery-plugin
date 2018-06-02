/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules

import org.sonar.api.rules.AnnotationRuleParser
import org.sonar.api.rules.Rule
import org.sonar.api.rules.RuleRepository
import org.sonar.plugins.xquery.api.XQueryConstants

/**
 * Repository for XQuery rules.
 *
 * @since 1.0
 */
class XQueryRulesRepository(private val annotationRuleParser: AnnotationRuleParser) : RuleRepository(CheckClasses.REPOSITORY_KEY, XQueryConstants.XQUERY_LANGUAGE_KEY) {

    init {
        name = REPOSITORY_NAME
    }

    override fun createRules(): List<Rule> {

        return annotationRuleParser.parse(CheckClasses.REPOSITORY_KEY, CheckClasses.checks)
    }

    companion object {
        private val REPOSITORY_NAME = "XQuery"
    }
}