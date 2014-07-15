/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class StrongTypingInFunctionDeclarationCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new StrongTypingInFunctionDeclarationCheck();

    @Test
    public void testMissingBothParameterType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add($a, $b)",
                "as xs:integer",
                "{",
                "    $a + $b",
                "};",
                "test:add(1, 1)"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testMissingParameterType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add($a, $b as xs:integer)",
                "as xs:integer",
                "{",
                "    $a + $b",
                "};",
                "test:add(1, 1)"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testMissingParameterTypeMultiline() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add(",
                "    $a,",
                "    $b as xs:integer)",
                "as xs:integer",
                "{",
                "    $a + $b",
                "};",
                "test:add(1, 1)"
            )
        );
        assertIssueLine(check, 3);
    }
    
    @Test
    public void testMissingReturnType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add($a as xs:integer, $b as xs:integer)",
                "{",
                "    $a + $b",
                "};",
                "test:add(1, 1)"
            )
        );
        assertIssueLine(check, 2);
    }    

    @Test
    public void testMissingSecondParameterType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add($a as xs:integer, $b)",
                "as xs:integer",
                "{",
                "    $a + $b",
                "};",
                "test:add(1, 1)"
            )
        );
        assertIssueLine(check, 2);
    }
    
    @Test
    public void testValidDeclaration() {
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare function test:add($a as xs:integer, $b as xs:integer)",
                "as xs:integer",
                "{",
                "    $a + $b",
                "};",
                "test:add(1, 1)"
            )
        );
    }
}
