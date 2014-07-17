/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.checks;

import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.annotations.Test;

public class StrongTypingInFLWORCheckTest extends AbstractSonarTest {

    private final AbstractCheck check = new StrongTypingInFLWORCheck();

    @Test
    public void testMissingForType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for $article in /ldswebml[@type = \"article\"]",
                "let $title as xs:string := $article/search-meta/title",
                "return",
                "    $title"
            )
        );
        assertIssueLine(check, 2);
    }
    
    @Test
    public void testMissingLetType() {
        checkInvalid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for $article as element(ldswebml) in /ldswebml[@type = \"article\"]",
                "let $title := $article/search-meta/title",
                "return",
                "    $title"
            )
        );
        assertIssueLine(check, 3);
    }

    @Test
    public void testValidDeclaration() {
        checkValid(
            check,
            code(
                "xquery version \"1.0-ml\";",
                "for $article as element(ldswebml) in /ldswebml[@type = \"article\"]",
                "let $title as xs:string := $article/search-meta/title",
                "return",
                "    $title"
            )
        );
    }
}
