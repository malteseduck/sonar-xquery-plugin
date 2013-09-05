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

import java.util.List;

/**
 * Default MarkLogic XQuery profile.
 * 
 * @since 1.0
 */
public final class XQueryProfile extends ProfileDefinition {

	@Override
	public RulesProfile createProfile(ValidationMessages validation) {
		RulesProfile rulesProfile = RulesProfile.create("Default Profile", XQueryConstants.XQUERY_LANGUAGE_KEY);
		
		// Add the default rules to the profile
		List<Rule> rules = new XQueryRulesRepository(new AnnotationRuleParser()).createDefaultRules();
		for (Rule rule : rules) {
			rulesProfile.activateRule(rule, rule.getSeverity());
		}
		
		rulesProfile.setDefaultProfile(true);
		return rulesProfile;
	}
}
