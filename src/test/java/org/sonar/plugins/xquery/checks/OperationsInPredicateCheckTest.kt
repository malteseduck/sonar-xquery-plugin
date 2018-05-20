/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class OperationsInPredicateCheckTest : AbstractSonarTest() {

    private val check = OperationsInPredicateCheck()

    @Test
    fun testAdditionOperation() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[1 to \$index + \$buffer]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testAttributeComparison() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[@locale = fn:concat('e', 'n', 'g')]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testDivisionOperation() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[1 to \$index div \$buffer]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testFalsePositiveFunctionCall() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[if (fn:not(@locale = ('eng', 'spa'))) then fn:exists(@lang) else fn:exists(@country)]"
            )
        )
    }

    @Test
    fun testFalsePositiveNamespace() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/forest-status[(./*:forest-name eq \$forest)]"
            )
        )
    }

    @Test
    fun testFalsePositiveStar() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/expressions/expression[pattern eq '*']"
            )
        )
    }

    @Test
    fun testFunctionCall() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[1 to xdmp:random()]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testFunctionCallMultiline() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article",
                "    [",
                "        title eq getTitle()",
                "    ]"
            )
        )
        assertIssueLine(check, 4)
    }

    @Test
    fun testLastFunction() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "fn:tokenize(\$name, '_')[1 to fn:last()]"
            )
        )
    }

    @Test
    fun testTypeFunction() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[xs:integer(sequence) gt 3]"
            )
        )
    }

    @Test
    fun testLastFunctionWithOperation() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "fn:tokenize(\$name, '_')[1 to fn:last() - 1]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testLocalFunctionCall() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[title eq getTitle()]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testModOperation() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[1 to \$index mod \$buffer]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testMultiplicationOperation() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[1 to \$index * \$buffer]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testOperationMultiline() {
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
        )
        assertIssueLines(check, intArrayOf(4, 5))
    }

    @Test
    fun testStandaloneFunctionCall() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[fn:data()]"
            )
        )
    }

    @Test
    fun testValidFunctionCall() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[fn:not(@locale = ('eng', 'spa'))]"
            )
        )
    }

    @Test
    fun testValidOperation() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "let \$sequence := 1 to \$index + \$buffer",
                "return",
                "    \$sequence"
            )
        )
    }
}
