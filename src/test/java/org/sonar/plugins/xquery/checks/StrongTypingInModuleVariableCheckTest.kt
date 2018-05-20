/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class StrongTypingInModuleVariableCheckTest : AbstractSonarTest() {

    private val check = StrongTypingInModuleVariableCheck()

    @Test
    fun testMissingType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare variable \$a := 2;",
                "\$a"
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
                "declare variable \$a as xs:integer := 2;",
                "\$a"
            )
        )
    }
}
