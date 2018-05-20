/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class StrongTypingInFunctionDeclarationCheckTest : AbstractSonarTest() {

    private val check = StrongTypingInFunctionDeclarationCheck()

    @Test
    fun testMissingBothParameterType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(\$a, \$b)",
                "as xs:integer",
                "{",
                "    \$a + \$b",
                "};",
                "test:add(1, 1)"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testMissingParameterType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(\$a, \$b as xs:integer)",
                "as xs:integer",
                "{",
                "    \$a + \$b",
                "};",
                "test:add(1, 1)"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testMissingParameterTypeMultiline() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(",
                "    \$a,",
                "    \$b as xs:integer)",
                "as xs:integer",
                "{",
                "    \$a + \$b",
                "};",
                "test:add(1, 1)"
            )
        )
        assertIssueLine(check, 3)
    }

    @Test
    fun testMissingReturnType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(\$a as xs:integer, \$b as xs:integer)",
                "{",
                "    \$a + \$b",
                "};",
                "test:add(1, 1)"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testMissingSecondParameterType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(\$a as xs:integer, \$b)",
                "as xs:integer",
                "{",
                "    \$a + \$b",
                "};",
                "test:add(1, 1)"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testValidDeclaration() {
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(\$a as xs:integer, \$b as xs:integer)",
                "as xs:integer",
                "{",
                "    \$a + \$b",
                "};",
                "test:add(1, 1)"
            )
        )
    }
}
