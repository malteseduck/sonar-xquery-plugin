/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class XPathDescendantStepsCheckTest : AbstractSonarTest() {

    private val check = XPathDescendantStepsCheck()

    @Test
    fun testInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article//title"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testInvalidMultiline() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "xdmp:http-get(",
                "    fn:concat('http://', \$current, ':8013/status/cluster-status.xqy?mode=performance')",
                ")[2]//ss:server-status/ss:server-name"
            )
        )
        assertIssueLine(check, 4)
    }

    @Test
    fun testMultipleInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "\$sample//status[",
                "    @active eq 'true'",
                "]//ss:server-status/ss:server-name"
            ),
            2
        )
        assertIssueLines(check, intArrayOf(2, 4))
    }

    @Test
    fun testValid() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article/title"
            )
        )
    }

}
