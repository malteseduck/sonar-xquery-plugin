/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: cieslinskice
 * Date: 10/25/12
 * Time: 2:17 PM
 */
public class LogCheckTest extends AbstractSonarTest{

    private final AbstractCheck check = new LogCheck();

    @Test
    public void testInvalid() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "xdmp:log('We should not have this')"
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