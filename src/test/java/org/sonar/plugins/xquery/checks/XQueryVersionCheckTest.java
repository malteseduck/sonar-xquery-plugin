/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class XQueryVersionCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new XQueryVersionCheck();

	@Test
	public void testInvalid() {
        checkInvalid(
            check, 
            code(
                "xquery version \"0.9-ml\"",
                "fn:current-dateTime()"
            )
        );
        assertViolationLine(check, 1);
	}

    @Test
    public void testInvalidMultiTransaction() {
        checkInvalid(
            check, 
            code(
                "fn:current-dateTime()",
                ";",
                "fn:current-dateTime()"                
            ),
            2
        );
        assertViolationLines(check, new int[] {1, 3});
    }	
	
	@Test
    public void testNoVersion() {
        checkInvalid(
            check, 
            code(
                "fn:current-dateTime()"
            )
        );
        assertViolationLine(check, 1);
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