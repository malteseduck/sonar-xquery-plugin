/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.testng.annotations.Test

class ParseErrorCheckTest : AbstractSonarTest() {

    private val check = ParseErrorCheck()

    @Test
    fun `Should get an error on line 2 when parsing a missing brace in the catch block`() {
        reporter = ProblemReporter().apply {
            isFailOnError = false
        }
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare variable \$year := try { xs:int('2000') } catch (\$e)  };",
                "\$year"
            )
        )
        reporter = null
        assertIssueLine(check, 2)
    }

    @Test
    fun testFalsePositive() {
        reporter = ProblemReporter().apply {
            isFailOnError = false
        }
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "\"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\",",
                "'hi'"
            )
        )
        reporter = null
    }

    @Test
    fun testValid() {
        reporter = ProblemReporter().apply {
            isFailOnError = false
        }
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "fn:current-dateTime()"
            )
        )
        reporter = null
    }
}
