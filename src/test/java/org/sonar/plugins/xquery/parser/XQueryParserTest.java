/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser;

import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.sonar.plugins.xquery.AbstractSonarTest;
import org.sonar.plugins.xquery.parser.reporter.Problem;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class XQueryParserTest extends AbstractSonarTest {
        
    @Test
    public void testAttributesWithoutSpace() throws RecognitionException {
        log("testAttributesWithoutSpace():");
        try {
            parse(
                code(
                    "xquery version '1.0-ml';",
                    "<table bgcolor='blue'cellpadding='10'>"               
                )
            );
            Assert.fail("Should have gotten a syntax error");
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), " - line 2:34 - no viable alternative at input 'cellpadding'", "Error message");
        }
    }   
    
    @Test
    public void testBinaryConstructor() throws RecognitionException {
        log("testBinaryConstructor():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "binary { xdmp:base64-encode(xs:string('')) }"               
            )
        );
        Assert.assertEquals(tree.getValue("PathExpr"), "binary { UnaryExpr }", "Binary constructor");
    }    
    
    @Test
    public void testComment() throws RecognitionException {
        log("testComment():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "(: Current Date :)",               
                "fn:current-dateTime()"
            )
        );
        Assert.assertEquals(tree.getTextValue("QueryBody.FunctionCall.FunctionName.QName"), "fn:current-dateTime", "Function call name");
    }        

    @Test
    public void testCommentAtBeginning() throws RecognitionException {
        log("testCommentAtBeginning():");
        XQueryTree tree = parse(
            code(
                "(: Comment :)",
                "xquery version '1.0-ml';",
                "fn:current-dateTime()"
            )
        );
        Assert.assertEquals(tree.getValue("VersionDecl.VersionValue.StringLiteral"), "1.0-ml", "Version");
        Assert.assertEquals(tree.getTextValue("QueryBody.FunctionCall.FunctionName.QName"), "fn:current-dateTime", "Function call name");
    }        

    @Test
    public void testCommentInDeclaration() throws RecognitionException {
        log("testComment():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'test';",
                "declare function (:TEST:) test:add()",               
                "as item()*",
                "{",
                "    'nyi'",                
                "};"                
            )
        );
        Assert.assertEquals(tree.getTextValue("FunctionDecl.FunctionName.QName"), "test:add", "Function declaration name");
    }        
    
    // @Test - TODO: figure out how to support this 
    public void testCommentLiteral() throws RecognitionException {
        log("testCommentLiteral():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "'\\(:TEST:\\)',",
                "fn:current-dateTime()"               
            )
        );
        
        Assert.assertEquals(tree.getTextValue("FunctionCall.FunctionName.QName"), "fn:current-dateTime", "Function call name");
    }       
    
    @Test
    public void testCommentNotAtBeginning() throws RecognitionException {
        log("testCommentNotAtBeginning():");
        XQueryTree tree = parse(
            code(            	
                "xquery version '1.0-ml';",
                "",
                "fn:current-dateTime()",
                "(: Comment :)"         
            )
        );
        Assert.assertEquals(tree.getValue("VersionDecl.VersionValue.StringLiteral"), "1.0-ml", "Version");
        Assert.assertEquals(tree.getValue("QueryBody.FunctionCall.FunctionName.QName"), "fn : current-dateTime", "Function call name");
    }      
        
    @Test
    public void testComparison() throws RecognitionException {
        log("testComparison():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "2 gt 1"
            )
        );
        XQueryTree body = tree.find("QueryBody");
        Assert.assertNotNull(body);
        List<XQueryTree> nodes = tree.find("QueryBody").getChildren();
        Assert.assertTrue(nodes.size() == 3);
        Assert.assertEquals(nodes.get(0).getValue("UnaryExpr.PathExpr"), "2", "First expression");
        Assert.assertEquals(nodes.get(1).getText(), "gt", "Comparator");
        Assert.assertEquals(nodes.get(2).getValue("UnaryExpr.PathExpr"), "1", "Second expression");
    }      
    
    @Test
    public void testComputedElement() throws RecognitionException {
        log("testComputedElement():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "element html {",
                "    element body {",
                "        element b { 'Hello World!' }",
                "    }",
                "}"
            )
        );
        Assert.assertEquals(tree.getValue("UnaryExpr.UnaryExpr.UnaryExpr.StringLiteral"), "Hello World!", "Computed element content");
    }       
    
    @Test
    public void testComputedElementWithEntity() throws RecognitionException {
        log("testComputedElementWithEntity():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "element test {",
                "    element body {",
                "        '&nbsp;',",
                "        element b { 'Hello World!' }",
                "    }",
                "}"
            )
        );
        Assert.assertEquals(tree.getValue("UnaryExpr.UnaryExpr.UnaryExpr.StringLiteral"), "&nbsp;", "Computed element content");
    }       
    
    @Test
    public void testConditional() throws RecognitionException {
        log("testConditional():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable $test as xs:string? := 'test';",
                "if (fn:exists($test)) then $test else if ($test) then $test else ()"
            )
        );
        Assert.assertEquals(tree.getValue("IfExpr.IfPredicate.FunctionCall.FunctionName.QName"), "fn : exists", "If predicate function name");
        Assert.assertEquals(tree.getValue("IfExpr.IfThen.QName"), "test", "If 'then' expression variable name");
        Assert.assertEquals(tree.getValue("IfExpr.IfElse.IfExpr.IfPredicate.QName"), "test", "If else predicate variable name");
        Assert.assertNotNull(tree.find("IfExpr.IfElse.IfElse.UnaryExpr.ParenthesizedExpr"), "Final else return expression");
    }    
    
    @Test
    public void testDirectComment() throws RecognitionException {
        log("testDirectElement():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<!-- comment -->"
            )
        );
        Assert.assertEquals(tree.getValue("UnaryExpr.DirComConstructor"), "<!-- comment -->", "Direct comment");
    }    
    
    @Test
    public void testDirectElement() throws RecognitionException {
        log("testDirectElement():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<html>",
                "    <body>",
                "        <b>Hello World!</b>",
                "    </body>",
                "</html>"
            )
        );
        Assert.assertEquals(tree.getValue("DirElemContent.DirElemContent.DirElemContent"), "Hello World!", "Direct element content");
    }   
    
    @Test
    public void testDirectElementWithEntity() throws RecognitionException {
        log("testDirectElementWithEntity():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<html>",
                "    <body>",
                "        <b>&nbsp;Hello World!</b>",
                "    </body>",
                "</html>"
            )
        );
        Assert.assertEquals(tree.getValue("DirElemContent.DirElemContent.DirElemContent"), "&nbsp; Hello World!", "Direct element content");
    }  
    
    //  @Test - TODO: Figure out how to support this 
    public void testDoctypeDeclaration() throws RecognitionException {
        log("testDoctypeDeclaration():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "\"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\",",
                "<html xmlns='http://www.w3.org/1999/xhtml'>",
                "</html>"
            )
        );
        
        Assert.assertEquals(tree.getValue("QueryBody.StringLiteral"), "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>", "Doctype expression");
    } 
    
    @Test
    public void testEmbeddedExpression() throws RecognitionException {
        log("testEmbeddedExpression():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<span>{ $displayText }</span>"
            )
        );
        Assert.assertEquals(tree.getValue("DirElemContent.PathExpr.QName"), "displayText", "Embedded variable reference");
    } 
    
    @Test
    public void testEmbeddedExpressionWithEntity() throws RecognitionException {
        log("testEmbeddedExpressionWithEntity():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<span>{ $displayText }&nbsp;</span>"
            )
        );
        Assert.assertEquals(tree.getValue("DirElemContent.PathExpr.QName"), "displayText", "Embedded variable reference");
        Assert.assertEquals(tree.getValue("DirElemContent"), "{ UnaryExpr } &nbsp;", "Embedded content");
    }       
    
    @Test
    public void testEmptyString() throws RecognitionException {
        log("testEmptyString():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "''"
            )
        );
        // This used to expect a null for the empty string - have no idea why we did that
        Assert.assertEquals(tree.getValue("QueryBody.StringLiteral"), "", "Empty string");
    }    

    @Test
    public void testEntitiesInConcat() throws RecognitionException {
        log("testEntitiesInConcat():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:concat('http://www.lds.org/?locale=', $locale, '&amp;id=', $id)"
            )
        );
        XQueryTree arguments = tree.find("QueryBody.FunctionCall.ArgumentList");
        Assert.assertNotNull(arguments, "Arguments");
        Assert.assertEquals(arguments.getChildCount(), 4, "Number of arguments");        

        Assert.assertEquals(arguments.getValue("Argument.StringLiteral"), "http://www.lds.org/?locale=", "First string argument");
        Assert.assertEquals(arguments.getChild(2).getValue("StringLiteral"), "&amp; id=", "Third string argument");        
    }
    
    @Test
    public void testEntitiesInHtml() throws RecognitionException {
        log("testEntitiesInHtml():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<html><body>&lt;node&gt;&amp;</body></html>"
            )
        );
        Assert.assertEquals(tree.getValue("QueryBody.DirElemContent.DirElemContent"), "&lt; node &gt; &amp;", "Entity expression");
    }
    
    @Test
    public void testEntitiesInString() throws RecognitionException {
        log("testEntitiesInString():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "'&lt; , &gt; , &amp;',",
                "'&rdquo;',",
                "'&bdquo;',",
                "'&brvbar;',",
                "'&bull;',",
                "'&circ;',",
                "'&emsp;',",
                "'&ensp;',",
                "'&hellip;',",
                "'&iexcl;',",
                "'&iquest;',",
                "'&laquo;',",
                "'&ldquo;',",
                "'&lsaquo;',",
                "'&lsquo;',",
                "'&mdash;',",
                "'&nbsp;',",
                "'&ndash;',",
                "'&oline;',",
                "'&prime;',",
                "'&Prime;',",
                "'&raquo;',",
                "'&rdquo;',",
                "'&rsaquo;',",
                "'&rsquo;',",
                "'&sbquo;',",
                "'&thinsp;',",
                "'&tilde;',",
                "'&uml;'"                
            )
        );
        
        XQueryTree body = tree.find("QueryBody");
        Assert.assertNotNull(body, "Query body");
        Assert.assertEquals(body.getChildCount(), 29, "Body children");

        Assert.assertEquals(body.getChild(0).getValue("StringLiteral"), "&lt; , &gt; , &amp;", "Entity expression");
        Assert.assertEquals(body.getChild(1).getValue("StringLiteral"), "&rdquo;", "&rdquo; Entity");
        Assert.assertEquals(body.getChild(28).getValue("StringLiteral"), "&uml;", "&uml; Entity");
        Assert.assertEquals(body.getChild(23).getValue("StringLiteral"), "&rsaquo;", "&rsaquo; Entity");
    }

    @Test
    public void testFLOWR() throws RecognitionException {
        log("testFLOWR():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "let $id := xdmp:get-request-field('id')",
                "let $uri := concat('/published/devpedia/content/kb', $id, '.xml')",
                "let $delete := xdmp:document-delete($uri)",
                "where $id ne '1'",
                "return xdmp:redirect-response('/')"
            )        
        );
        
        XQueryTree flowr = tree.find("FLOWRExpr");
        Assert.assertNotNull(flowr, "FLOWR expression");
        Assert.assertEquals(flowr.getChildCount(), 5, "FLOWR expression clauses");
       
        Assert.assertEquals(flowr.getValue("LetClause.LetName.QName"), "id", "First let clause variable name");
        Assert.assertEquals(flowr.getValue("LetClause.LetBinding.FunctionCall.FunctionName.QName"), "xdmp : get-request-field", "First let clause binding function name");
        Assert.assertEquals(flowr.getValue("WhereClause.QName"), "id", "Where clause variable name");
        Assert.assertEquals(flowr.getValue("WhereClause.StringLiteral"), "1", "Where clause string");
        Assert.assertEquals(flowr.getValue("ReturnClause.FunctionCall.FunctionName.QName"), "xdmp : redirect-response", "Return clause function name");
    }

    @Test
    public void testFLOWRBindTypes() throws RecognitionException {
        log("testFLOWRBindTypes():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "for $article as element(article) in /article",
                "let $title as xs:string? := $article/title",
                "return $title"
            )
        );
        Assert.assertEquals(tree.getTypeValue("ForClause.ForType"), "element(article)", "For clause type");
        Assert.assertEquals(tree.getValue("LetClause.LetName.QName"), "title", "First let clause variable name");
        Assert.assertEquals(tree.getTypeValue("LetClause.LetType"), "xs:string", "First let clause variable type");
    }

    @Test
    public void testFLOWROrderBy() throws RecognitionException {
        log("testFLOWROrderBy():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "for $article in /article",
                "order by $article/title ascending, $article/sequence descending",
                "return $article"
            )
        );
        
        XQueryTree flwor = tree.find("FLOWRExpr");
        Assert.assertNotNull(flwor, "FLOWR expression");
        Assert.assertEquals(flwor.getChildCount(), 3, "FLOWR expression clauses");
       
        Assert.assertEquals(flwor.getValue("ForClause.ForName.QName"), "article", "For clause variable name");
        Assert.assertEquals(flwor.getValue("ForClause.ForBinding.PathExpr"), "/ QName", "For clause binding XPath expression");
        Assert.assertEquals(flwor.getValue("OrderByClause.OrderSpecList.OrderSpec.UnaryExpr.PathExpr"), "$ QName / QName", "First order specification expression");
        Assert.assertEquals(flwor.getValue("OrderByClause.OrderSpecList.OrderSpec.OrderModifier"), "ascending", "First order specification modifier");
        Assert.assertEquals(flwor.getValue("ReturnClause.QName"), "article", "Return clause variable name");
    }

    @Test
    public void testFLOWRReturnClause() throws RecognitionException {
        log("testFLOWRReturnClause():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "for $article in /article",
                "return (",
                "   xdmp:set-response-content-type( 'text/html' ),",
                "   <html xmlns='http://www.w3.org/1999/xhtml'>{",
                "       $article",
                "   }</html>",
                ")"
            )
        );
        
        XQueryTree flwor = tree.find("FLOWRExpr");
        Assert.assertNotNull(flwor, "FLOWR expression");
        Assert.assertEquals(flwor.getChildCount(), 2, "FLOWR expression clauses");
       
        XQueryTree returns = flwor.find("ReturnClause.ParenthesizedExpr");
        Assert.assertNotNull(returns, "Parenthesized expression root");
        Assert.assertEquals(returns.getChildCount(), 2, "Parenthesized expressions");
    }

    @Test
    public void testFunctionCall() throws RecognitionException {
        log("testFunctionCall():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "xdmp:eval('fn:current-dateTime()')"
            )
        );
        Assert.assertEquals(tree.getTextValue("FunctionCall.FunctionName.QName"), "xdmp:eval", "Function name");
        Assert.assertEquals(tree.getValue("FunctionCall.ArgumentList.Argument.UnaryExpr.StringLiteral"), "fn:current-dateTime()", "Function argument string");
    }     
        
    @Test
    public void testFunctionCallInXPath() throws RecognitionException {
        log("testFunctionCallInXPath():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article/title/fn:base-uri()"
            )
        );
        Assert.assertEquals(tree.getTextValue("PathExpr.FunctionCall.FunctionName.QName"), "fn:base-uri", "Function name");
    }

    @Test
    public void testFunctionCallNested() throws RecognitionException {
        log("testFunctionCallNested():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:substring-after(fn:substring-before($requestUrl, '.'), '?url=')"
            )
        );
        Assert.assertEquals(tree.getValue("FunctionCall.Argument.FunctionCall.FunctionName.QName"), "fn : substring-before", "Argument function name");
        
        XQueryTree arguments = tree.find("FunctionCall.ArgumentList");
        Assert.assertNotNull(arguments, "First function arguments");
        Assert.assertEquals(arguments.getChildCount(), 2, "First function number of arguments");
        Assert.assertEquals(arguments.getChild(1).getValue("StringLiteral"), "?url=", "First function second argument");
    }

    @Test
    public void testFunctionCallNoNamespaceInXPath() throws RecognitionException {
        log("testFunctionCallNoNamespaceInXPath():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article/title/base-uri()"
            )
        );
        Assert.assertEquals(tree.getValue("PathExpr.FunctionCall.FunctionName.QName"), "base-uri", "Function name");
    }
    
    @Test
    public void testFunctionCallParenthesized() throws RecognitionException {
        log("testFunctionCallParenthesized():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "( fn:substring-before($requestUrl, '.') )"
            )
        );
        Assert.assertEquals(tree.getValue("ParenthesizedExpr.FunctionCall.FunctionName.QName"), "fn : substring-before", "Argument function name");
        
        XQueryTree arguments = tree.find("ParenthesizedExpr.FunctionCall.ArgumentList");
        Assert.assertNotNull(arguments, "Function arguments");
        Assert.assertEquals(arguments.getChildCount(), 2, "Function number of arguments");
        Assert.assertEquals(arguments.getChild(1).getValue("StringLiteral"), ".", "Function second argument");
    }

    @Test
    public void testFunctionCallTextInXPath() throws RecognitionException {
        log("testFunctionCallNoNamespaceInXPath():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article/title/text()"
            )
        );
        // TODO: Doesn't recognize "text()" as a function?
//        Assert.assertEquals(tree.getValue("PathExpr.FunctionCall.FunctionName.QName"), "text", "Function name");
        Assert.assertEquals(tree.getValue("PathExpr"), "/ QName / QName / text ( )", "XPath with text() function");
    }

    @Test
    public void testFunctionCallWithEntity() throws RecognitionException {
        log("testFunctionCallParenthesized():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:substring-before($requestUrl, '&amp;')"
            )
        );
        Assert.assertEquals(tree.getValue("FunctionCall.FunctionName.QName"), "fn : substring-before", "Argument function name");

        XQueryTree arguments = tree.find("FunctionCall.ArgumentList");
        Assert.assertNotNull(arguments, "Function arguments");
        Assert.assertEquals(arguments.getChildCount(), 2, "Function number of arguments");
        Assert.assertEquals(arguments.getChild(1).getValue("StringLiteral"), "&amp;", "Function second argument");
    }

    @Test
    public void testFunctionCallWithNestedEntity() throws RecognitionException {
        log("testFunctionCallWithNestedEntity():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "if ($wordsUsed > $wordCount) then (",
                "    (",
                "        processFunctions:truncateText($words - ($wordsUsed - $wordCount), $node, fn:true()),",
                "        '&rdquo;'",
                "    )",
                ") else (",
                "    $node",
                ")"
            )
        );

        XQueryTree parenthesized = tree.find("IfExpr.IfThen.ParenthesizedExpr.ParenthesizedExpr");
        Assert.assertNotNull(parenthesized, "Nested parenthesized");
        Assert.assertEquals(parenthesized.getChildCount(), 2, "Nested parenthesized expressions");
        Assert.assertEquals(parenthesized.getChild(1).getValue("StringLiteral"), "&rdquo;", "Nested &rdquo; entity");
    }

    @Test
    public void testFunctionTypes() throws RecognitionException {
        log("testFunctionTypes():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare function local:add($a as xs:integer?, $b as xs:integer?)",
                "as xs:integer? {",
                "    $a + $b",
                "};",
                "local:add(1, 1)"
            )
        );

        Assert.assertEquals(tree.getTextValue("FunctionDecl.FunctionName.QName"), "local:add", "Function name");
        Assert.assertEquals(tree.getValue("FunctionDecl.ParamList.Param.ParamName.QName"), "a",
                "First param variable name");
        Assert.assertEquals(tree.getTypeValue("FunctionDecl.ParamList.Param.TypeDeclaration"), "xs:integer", "First param variable type");
        Assert.assertEquals(tree.getTypeValue("FunctionDecl.ReturnType"), "xs:integer", "Return type");
    }

    @Test
    public void testFunctionTypeBinaryReturn() throws RecognitionException {
        log("testFunctionTypeBinaryReturn():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare function local:buildZip($files as element(), $updatedFiles as element()) as binary() {",
                "    xdmp:filesystem-file($files/path)",
                "};",
                "local:getImage(<files><path>/test/image.jpg</path></files>, ())"
            )
        );
        
        Assert.assertEquals(tree.getTextValue("FunctionDecl.FunctionName.QName"), "local:buildZip", "Function name");
        Assert.assertEquals(tree.getValue("FunctionDecl.ParamList.Param.ParamName.QName"), "files", "First param variable name");        
        Assert.assertEquals(tree.getTypeValue("FunctionDecl.ParamList.Param.TypeDeclaration"), "element()", "First param variable type");        
        Assert.assertEquals(tree.getTypeValue("FunctionDecl.ReturnType"), "binary()", "Return type");
    }    
    
    // @Test - TODO: need to figure out how to support this
    public void testImportOnFirstLine() throws RecognitionException {
        log("testImportOnFirstLine():");
        XQueryTree tree = parse(
            code(
                "import module namespace feedback = 'http://lds.org/code/ldsorg/feedBackFunctions' at '/admin-ldsorg/feedback/modules/feedbackFunctions.xqy';",
                "fn:current-dateTime()"
            )
        );
        Assert.assertEquals(tree.getTextValue("ModuleImport.ModulePrefix"), "feedback", "Import module namespace name");
        Assert.assertEquals(tree.getTextValue("ModuleImport.ModuleNamespace.StringLiteral"), "http://lds.org/code/ldsorg/feedBackFuntions", "Import module namespace");
    }       
    
    @Test
    public void testKeywordNamespaceElement() throws RecognitionException {
        log("testKeywordNamespace():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare namespace if = 'http://lds.org/code/test';",               
                "element if:test {}"
            )
        );
        Assert.assertEquals(tree.getTextValue("PathExpr.QName"), "if:test", "Element name");
    }     
    
    // @Test - TODO: Figure out how to support this 
    public void testKeywordNamespaceFunctionCall() throws RecognitionException {
        log("testKeywordNamespaceFunctionCall():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "import module namespace if = 'http://lds.org/code/test' at '/test.xqy';",               
                "if:test()"
            )
        );
        Assert.assertEquals(tree.getTextValue("FunctionCall.FunctionName.QName"), "if:test", "Function name");
    }        
    
    @Test
    public void testModuleDeclaration() throws RecognitionException {
        log("testModuleDeclaration():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';"
            )
        );
        Assert.assertEquals(tree.getValue("ModuleDecl.ModulePrefix"), "test", "Delcaration prefix");
        Assert.assertEquals(tree.getTextValue("ModuleDecl.StringLiteral"), "http://lds.org/code/test", "Module namespace");
    }        

    @Test
    public void testModuleImport() throws RecognitionException {
        log("testModuleImport():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "import module namespace admin = 'http://marklogic.com/xdmp/admin' at '/MarkLogic/admin.xqy';",
                "()"
            )
        );
        Assert.assertEquals(tree.getValue("ModuleImport.ModulePrefix"), "admin", "Namepsace name");
        Assert.assertEquals(tree.getValue("ModuleImport.ModuleNamespace.StringLiteral"), "http://marklogic.com/xdmp/admin", "Namespace string");
        Assert.assertEquals(tree.getValue("ModuleImport.ModuleAtHints.StringLiteral"), "/MarkLogic/admin.xqy", "First namespace 'at hint'");
    }
    
    @Test
    public void testNamespaceAxis() throws RecognitionException {
        log("testNamespaceAxis():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "(/article/namespace::*)[. ne 'http://lds.org/code/test']"
            )
        );
        Assert.assertEquals(tree.getValue("QueryBody.ParenthesizedExpr.PathExpr"), "/ QName / namespace :: *", "XPath expression");
    }
    
    @Test
    public void testOldVersion() throws RecognitionException {
        log("testOldVersion():");
        XQueryTree tree = parse(
             code(
                 "xquery version '0.9-ml'",
                 "()"
             )
         );
        Assert.assertEquals(tree.getValue("VersionDecl.VersionValue.StringLiteral"), "0.9-ml", "Version");
    }             
  
    @Test
    public void testOptionDeclaration() throws RecognitionException {
        log("testOptionDeclaration():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare option xdmp:mapping 'true';",
                "()"
            )
        );
        Assert.assertEquals(tree.getValue("OrderedDecls.OptionDecl.QName"), "xdmp : mapping", "Option name");
        Assert.assertEquals(tree.getValue("OrderedDecls.OptionDecl.StringLiteral"), "true", "Option value");
    }             
 
    @Test
    public void testOrderedDeclarations() throws RecognitionException {
        log("testOptionDeclaration():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare option xdmp:mapping 'true';",
                "declare function local:add($a, $b) { $a + $b };",
                "()"
            )
        );
        
        XQueryTree decls = tree.find("OrderedDecls");
        Assert.assertNotNull(decls, "Ordered declarations");
        Assert.assertEquals(decls.getChildCount(), 2, "Number of ordered declarations");
        Assert.assertEquals(tree.getValue("OrderedDecls.OptionDecl.QName"), "xdmp : mapping", "Option name");
        Assert.assertEquals(tree.getValue("OrderedDecls.OptionDecl.StringLiteral"), "true", "Option value");
    }             

    @Test
    public void testPredicate() throws RecognitionException {
        log("testPredicate():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article[publication eq 'Ensign'][status eq 'published']"
            )
        );

        XQueryTree predicates = tree.find("PredicateList");
        Assert.assertNotNull(predicates, "Predicate list");
        Assert.assertEquals(predicates.getChildCount(), 2, "Predicates");
        
        Assert.assertEquals(predicates.getValue("Predicate.QName"), "publication", "First predicate variable name");
        Assert.assertEquals(predicates.getValue("Predicate.StringLiteral"), "Ensign", "First predicate string");
    }

    @Test
    public void testDoubleVbar() throws RecognitionException {
        log("testDoubleVbar():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "'hello' || ' world'"
            )
        );

        XQueryTree queryBody = tree.find("QueryBody");
        Assert.assertNotNull(queryBody, "Query body");
        Assert.assertEquals(queryBody.getValue(), "UnaryExpr || UnaryExpr");
    }

    @Test
    public void testDoubleVbarInFunctionCall() throws RecognitionException {
        log("testDoubleVbarInFunctionCall():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "xdmp:log('hello' || ' world')"
            )
        );

        XQueryTree functionCall = tree.find("FunctionCall");
        Assert.assertNotNull(functionCall, "Function call");
        Assert.assertEquals(functionCall.getTextValue("ArgumentList.Argument"), "UnaryExpr||UnaryExpr");
    }

    @Test
    public void testPrivateVariable() throws RecognitionException {
        log("testPrivateVariable():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare private variable $test as xs:string := 'test';",               
                "$test"
            )
        );
        Assert.assertEquals(tree.getTextValue("VarDecl.VarName.QName"), "test", "Variable name");
    }         
        
    @Test
    public void testPropertyAxis() throws RecognitionException {
        log("testPropertyAxis():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:doc('/test/bubba.xml')/property::prop:last-modified"
            )
        );
        Assert.assertEquals(tree.getTextValue("PathExpr"), "FunctionCall/property::QName", "Property axis");
        
        
    }      
    
    @Test
    public void testReporterErrors() throws RecognitionException {
        log("testReporterErrors():");
        ProblemReporter reporter = new ProblemReporter();
        reporter.setFailOnError(false);
        setReporter(reporter);
        parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable $year := try { xs:int('2000') } catch ($e) { };",               
                "$year"
            )
        );
        setReporter(null);
       
        List<Problem> problems = reporter.getProblems();
        
        Assert.assertNotNull(problems, "Reporter problems");
        Assert.assertEquals(problems.size(), 1, "Number of reporter problems");        
        Assert.assertEquals(problems.get(0).getLine(), 2, "Problem line number");               
    }    

    @Test
    public void testSelfAxis() throws RecognitionException {
        log("testSelfAxis():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "(: Current Date :)",               
                "(",
                "    for $text in ('iPhone','iPod')",
                "    return",
                "        fn:contains($userAgent, $text)",
                ")[self::node() eq fn:true()]"
            )
        );
        Assert.assertEquals(tree.getTextValue("Predicate.PathExpr"), "self::node()", "Self axis");               
    }     
    
    @Test
    public void testSequenceComparison() throws RecognitionException {
        log("testSequenceComparison():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "2 > (1, 2, 3)"
            )
        );
        XQueryTree body = tree.find("QueryBody");
        Assert.assertNotNull(body);
        List<XQueryTree> nodes = tree.find("QueryBody").getChildren();
        Assert.assertTrue(nodes.size() == 3);
        Assert.assertEquals(nodes.get(0).getValue("UnaryExpr.PathExpr"), "2", "First expression");
        Assert.assertEquals(nodes.get(1).getText(), ">", "Comparator");
        Assert.assertEquals(nodes.get(2).getValue("UnaryExpr.UnaryExpr.PathExpr"), "1", "Second expression");
    }     

    @Test
    public void testStepWithPeriod() throws RecognitionException {
        log("testStepWithPeriod():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "$articles/.[name eq 'bob']"
            )
        );
        Assert.assertEquals(tree.getValue("PathExpr.Predicate.QName"), "name", "Predicate QName");
    } 
    
    @Test
    public void testString() throws RecognitionException {
        log("testString():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "'HI'"
            )
        );
        Assert.assertEquals(tree.getValue("QueryBody.StringLiteral"), "HI", "Entity expression");
    }
    
    @Test
    public void testStringLiteralLineNumbers() throws RecognitionException {
        log("testStringLiteralLineNumbers():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "let $id := 'ids'",
                "",
                "",
                "",
                "return",
                "    $id"
            )
        );
        
        XQueryTree string = tree.find("LetClause.StringLiteral");
        Assert.assertNotNull(string, "String");
        Assert.assertEquals(string.getLine(), 2, "String line number");
    }
    
    @Test
    public void testTransactionalSeparator() throws RecognitionException {
        log("testTransactionalSeparator():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:current-dateTime()",
                ";",
                "xquery version '1.0-ml';",
                "fn:current-dateTime()"
            )
        );
        
        XQueryTree xquery = tree.find("XQuery");
        Assert.assertNotNull(xquery);
        List<XQueryTree> modules = xquery.getChildren();
        Assert.assertEquals(modules.size(), 2, "Module transactions");

        Assert.assertEquals(tree.getTextValue("QueryBody.FunctionCall.FunctionName.QName"), "fn:current-dateTime", "Function call name");
    }
    
    @Test
    public void testTryCatch() throws RecognitionException {
        log("testTryCatch():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "try {",
                "    fn:current-dateTime()",
                "} catch ($error) {",
                "   xdmp:log($error),",
                "   fn:current-date()",
                "}"
            )
        );
        
        Assert.assertEquals(tree.getValue("TryCatchExpr.TryClause.FunctionCall.FunctionName.QName"), "fn : current-dateTime", "Try expression function");    
        Assert.assertEquals(tree.getValue("TryCatchExpr.CatchClause.CatchError.QName"), "error", "Catch clause error list");    
        XQueryTree catchExpr = tree.find("TryCatchExpr.CatchClause.CatchExpr");
        Assert.assertNotNull(catchExpr, "Catch expression");
        Assert.assertEquals(catchExpr.getChildCount(), 2, "Catch expression count");    
        Assert.assertEquals(catchExpr.getChild(0).getValue("FunctionCall.FunctionName.QName"), "xdmp : log", "Catch expression log statement");    
    }

    @Test
    public void testTypeswitchLineNumbers() throws RecognitionException {
        log("testTypeswitchLineNumbers():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "typeswitch($address)",
                "    case $address",
                "        as element(USAddress)",
                "            return",
                "                1",
                "    default return 0"
            )
        );
        
        XQueryTree caseClause = tree.find("TypeswitchExpr.CaseClause");
        Assert.assertNotNull(caseClause, "Case clause");
        Assert.assertEquals(caseClause.getLine(), 3, "Case clause line number");    
    }
        
    @Test
    public void testTypeswitchNoVar() throws RecognitionException {
        log("testTypeswitchNoVar():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "typeswitch ( $contentItem )",
                "case element ( chapter ) return",
                "    xdmp:directory($context, 'infinity')/chapter",
                "        [",
                "            fn:starts-with(@fileID, $tokenizedId[1])",
                "            and fn:ends-with(@fileID, $tokenizedId[3])",
                "        ]/@locale",
                "default return ()"
            )
        );
        
        XQueryTree cases = tree.find("TypeswitchExpr.TypeswitchCases");
        Assert.assertNotNull(cases, "Typeswitch cases");
        Assert.assertEquals(cases.getChildCount(), 1, "Typeswitch case clauses");
    }             

    @Test
    public void testTypeswitchTypes() throws RecognitionException {
        log("testTypeswitchTypes():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "typeswitch($address)",
                "    case $address as element(USAddress) return 1",
                "    case $address as element(CanadaAddress) return 2",
                "    case $address as element(UKAddress) return 3",
                "    default return 0"
            )
        );
        Assert.assertEquals(tree.getValue("TypeswitchExpr.TypeswitchPredicate.UnaryExpr.QName"), "address", "Predicate name");
        Assert.assertEquals(tree.getValue("TypeswitchExpr.TypeswitchDefault.UnaryExpr.PathExpr"), "0", "Default return");
        
        XQueryTree cases = tree.find("TypeswitchExpr.TypeswitchCases");
        Assert.assertNotNull(cases, "Typeswitch cases");
        Assert.assertEquals(cases.getChildCount(), 3, "Typeswitch case clauses");

        Assert.assertEquals(tree.getValue("TypeswitchExpr.TypeswitchCases.CaseClause.CaseReturn.UnaryExpr.PathExpr"), "1", "First case return expression");
        Assert.assertEquals(tree.getValue("TypeswitchExpr.TypeswitchCases.CaseClause.CaseName.QName"), "address", "First case name");
        Assert.assertEquals(tree.getTypeValue("TypeswitchExpr.TypeswitchCases.CaseClause.CaseType"), "element(USAddress)", "First case type");
    }      
    
    @Test
    public void testVariableDeclaration() throws RecognitionException {
        log("testVariableDeclaration():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable $test as xs:string? := 'hello';",
                "$test"
            )
        );
        Assert.assertEquals(tree.getValue("VarDecl.VarName.QName"), "test", "Variable name");
        Assert.assertEquals(tree.getTypeValue("VarDecl.VarType"), "xs:string", "Variable type");
        Assert.assertEquals(tree.getValue("VarDecl.VarValue.StringLiteral"), "hello", "Variable value");
        Assert.assertEquals(tree.getValue("QueryBody.QName"), "test", "Variable reference");
    }

    @Test
    public void testVariableDeclarationEmpty() throws RecognitionException {
        log("testVariableDeclarationEmpty():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable $test as xs:string? := '';",
                "$test"
            )
        );
        Assert.assertEquals(tree.getValue("VarDecl.VarName.QName"), "test", "Variable name");
        Assert.assertEquals(tree.getTypeValue("VarDecl.VarType"), "xs:string", "Variable type");
        Assert.assertEquals(tree.getValue("VarDecl.VarValue.StringLiteral"), "", "Variable value");
        Assert.assertEquals(tree.getValue("QueryBody.QName"), "test", "Variable reference");
    }

    @Test
    public void testVariableDeclarationWithNull() throws RecognitionException {
        log("testVariableDeclarationWithNull():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable $null as xs:string := 'null';",
                "$null"
            )
        );
        Assert.assertEquals(tree.getValue("VarDecl.VarName.QName"), "null", "Variable name");
        Assert.assertEquals(tree.getTypeValue("VarDecl.VarType"), "xs:string", "Variable type");
        Assert.assertEquals(tree.getValue("VarDecl.VarValue.StringLiteral"), "null", "Variable value");
        Assert.assertEquals(tree.getValue("QueryBody.QName"), "null", "Variable reference");
    }

    @Test
    public void testVersion() throws RecognitionException {
        log("testVersion():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0';",
                "()"
            )
        );
        Assert.assertEquals(tree.getValue("VersionDecl.VersionValue.StringLiteral"), "1.0", "Version");
    }         

    @Test
    public void testVersionAfterNewline() throws RecognitionException {
        log("testVersionAfterNewline():");
        XQueryTree tree = parse(
            code(               
                "",
                "xquery version '1.0-ml';",
                "fn:current-dateTime()"
            )
        );
        Assert.assertEquals(tree.getValue("VersionDecl.VersionValue.StringLiteral"), "1.0-ml", "Version");
        Assert.assertEquals(tree.getValue("QueryBody.FunctionCall.FunctionName.QName"), "fn : current-dateTime", "Function call name");
    }

    @Test
    public void testVersionAfterSpace() throws RecognitionException {
        log("testVersionAfterSpace():");
        XQueryTree tree = parse(
            code(               
                " xquery version '1.0-ml';",
                "fn:current-dateTime()"
            )
        );
        Assert.assertEquals(tree.getValue("VersionDecl.VersionValue.StringLiteral"), "1.0-ml", "Version");
        Assert.assertEquals(tree.getValue("QueryBody.FunctionCall.FunctionName.QName"), "fn : current-dateTime", "Function call name");
    }
    
    @Test
    public void testXPathSteps() throws RecognitionException {
        log("testXPathSteps():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article//title"
            )
        );
        Assert.assertEquals(tree.getValue("QueryBody.PathExpr"), "/ QName // QName", "XPath expression");
    }      

    @Test
    public void testXPathText() throws RecognitionException {
        log("testXPathText():");
        XQueryTree tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article/title/text()"
            )
        );
        Assert.assertEquals(tree.getValue("QueryBody.PathExpr"), "/ QName / QName / text ( )", "XPath expression");
    }

    @Test
    public void testFunctionWithPrivateAnnotationForMarklogicVersion() throws RecognitionException {
        log("testFunctionWithPrivateAnnotationForMarklogicVersion():");
        XQueryTree tree = parse(
                code(
                        "xquery version '1.0-ml';",
                        "declare %private function local:add($a as xs:integer?, $b as xs:integer?)",
                        "as xs:integer? {",
                        "    $a + $b",
                        "};",
                        "local:add(1, 1)"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "private", "Annotation name");
    }

    @Test
    public void testFunctionWithAnyAnnotationForMarklogicVersion() throws RecognitionException {
        log("testFunctionWithAnyAnnotationForMarklogicVersion():");
        XQueryTree tree = parse(
                code(
                        "xquery version '1.0-ml';",
                        "declare %java:method(\"java.lang.StrictMath.copySign\") function local:add($a as xs:integer?, $b as xs:integer?)",
                        "as xs:integer? {",
                        "    $a + $b",
                        "};",
                        "local:add(1, 1)"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "java : method", "Annotation name");
    }

    @Test
    public void testFunctionWithPrivateAnnotationForXQuery30() throws RecognitionException {
        log("testFunctionWithPrivateAnnotationForXQuery30():");
        XQueryTree tree = parse(
                code(
                        "xquery version '3.0';",
                        "declare %private function local:add($a as xs:integer?, $b as xs:integer?)",
                        "as xs:integer? {",
                        "    $a + $b",
                        "};",
                        "local:add(1, 1)"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "private", "Annotation name");
    }

    @Test
    public void testFunctionWithAnyAnnotationForXQuery30() throws RecognitionException {
        log("testFunctionWithAnyAnnotationForXQuery30():");
        XQueryTree tree = parse(
                code(
                        "xquery version '3.0';",
                        "declare %java:method(\"java.lang.StrictMath.copySign\") function local:add($a as xs:integer?, $b as xs:integer?)",
                        "as xs:integer? {",
                        "    $a + $b",
                        "};",
                        "local:add(1, 1)"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "java : method", "Annotation name");
    }

    @Test
    public void testVariableWithPrivateAnnotationForMarklogicVersion() throws RecognitionException {
        log("testVariableWithPrivateAnnotationForMarklogicVersion():");
        XQueryTree tree = parse(
                code(
                        "xquery version '1.0-ml';",
                        "declare %private variable $test as xs:string := 'test';",
                        "$test"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "private", "Annotation name");
    }

    @Test
    public void testVariableWithAnyAnnotationForMarklogicVersion() throws RecognitionException {
        log("testVariableWithPrivateAnnotationForMarklogicVersion():");
        XQueryTree tree = parse(
                code(
                        "xquery version '1.0-ml';",
                        "declare %eg:volatile variable $test as xs:string := 'test';",
                        "$test"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "eg : volatile", "Annotation name");
    }

    @Test
    public void testVariableWithPrivateAnnotationForXQuery30() throws RecognitionException {
        log("testVariableWithPrivateAnnotationForXQuery30():");
        XQueryTree tree = parse(
                code(
                        "xquery version '3.0';",
                        "declare %private variable $test as xs:string := 'test';",
                        "$test"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "private", "Annotation name");
    }

    @Test
    public void testVariableWithAnyAnnotationForXQuery30() throws RecognitionException {
        log("testVariableWithPrivateAnnotationForXQuery30():");
        XQueryTree tree = parse(
                code(
                        "xquery version '3.0';",
                        "declare %eg:volatile variable $test as xs:string := 'test';",
                        "$test"
                )
        );

        Assert.assertEquals(tree.getValue("OrderedDecls.QName"), "eg : volatile", "Annotation name");
    }
}
