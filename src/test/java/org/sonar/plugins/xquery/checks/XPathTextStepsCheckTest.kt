/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class XPathTextStepsCheckTest : AbstractSonarTest() {

    private val check = XPathTextStepsCheck()

    @Test
    fun testAtomization() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[title eq 'Title']"
            )
        )
    }

    @Test
    fun testClosingElement() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "<textarea>Hi</textarea>"
            )
        )
    }

    @Test
    fun testFalsePositive() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "fn:string(/article/text)"
            )
        )
    }

    @Test
    fun `Should fail when using text node selector in predicate`() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article[title/text() eq 'Title']"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article/title/text()"
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
                ")[2]/ss:server-status/ss:server-name/text()"
            )
        )
        assertIssueLine(check, 4)
    }

    @Test
    fun testInvalidWithFalsePositive() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "/article",
                "    /text",
                "    /nodes",
                "    /text()"
            )
        )
        assertIssueLine(check, 5)
    }

    @Test
    fun testNotFullyQualified() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (fn:empty(\$status)) then \$status else ()"
            )
        )
    }

    @Test
    fun testStringFunction() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "fn:string(/article/title)"
            )
        )
    }
}
