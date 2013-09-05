/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class XPathDecendantStepsCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new XPathDecendantStepsCheck();
    
    @Test
    public void testInvalid() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml'",
                "/article//title"
            )
        );
        assertViolationLine(check, 2);
    }

    @Test
    public void testInvalidMultiline() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml'",
                "xdmp:http-get(",
                "    fn:concat('http://', $current, ':8013/status/cluster-status.xqy?mode=performance')",
                ")[2]//ss:server-status/ss:server-name"
            )
        );
        assertViolationLine(check, 4);
    }

    @Test
    public void testMultipleInvalid() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml'",
                "$sample//status[",
                "    @active eq 'true'",
                "]//ss:server-status/ss:server-name"
            ),
            2
        );
        assertViolationLines(check, new int[] { 2, 4 });
    }    
    
    @Test
    public void testValid() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article/title"
            )
        );
    }
    
}
