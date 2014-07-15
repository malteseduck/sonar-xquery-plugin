/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.api.profiles.AnnotationProfileParser;

import java.util.List;

/**
 * Default XQuery profile.
 * 
 * @since 1.0
 */
public final class XQueryProfile extends ProfileDefinition {

    private final AnnotationProfileParser annotationProfileParser;

    public XQueryProfile(AnnotationProfileParser annotationProfileParser) {
        this.annotationProfileParser = annotationProfileParser;
    }

    @Override
    public RulesProfile createProfile(ValidationMessages messages) {
        return annotationProfileParser.parse(CheckClasses.REPOSITORY_KEY, "Default Profile", XQueryConstants.XQUERY_LANGUAGE_KEY, CheckClasses.getChecks(), messages);
    }
}
