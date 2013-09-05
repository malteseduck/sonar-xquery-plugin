/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.xquery.AbstractSonarTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class XqueryLineCountParserTest extends AbstractSonarTest {
    
    @Test
    public void testComments() {
        SourceCode sourceCode = 
            code(
                "xquery version '1.0-ml';",
                "",
                "(: This is the current date :)",
                "fn:current-dateTime()"
            );
        
        new XQueryLineCountParser(sourceCode).count();
        
        Assert.assertEquals(sourceCode.getMeasure(CoreMetrics.COMMENT_LINES).getValue(), 1.0);
    }

//    @Test
//    public void testEmbeddedCommentsBefore() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "(: This is the current date :) fn:current-dateTime()"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.COMMENT_LINES, 0);
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 2);            
//        }};
//    }
//    
//    @Test
//    public void testEmbeddedCommentsAfter() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "fn:current-dateTime() (: This is the current date :)"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.COMMENT_LINES, 0);
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 2);
//        }};
//    }
//    
//    @Test
//    public void testMultiline() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "(:~ ",
//                "    Displays the current date, minus an interval",
//                "",
//                "    @param $interval The interval to subtract",
//                "    @returns A dateTime of the resultant math",
//                ":)",
//                "declare function local:subtract($date as xs:dateTime()?, $interval as xs:dayTimeDuration()?)",
//                "as xs:dateTime()?",
//                "{",
//                "    $date - $interval",
//                "};",
//                "",
//                "local:subtract(fn:current-dateTime(), xs:dayTimeDuration('PT3D'))"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.COMMENT_LINES, 5);
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 7);
//            sourceCode.addMeasure(CoreMetrics.LINES, 15);
//        }};
//    }
//
//    @Test
//    public void testMethods() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "declare function local:subtract($date as xs:dateTime()?, $interval as xs:dayTimeDuration()?) as xs:dateTime()? {()};",
//                "declare function local:subtracter($date as xs:dateTime()?, $interval as xs:dayTimeDuration()?) as xs:dateTime()? {()};",
//                "declare function local:subtracterest($date as xs:dateTime()?, $interval as xs:dayTimeDuration()?) as xs:dateTime()? {()};",
//                "",
//                "declare function local:subtracterestier($date as xs:dateTime()?, $interval as xs:dayTimeDuration()?) as xs:dateTime()? {()};",
//                "",
//                "local:subtract(fn:current-dateTime(), xs:dayTimeDuration('PT3D'))"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 6);
//            sourceCode.addMeasure(CoreMetrics.FUNCTIONS, 4);
//            sourceCode.addMeasure(CoreMetrics.LINES, 9);
//        }};
//    }
//    
//    @Test
//    public void testTests() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "declare function (:TEST:) testSubtract() as xs:item()* {()};",
//                "declare function helper() as xs:integer* {()};",
//                "",
//                "declare function (:TEST:) testMultiply() as xs:item()* {()};"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 4);
//            sourceCode.addMeasure(CoreMetrics.FUNCTIONS, 3);
////            sourceCode.addMeasure(CoreMetrics.TESTS, 2);
//            sourceCode.addMeasure(CoreMetrics.LINES, 6);
//        }};
//    }
//    
//    @Test
//    public void testLines() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "fn:current-dateTime()"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.LINES, 2);
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 2);
//        }};
//    }
//    
//    @Test
//    public void testLinesWithBlanks() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "fn:current-dateTime()"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.LINES, 3); 
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 2); 
//        }};
//    }
//
//    @Test
//    public void testCodeLines() {
//        new NonStrictExpectations() {{
//            sourceCode.getCode(); result = code(
//                "xquery version '1.0-ml';",
//                "",
//                "fn:current-dateTime(),",
//                "",
//                "fn:current-date()"
//            );
//        }};        
//        
//        new XQueryLineCountParser(sourceCode).count();
//        
//        new Verifications() {{
//            sourceCode.addMeasure(CoreMetrics.LINES, 5); 
//            sourceCode.addMeasure(CoreMetrics.NCLOC, 3); 
//        }};
//    }
}
