/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class OrderByRangeCheckTest : AbstractSonarTest() {

    private val check = OrderByRangeCheck()

    // Currently can't accurately support "gt" and "lt" without a parser
    fun testComparison() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "/article[index gt 0]"
            )
        )
        assertIssueLine(check, 2)
    }

    //    @Test - the current parser doesn't handle entities well
    fun testFalsePositive() {
        // This is not a comparision and should not be matched
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "let \$xml := \"&lt;bob&gt;\"",
                "return",
                "    \$xml"
            )
        )
    }

    @Test
    fun testOrderBy() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for \$article in /article",
                "order by \$article/title",
                "return \$article"
            )
        )
        assertIssueLine(check, 3)
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
