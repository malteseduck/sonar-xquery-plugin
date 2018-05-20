/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.sonar.api.rule.RuleKey

class Issue(private val rule: RuleKey, private val line: Int, private val message: String) {

    fun rule(): RuleKey {
        return rule
    }

    fun line(): Int {
        return line
    }

    fun message(): String {
        return message
    }
}
