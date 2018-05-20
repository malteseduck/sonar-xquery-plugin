/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class XQueryVersionCheckTest : AbstractSonarTest() {

    private val check = XQueryVersionCheck()

    @Test
    fun `Should fail when the version is set to the old ML version`() {
        checkInvalid(
            check,
            code(
                "xquery version \"0.9-ml\";",
                "fn:current-dateTime()"
            )
        )
        assertIssueLine(check, 1)
    }

    @Test
    fun testInvalidMultiTransaction() {
        checkInvalid(
            check,
            code(
                "fn:current-dateTime()",
                ";",
                "fn:current-dateTime()"
            ),
            2
        )
        assertIssueLines(check, intArrayOf(1, 3))
    }

    @Test
    fun testNoVersion() {
        checkInvalid(
            check,
            code(
                "fn:current-dateTime()"
            )
        )
        assertIssueLine(check, 1)
    }

    @Test
    fun testValid() {
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "fn:current-dateTime()"
            )
        )
    }
}