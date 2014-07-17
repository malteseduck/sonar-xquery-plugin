/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.Declaration;
import org.sonar.plugins.xquery.parser.node.Function;
import org.sonar.plugins.xquery.rules.CheckClasses;

/**
 * Checks for reliance on effective boolean values in conditionals.
 * 
 * @since 1.0
 */
@Rule(
    key = EffectiveBooleanCheck.RULE_KEY,
    name = "Effective Boolean in Conditional Predicate",
    description = "Unless the value in the conditional is of type xs:boolean it is recommended you use " +
            "fn:exists(), fn:empty(), or other boolean functions inside of conditional predicates to check values.",
    priority = Priority.MINOR
)
public class EffectiveBooleanCheck extends AbstractCheck {

    public static final String RULE_KEY = "EffectiveBoolean";
    private static final RuleKey RULE = RuleKey.of(CheckClasses.REPOSITORY_KEY, RULE_KEY);

    private static final String[] FUNCTIONS = new String[] { "exists", "empty", "contains", "starts-with", "ends-with", "boolean", "not", "true", "false", "matches" };
    private static final String[] EXPRESSIONS = new String[] { "UnaryExpr =", "UnaryExpr eq", "UnaryExpr !=", "UnaryExpr ne", "UnaryExpr <", "UnaryExpr lt", "UnaryExpr <=", "UnaryExpr le", "UnaryExpr >", "UnaryExpr gt", "UnaryExpr >=", "UnaryExpr ge", "UnaryExpr castable as", "UnaryExpr instance of" };

    @Override
    public void enterExpression(XQueryTree node) {
        super.enterExpression(node);
        if (XQueryParser.IfPredicate == node.getType()) {
            boolean valid = false;

            // Function calls are path expressions, make sure they are the only
            // node in the path expression
            XQueryTree pathExpr = node.find("IfPredicate.UnaryExpr.PathExpr", false);
            if (pathExpr != null && pathExpr.getChildCount() == 1) {

                // Check for valid function calls
                String functionName = pathExpr.getChildTextValue("PathExpr.FunctionCall.FunctionName.QName");
                Function decl = getMapper().getFunctionDeclaration(functionName, getMapper().resolvePrefixNamespace(functionName));
                if (decl != null && "xs:boolean".equals(decl.getType())) {
                    valid = true;
                } else {
                    for (String function : FUNCTIONS) {
                        if (StringUtils.endsWith(functionName, function)) {
                            valid = true;
                        }
                    }
                }
            }

            // Check to see if there is a boolean expression
            String predicate = node.getValue();
            for (String expression : EXPRESSIONS) {
                if (StringUtils.contains(predicate, expression)) {
                    valid = true;
                }
            }

            // Check to see if there is a variable reference and if the variable
            // is a boolean
            predicate = node.getTextValue("PathExpr");
            if ("$QName".equals(predicate)) {
                String varName = node.getTextValue("PathExpr.QName");
                // TODO: do a "resolve namespace" function on the qname against registered prefixes
                Declaration decl = getMapper().getVariableDeclaration(varName, getMapper().resolvePrefixNamespace(varName));
                if (decl != null && "xs:boolean".equals(decl.getType())) {
                    valid = true;
                }
            }

            if (!valid) {
                createIssue(RULE, node.getLine());
            }
        }
    }
}