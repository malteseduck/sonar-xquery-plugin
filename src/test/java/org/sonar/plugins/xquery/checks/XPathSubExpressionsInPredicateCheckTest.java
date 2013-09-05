/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class XPathSubExpressionsInPredicateCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new XPathSubExpressionsInPredicateCheck();

    @Test
    public void testPathStep() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "/ldswebml[search-meta/title]"
            )
        );
        assertViolationLine(check, 2);
    }

    @Test
    public void testSubPredicate() {
        checkInvalid(
            check, 
            code(
                "xquery version \"1.0-ml\";",
                "/ldswebml[search-meta[title]]"
            )
        );
        assertViolationLine(check, 2);
    }

    @Test
    public void testValid() {
        checkValid(
            check, 
            code(
                "xquery version \"1.0-ml\";",
                "/article[title]"
            )
        );
    }
}
