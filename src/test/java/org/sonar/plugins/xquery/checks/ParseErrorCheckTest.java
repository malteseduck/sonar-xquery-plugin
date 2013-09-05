/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.testng.annotations.Test;

public class ParseErrorCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new ParseErrorCheck();

    @Test
    public void testInvalid() {
        ProblemReporter reporter = new ProblemReporter();
        reporter.setFailOnError(false);
        setReporter(reporter);
        checkInvalid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "declare variable $year := try { xs:int('2000') } catch ($e) { };",               
                "$year"
            )
        );
        setReporter(null);
        assertViolationLine(check, 2);
    }

    @Test
    public void testFalsePositive() {
        ProblemReporter reporter = new ProblemReporter();
        reporter.setFailOnError(false);
        setReporter(reporter);
        checkValid(
            check, 
            code(
                "xquery version '1.0-ml';",
                "\"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\",",
                "'hi'"
            )
        );
        setReporter(null);
    }
    
    
    @Test
    public void testValid() {
        ProblemReporter reporter = new ProblemReporter();
        reporter.setFailOnError(false);
        setReporter(reporter);
        checkValid(
            check, 
            code(
                "xquery version \"1.0-ml\";",
                "fn:current-dateTime()"
            )
        );
        setReporter(null);
    }
}
