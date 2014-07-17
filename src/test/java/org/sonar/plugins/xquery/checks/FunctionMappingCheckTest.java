/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class FunctionMappingCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new FunctionMappingCheck();

    @Test
    public void testInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "let $test := 'test'",
                "return $test"
            )
        );
        assertIssueLine(check, 1);
    }

    @Test
    public void testInvalidMultiTransaction() {
        checkInvalid(
            check,
            code(
                "xquery version '1.0-ml';",
                "let $test := 'test'",
                "return $test",
                ";",
                "xquery version '1.0-ml';",
                "let $test := 'test'",
                "return $test"
            ),
            2
        );
        assertIssueLines(check, new int[]{1, 5});
    }    
    
    @Test
    public void testOldVersion() {
        checkValid(
            check,
            code(
                "xquery version '0.9-ml';",
                "let $test := 'test'",
                "return $test"
            )
        );
    }

    @Test
    public void testValid() {
        checkValid(
            check,
            code(
                "xquery version '1.0-ml';",
                "declare option xdmp:mapping 'false';",
                "let $test := 'test'",
                "return $test"
            )
        );
    }
}
