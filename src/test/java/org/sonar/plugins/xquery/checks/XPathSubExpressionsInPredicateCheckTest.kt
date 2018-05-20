/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.testng.annotations.Test

class XPathSubExpressionsInPredicateCheckTest : AbstractSonarTest() {

    private val check = XPathSubExpressionsInPredicateCheck()

    @Test
    fun `Should fail when an XPath expression is contained in a predicate`() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "/ldswebml[search-meta/title]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun `Should fail when there are nested predicates`() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "/ldswebml[search-meta[title]]"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun `Should be valid when no sub expressions exist`() {
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "/article[title]"
            )
        )
    }
}
