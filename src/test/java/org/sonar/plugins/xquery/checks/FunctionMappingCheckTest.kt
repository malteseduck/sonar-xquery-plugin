/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class FunctionMappingCheckTest : AbstractSonarTest() {

    private val check = FunctionMappingCheck()

    @Test
    fun testInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "let \$test := 'test'",
                "return \$test"
            )
        )
        assertIssueLine(check, 1)
    }

    @Test
    fun testInvalidMultiTransaction() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "let \$test := 'test'",
                "return \$test",
                ";",
                "xquery version '1.0-ml';",
                "let \$test := 'test'",
                "return \$test"
            ),
            2
        )
        assertIssueLines(check, intArrayOf(1, 5))
    }

    @Test
    fun testOldVersion() {
        checkValid(
            check,
            code(
                "xquery version '0.9-ml';",
                "let \$test := 'test'",
                "return \$test"
            )
        )
    }

    @Test
    fun testValid() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare option xdmp:mapping 'false';",
                "let \$test := 'test'",
                "return \$test"
            )
        )
    }
}
