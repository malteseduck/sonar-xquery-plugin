/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class DynamicFunctionCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new DynamicFunctionCheck();

    @Test
    public void testInvalid() {
        checkInvalid(
            check, 
            code(
                "xquery version \"1.0-ml\";",
                "let $date := xdmp:eval('fn:current-dateTime()')",
                "return",
                "    $date"
            )
        );
        assertIssueLine(check, 2);
    }

    @Test
    public void testValid() {
        checkValid(
            check, 
            code(
                "xquery version \"1.0-ml\";",
                "fn:current-dateTime()"
            )
        );
    }
}
