/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class OperationsInPredicateCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new OperationsInPredicateCheck();

    @Test
    public void testAdditionOperation() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[1 to $index + $buffer]"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testAttributeComparison() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[@locale = fn:concat('e', 'n', 'g')]"
            )
        );
        assertIssueLine(check, 2);
    }
    
    @Test
    public void testDivisionOperation() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[1 to $index div $buffer]"
            )
        );
        assertIssueLine(check, 2);
    }           
        
    @Test
    public void testFalsePositiveFunctionCall() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[if (fn:not(@locale = ('eng', 'spa'))) then fn:exists(@lang) else fn:exists(@country)]"
            )
        );
    }

    @Test
    public void testFalsePositiveNamespace() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/forest-status[(./*:forest-name eq $forest)]"
            )
        );
    }

    @Test
    public void testFalsePositiveStar() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/expressions/expression[pattern eq '*']"
            )
        );
    }

    @Test
    public void testFunctionCall() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[1 to xdmp:random()]"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testFunctionCallMultiline() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article",
                "    [",
                "        title eq getTitle()",
                "    ]"
            )
        );
        assertIssueLine(check, 4);
    }

    @Test
    public void testLastFunction() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "fn:tokenize($name, '_')[1 to fn:last()]"
            )
        );
    }

    @Test
    public void testTypeFunction() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[xs:integer(sequence) gt 3]"
            )
        );
    }
    
    @Test
    public void testLastFunctionWithOperation() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "fn:tokenize($name, '_')[1 to fn:last() - 1]"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testLocalFunctionCall() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[title eq getTitle()]"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testModOperation() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[1 to $index mod $buffer]"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testMultiplicationOperation() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[1 to $index * $buffer]"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testOperationMultiline() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article",
                "    [",
                "        1 + 1 [",
                "            2 - 2",
                "        ]",
                "    ]"
            ),
            2
        );
        assertIssueLines(check, new int[]{4, 5});
    }

    @Test
    public void testStandaloneFunctionCall() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[fn:data()]"
            )
        );
    }
    
    @Test
    public void testValidFunctionCall() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[fn:not(@locale = ('eng', 'spa'))]"
            )
        );
    }
    
    @Test
    public void testValidOperation() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "let $sequence := 1 to $index + $buffer",
                "return",
                "    $sequence"
            )
        );
    }
}
