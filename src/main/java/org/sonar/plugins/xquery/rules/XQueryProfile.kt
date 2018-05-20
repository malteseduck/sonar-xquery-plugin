/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules

import org.sonar.api.profiles.AnnotationProfileParser
import org.sonar.api.profiles.ProfileDefinition
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.utils.ValidationMessages
import org.sonar.plugins.xquery.api.XQueryConstants

/**
 * Default XQuery profile.
 *
 * @since 1.0
 */
class XQueryProfile(private val annotationProfileParser: AnnotationProfileParser) : ProfileDefinition() {

    override fun createProfile(messages: ValidationMessages): RulesProfile {
        return annotationProfileParser.parse(CheckClasses.REPOSITORY_KEY, "Default Profile", XQueryConstants.XQUERY_LANGUAGE_KEY, CheckClasses.checks, messages)
    }
}
