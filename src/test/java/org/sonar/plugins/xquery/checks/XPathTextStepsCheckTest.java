/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class XPathTextStepsCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new XPathTextStepsCheck();

    @Test
    public void testAtomization() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[title eq 'Title']"
            )
        );
    }

    @Test
    public void testClosingElement() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "<textarea>Hi</textarea>"
            )
        );
    }
    
    @Test
    public void testFalsePositive() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml'",
                "fn:string(/article/text)"
            )
        );
    }
    
    @Test
    public void testInPredicate() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article[title/text() eq 'Title']"
            )
        );
        assertIssueLine(check, 2);
    }    

    @Test
    public void testInvalid() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "/article/title/text()"
            )
        );
        assertIssueLine(check, 2);
    }      
    
    @Test
    public void testInvalidMultiline() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml'",
                "xdmp:http-get(",
                "    fn:concat('http://', $current, ':8013/status/cluster-status.xqy?mode=performance')",
                ")[2]/ss:server-status/ss:server-name/text()"
            )
        );
        assertIssueLine(check, 4);
    }

    @Test
    public void testInvalidWithFalsePositive() {
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml'",
                "/article",
                "    /text",
                "    /nodes",
                "    /text()"
            )
        );
        assertIssueLine(check, 5);
    }

    @Test
    public void testNotFullyQualified() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "if (fn:empty($status)) then $status else ()"
            )
        );    
    }

    @Test
    public void testStringFunction() {
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "fn:string(/article/title)"
            )
        );
    }
}
