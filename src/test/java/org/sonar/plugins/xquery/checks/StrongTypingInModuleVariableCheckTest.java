/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class StrongTypingInModuleVariableCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new StrongTypingInModuleVariableCheck();

    @Test
    public void testMissingType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "declare variable $a := 2;",
                "$a"
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
                "declare variable $a as xs:integer := 2;",
                "$a"
            )
        );
    }
}
