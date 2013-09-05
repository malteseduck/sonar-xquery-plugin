/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.rules;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.*;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.plugins.xquery.checks.AbstractCheck;

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

	private static final Logger logger = Logger.getLogger(XQueryRulesRepository.class.getName());

	public static final String REPOSITORY_NAME = "XQuery";
	public static final String REPOSITORY_KEY = XQueryConstants.XQUERY_LANGUAGE_KEY;

	private final AnnotationRuleParser annotationRuleParser;

	public XQueryRulesRepository(AnnotationRuleParser ruleParser) {
		super(REPOSITORY_KEY, XQueryConstants.XQUERY_LANGUAGE_KEY);
		setName(REPOSITORY_NAME);
		this.annotationRuleParser = ruleParser;
	}

	@Override
	public List<Rule> createRules() {
		List<Rule> rules = annotationRuleParser.parse(XQueryConstants.XQUERY_LANGUAGE_KEY, CheckClasses.getCheckClasses());
		for (Rule rule : rules) {
			rule.setCardinality(Cardinality.MULTIPLE);
		}
		return rules;
	}

    public List<Rule> createDefaultRules() {
        List<Rule> rules = annotationRuleParser.parse(XQueryConstants.XQUERY_LANGUAGE_KEY, CheckClasses.getDefaultCheckClasses());
        for (Rule rule : rules) {
            rule.setCardinality(Cardinality.MULTIPLE);
        }
        return rules;
    }

	/**
	 * Instantiate checks as defined in the RulesProfile.
	 * 
	 * @param profile
	 */
	public static List<AbstractCheck> createChecks(RulesProfile profile) {
	    logger.info("Loading checks for profile " + profile.getName());

		List<AbstractCheck> checks = new ArrayList<AbstractCheck>();

		for (ActiveRule activeRule : profile.getActiveRules()) {
			if (REPOSITORY_KEY.equals(activeRule.getRepositoryKey())) {
				Class<AbstractCheck> checkClass = getCheckClass(activeRule);
				if (checkClass != null) {
					checks.add(createCheck(checkClass, activeRule));
				}
			}
		}

		return checks;
	}

	private static AbstractCheck createCheck(Class<AbstractCheck> checkClass, ActiveRule activeRule) {

		try {
			AbstractCheck check = checkClass.newInstance();
			check.setRule(activeRule.getRule());
			if (activeRule.getActiveRuleParams() != null) {
				for (ActiveRuleParam param : activeRule.getActiveRuleParams()) {
					if (!StringUtils.isEmpty(param.getValue())) {
					    logger.fine("Rule param " + param.getKey() + " = " + param.getValue());
						BeanUtils.setProperty(check, param.getRuleParam().getKey(), param.getValue());
					}
				}
			}

			return check;
		} catch (IllegalAccessException e) {
			throw new SonarException(e);
		} catch (InvocationTargetException e) {
			throw new SonarException(e);
		} catch (InstantiationException e) {
			throw new SonarException(e);
		}
	}

	private static Class<AbstractCheck> getCheckClass(ActiveRule activeRule) {
		for (Class<?> checkClass : CheckClasses.getCheckClasses()) {

			org.sonar.check.Rule ruleAnnotation = AnnotationUtils.getClassAnnotation(checkClass, org.sonar.check.Rule.class);
			if (ruleAnnotation.key().equals(activeRule.getConfigKey())) {
				return (Class<AbstractCheck>) checkClass;
			}
		}
		logger.severe("Could not find check class for config key " + activeRule.getConfigKey());
		return null;
	}
}