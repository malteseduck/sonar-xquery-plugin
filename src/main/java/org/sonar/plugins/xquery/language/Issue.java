/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.sonar.api.rule.RuleKey;

public class Issue {
    private RuleKey rule;
    private int line;
    private String message;

    public Issue(RuleKey rule, int line, String message) {
        this.rule = rule;
        this.line = line;
        this.message = message;
    }

    public RuleKey rule() {
        return rule;
    }

    public int line() {
        return line;
    }

    public String message() {
        return message;
    }
}
