package org.sonar.plugins.xquery.rules

import org.sonar.api.server.rule.RulesDefinition
import org.sonar.plugins.xquery.api.XQueryConstants
import org.sonarsource.analyzer.commons.RuleMetadataLoader

class XQueryRulesDefinition : RulesDefinition {

    override fun define(context: RulesDefinition.Context) {
        val repository = context
            .createRepository(REPOSITORY_KEY, XQueryConstants.XQUERY_LANGUAGE_KEY)
            .setName(REPOSITORY_NAME)

        val ruleMetadataLoader = RuleMetadataLoader("org/sonar/rules/xquery")
        ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckClasses.checks)

        for (rule in repository.rules()) {
            if (TEMPLATE_RULE_KEYS.contains(rule.key())) {
                rule.setTemplate(true)
            }
        }

        repository.done()
    }

    companion object {

        val REPOSITORY_KEY = XQueryConstants.XQUERY_LANGUAGE_NAME
        val REPOSITORY_NAME = "XQuery"

        private val TEMPLATE_RULE_KEYS: Set<String> = emptySet()
    }
}