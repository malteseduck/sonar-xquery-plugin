/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks

import org.sonar.plugins.xquery.AbstractSonarTest
import org.sonar.plugins.xquery.parser.node.DependencyMapper
import org.testng.annotations.Test

class EffectiveBooleanCheckTest : AbstractSonarTest() {

    private val check = EffectiveBooleanCheck()

    @Test
    fun testBooleanEqComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status eq 'published') then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanEqualsComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status = ('published', 'preview')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test(enabled = false, description = "This complicated case is ok, but the check doesn't process it correctly")
    fun testBooleanFalsePositive() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$article[name = 'bob']/enabled/fn:not(.)) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testBooleanFalsePositiveFLOWR() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (let \$check as xs:boolean := fn:true() return \$check) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testBooleanFunctionGlobal() {
        val mapper = importModule(
            code(
                "xquery version '1.0-ml';",
                "",
                "module namespace test = 'http://lds.org/code/test';",
                "",
                "declare function test:status()",
                "as xs:boolean",
                "{",
                "    fn:true()",
                "};"
            )
        )
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                "if (test:status()) then",
                "    \$test:status",
                "else",
                "    ()"
            ),
            mapper
        )
    }

    @Test
    fun testBooleanFunctionLocal() {
        val mapper = DependencyMapper()
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare function local:checkStatus()",
                "as xs:boolean?",
                "{",
                "    fn:true()",
                "};",
                "if (local:checkStatus()) then",
                "    'hi'",
                "else",
                "    'bye'"
            ),
            mapper
        )
    }

    @Test
    fun testBooleanGeComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status ge 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanGreaterThanComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status > 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanGreaterThanEqualComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status >= 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanGtComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status gt 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanInFunctionBody() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "module namespace layout = 'http://lds.org/code/test/layout';",
                "declare function layout:check(\$monitored as xs:boolean)",
                "as xs:string",
                "{",
                "    if (\$monitored) then",
                "        'yes'",
                "    else",
                "       'no'",
                "};"
            )
        )
    }

    @Test
    fun testBooleanInFunctionBodyFLWOR() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "module namespace layout = 'http://lds.org/code/test/layout';",
                "declare function layout:check(\$servers as xs:string*)",
                "as xs:string",
                "{",
                "    for \$server in \$servers",
                "    let \$monitored as xs:boolean := \$server/monitored",
                "    return",
                "        if (\$monitored) then",
                "            'yes'",
                "        else",
                "           'no'",
                "};"
            )
        )
    }

    @Test
    fun testBooleanLeComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status le 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanLessThanComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status < 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanLessThanEqualComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status <= 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanLtComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status lt 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanNeComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status ne ('published', 'preview')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanNotEqualsComparison() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status != ('published', 'preview')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanPositiveFLOWR() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if ((let \$test := 1 return 1) = 1) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testBooleanVariable() {
        val mapper = DependencyMapper()
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "let \$status as xs:boolean := fn:true()",
                "return",
                "    if (\$status) then",
                "        \$status",
                "    else",
                "        ()"
            ),
            mapper
        )
    }

    @Test
    fun testBooleanVariableFunctionParameter() {
        val mapper = DependencyMapper()
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare function local:checkStatus(\$status as xs:boolean)",
                "as xs:boolean?",
                "{",
                "    if (\$status) then",
                "        \$status",
                "    else",
                "        ()",
                "};",
                "local:checkStatus(fn:true())"
            ),
            mapper
        )
    }

    @Test
    fun testBooleanVariableFunctionParameterRedefined() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare function local:checkStatus(\$status as xs:boolean)",
                "as xs:string",
                "{",
                "    let \$status as xs:string := 'published'",
                "    return",
                "        if (\$status) then",
                "            \$status",
                "        else",
                "            'no'",
                "};",
                "local:checkStatus(fn:true())"
            )
        )
        assertIssueLine(check, 7)
    }

    @Test
    fun testBooleanVariableGlobal() {
        val mapper = importModule(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare variable \$status as xs:boolean := fn:true();"
            )
        )
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                "if (\$test:status) then",
                "    \$test:status",
                "else",
                "    ()"
            ),
            mapper
        )
    }

    @Test
    fun testContains() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (contains(\$status, 'bubba')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testEmpty() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (fn:empty(\$status)) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testEmptyWithoutPrefix() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (empty(\$status)) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testEndsWith() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (ends-with(\$status, 'bubba')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testExists() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (fn:exists(\$status)) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testExistsWithoutPrefix() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (exists(\$status)) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testExplicitEffectiveBoolean() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (boolean(\$status)) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testExpressionInHtml() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "<td style='background-color: { if (fn:exists(\$error)) then '#FF0000' else '#FFFF00' };'>{",
                "    if (\$percentage lt 100) then",
                "        '&nbsp;'",
                "    else ()",
                "}</td>"
            )
        )
    }

    @Test
    fun testInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (",
                "        \$status",
                ") then",
                "    \$status",
                "else",
                "    ()"
            )
        )
        assertIssueLine(check, 3)
    }

    @Test
    fun testInvalidMultiline() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testMatches() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (fn:matches(\$status, 'published')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testNestedFunction() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (\$status) then",
                "    fn:exists(\$status)",
                "else",
                "    ()"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testStartsWith() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (starts-with(\$status, 'bubba')) then",
                "    \$status",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testStartsWithFalsePositive() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "if (if (starts-with(\$status, 'bubba')) then 'yes' else 'no') then",
                "    \$status",
                "else",
                "    ()"
            )
        )
        assertIssueLine(check, 2)
    }

    @Test
    fun testInstanceOf() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare variable \$count as xs:integer := 1;",
                "if (\$check instance of xs:integer) then",
                "    \$check + 1",
                "else",
                "    ()"
            )
        )
    }

    @Test
    fun testCastableAs() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare variable \$count as xs:integer := 1;",
                "if (\$check castable as xs:integer) then",
                "    \$check + 1",
                "else",
                "    ()"
            )
        )
    }

    //@Test
    fun testOrMultiline() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare variable \$path as xs:integer := '/path';",
                "if (",
                "    (fn:ends-with(\$path, '.xqy') and \$path eq xdmp:get-request-path())",
                "    or fn:starts-with(xdmp:get-request-path(), \$path)",
                ") then",
                "    'selected'",
                "else ()"
            )
        )
    }
}
