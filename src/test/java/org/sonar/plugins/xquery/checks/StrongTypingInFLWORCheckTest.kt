/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class StrongTypingInFLWORCheckTest : AbstractSonarTest() {

    private val check = StrongTypingInFLWORCheck()

    @Test
    fun `Should fail when no type is specified in the "for" variable`() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for \$article in /ldswebml[@type = \"article\"]",
                "let \$title as xs:string := \$article/search-meta/title",
                "return",
                "    \$title"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun `Should fail when no type is specified in the "let" declaration`() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for \$article as element(ldswebml) in /ldswebml[@type = \"article\"]",
                "let \$title := \$article/search-meta/title",
                "return",
                "    \$title"
            )
        )
        assertIssueLine(check, 3)
    }

    @Test
    fun `Should be valid when all declarations have a type`() {
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for \$article as element(ldswebml) in /ldswebml[@type = \"article\"]",
                "let \$title as xs:string := \$article/search-meta/title",
                "return",
                "    \$title"
            )
        )
    }
}
