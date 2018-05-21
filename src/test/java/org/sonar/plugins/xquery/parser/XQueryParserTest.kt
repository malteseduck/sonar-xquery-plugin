/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser

import org.antlr.runtime.RecognitionException
import org.antlr.v4.runtime.ParserRuleContext
import org.assertj.core.api.Assertions.assertThat
import org.sonar.plugins.xquery.AbstractSonarTest
import org.sonar.plugins.xquery.parser.XQueryParser.*
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class XQueryParserTest : AbstractSonarTest() {

    @BeforeTest
    fun before() {
//        trace()
    }

    //    @Test
    @Throws(RecognitionException::class)
    fun testAttributesWithoutSpace() {
        log("testAttributesWithoutSpace():")
        try {
            parse(
                code(
                    "xquery version '1.0-ml';",
                    "<table bgcolor='blue'cellpadding='10'>"
                )
            )
            Assert.fail("Should have gotten a syntax error")
        } catch (e: RuntimeException) {
            assertEquals(e.message, " - line 2:34 - no viable alternative at input 'cellpadding'", "Error message")
        }
    }

    @Test
    @Throws(RecognitionException::class)
    fun testBinaryConstructor() {
        log("testBinaryConstructor():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "binary { xdmp:base64-encode(xs:string('')) }"
            )
        )
        val constructor: BinaryConstructorContext = tree.find()
    }

    @Test
    @Throws(RecognitionException::class)
    fun testComment() {
        log("testComment():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "(: Current Date :)",
                "fn:current-dateTime()"
            )
        )
        val functionName: FunctionNameContext = tree.find()
        assertThat(functionName).isNotNull
        assertThat(functionName.text).isEqualTo("fn:current-dateTime")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testCommentAtBeginning() {
        log("testCommentAtBeginning():")
        val tree = parse(
            code(
                "(: Comment :)",
                "xquery version '1.0-ml';",
                "fn:current-dateTime()"
            )
        )

        val version: VersionDeclContext = tree.find()
        val functionName: FunctionNameContext = tree.find()

        assertNotNull(version, "version")
        assertEquals(version.version.text, "'1.0-ml'", "version")
        assertNotNull(functionName, "function name")
        assertEquals(functionName.text, "fn:current-dateTime", "function call name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testCommentInDeclaration() {
        log("testComment():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'test';",
                "declare function (:TEST:) test:add()",
                "as item()*",
                "{",
                "    'nyi'",
                "};"
            )
        )
        val functionName: FunctionNameContext = tree.find()
        assertNotNull(functionName, "functionName")
        assertEquals(functionName.text, "test:add", "Function declaration name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testCommentLiteral() {
        log("testCommentLiteral():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "'\\(:TEST:\\)',",
                "fn:current-dateTime()"
            )
        )

        val functionName: FunctionNameContext = tree.find()
        assertNotNull(functionName, "functionName")
        assertEquals(functionName.text, "fn:current-dateTime", "function call name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testCommentNotAtBeginning() {
        log("testCommentNotAtBeginning():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "",
                "fn:current-dateTime()",
                "(: Comment :)"
            )
        )
        val version: VersionDeclContext = tree.find()
        val functionName: FunctionNameContext = tree.find()

        assertNotNull(version, "version")
        assertEquals(version.version.text, "'1.0-ml'", "Version")
        assertNotNull(functionName, "function name")
        assertEquals(functionName.text, "fn:current-dateTime", "Function call name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testComparison() {
        log("testComparison():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "2 gt 1"
            )
        )
        val compare: ComparisonContext = tree.find()

        val nodes = compare.children
        Assert.assertEquals(nodes?.size, 3, "child nodes")
        assertEquals(compare.l.findText<IntegerContext>(), "2", "First expression")
        assertEquals(compare.op.text, "gt", "Comparator")
        assertEquals(compare.r.findText<IntegerContext>(), "1", "Second expression")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testComputedElement() {
        log("testComputedElement():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "element html {",
                "    element body {",
                "        element b { 'Hello World!' }",
                "    }",
                "}"
            )
        )
        val literal: StringLiteralContext = tree.findIn(
            ElementConstructorContext::class,
            ElementConstructorContext::class,
            ElementConstructorContext::class
        )
        assertEquals(literal.unquotedText(), "Hello World!", "Computed element content")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testComputedElementWithEntity() {
        log("testComputedElementWithEntity():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "element test {",
                "    element body {",
                "        '&nbsp;',",
                "        element b { 'Hello World!' }",
                "    }",
                "}"
            )
        )
        val literal: StringLiteralContext = tree.findIn(
            ElementConstructorContext::class,
            ElementConstructorContext::class
        )
        assertEquals(literal.unquotedText(), "&nbsp;", "Computed element content")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testConditional() {
        log("testConditional():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable \$test as xs:string? := 'test';",
                "if (fn:exists(\$test)) then \$test else if (\$test) then \$test else ()"
            )
        )
        val ifExpr: IfExprContext = tree.find()

        assertEquals(ifExpr.conditionExpr.findText<FunctionNameContext>(), "fn:exists", "If predicate function name")
        assertEquals(ifExpr.thenExpr.findText<QNameContext>(), "test", "If 'then' expression variable name")
        assertEquals(ifExpr.elseExpr.ifExpr().conditionExpr.findText<QNameContext>(), "test", "If else predicate variable name")
        assertEquals(ifExpr.elseExpr.ifExpr().elseExpr.findText<ParenthesizedExprContext>(), "()", "Final else return expression")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testDirectComment() {
        log("testDirectElement():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<!-- comment -->"
            )
        )
    }

    @Test
    @Throws(RecognitionException::class)
    fun testDirectElement() {
        log("testDirectElement():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<html>",
                "    <body>",
                "        <b>Hello World!</b>",
                "    </body>",
                "</html>"
            )
        )
        val content: DirElemConstructorOpenCloseContext = tree.findIn(
            DirElemConstructorOpenCloseContext::class,
            DirElemConstructorOpenCloseContext::class
        )
        assertEquals(content.dirElemContent().childText(), "HelloWorld!", "Direct element content")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testDirectElementWithEntity() {
        log("testDirectElementWithEntity():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<html>",
                "    <body>",
                "        <b>&nbsp;Hello World!</b>",
                "    </body>",
                "</html>"
            )
        )
        val content: DirElemConstructorOpenCloseContext = tree.findIn(
            DirElemConstructorOpenCloseContext::class,
            DirElemConstructorOpenCloseContext::class
        )
        // TODO: Do we really need to support whitespace here?  Are there checks we need to be doing?
        assertEquals(content.dirElemContent().childText(), "&nbsp;HelloWorld!", "Direct element content")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testDoctypeDeclaration() {
        log("testDoctypeDeclaration():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "\"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\",",
                "<html xmlns='http://www.w3.org/1999/xhtml'>",
                "</html>"
            )
        )

        val literal: StringLiteralContext = tree.findIn(QueryBodyContext::class)
        assertEquals(literal.unquotedText(), "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>", "Doctype expression")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testEmbeddedExpression() {
        log("testEmbeddedExpression():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<span>{ \$displayText }</span>"
            )
        )
        val qName: QNameContext = tree.findIn(DirElemContentContext::class)
        assertEquals(qName.text, "displayText", "Embedded variable reference")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testEmbeddedExpressionWithEntity() {
        log("testEmbeddedExpressionWithEntity():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<span>{ \$displayText }&nbsp;</span>"
            )
        )
        val qName: QNameContext = tree.findIn(DirElemContentContext::class)
        assertEquals(qName.text, "displayText", "Embedded variable reference")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testEmptyString() {
        log("testEmptyString():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "''"
            )
        )
        val literal: StringLiteralContext = tree.findIn(QueryBodyContext::class)
        assertEquals(literal.text, "''", "Empty string")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testEntitiesInConcat() {
        log("testEntitiesInConcat():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:concat('http://www.lds.org/?locale=', \$locale, '&amp;id=', \$id)"
            )
        )
        val arguments: ArgumentListContext = tree.find()
        assertThat(arguments.argument()).hasSize(4)
        assertThat(arguments.argument(0).text).`as`("First string argument").isEqualTo("'http://www.lds.org/?locale='")
        assertThat(arguments.argument(2).text).`as`("Third string argument").isEqualTo("'&amp;id='")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testEntitiesInHtml() {
        log("testEntitiesInHtml():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "<html><body>&lt;node&gt;&amp;</body></html>"
            )
        )

        val elem: DirElemConstructorOpenCloseContext = tree.findIn(
            DirElemConstructorOpenCloseContext::class
        )
        assertEquals(elem.dirElemContent().childText(), "&lt;node&gt;&amp;", "Entity expression")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testEntitiesInString() {
        log("testEntitiesInString():")
        val tree = parse(
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
        )

        val body: ExprContext = tree.findIn(QueryBodyContext::class)

        val children = body.children.filter { it is ParserRuleContext } as List<ParserRuleContext>
        assertThat(children).hasSize(29)

        assertEquals(children[0].text, "'&lt; , &gt; , &amp;'", "Entity expression")
        assertEquals(children[1].text, "'&rdquo;'", "&rdquo; Entity")
        assertEquals(children[28].text, "'&uml;'", "&uml; Entity")
        assertEquals(children[23].text, "'&rsaquo;'", "&rsaquo; Entity")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFLOWR() {
        log("testFLOWR():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "let \$id := xdmp:get-request-field('id')",
                "let \$uri := concat('/published/devpedia/content/kb', \$id, '.xml')",
                "let \$delete := xdmp:document-delete(\$uri)",
                "where \$id ne '1'",
                "return xdmp:redirect-response('/')"
            )
        )

        val flowr: FlworExprContext = tree.find()

        assertEquals(flowr.letClause()[0].letVar.name.text, "id", "First let clause variable name")
        assertEquals(flowr.letClause()[0].letVar.findText<FunctionNameContext>(), "xdmp:get-request-field", "First let clause binding function name")
        assertEquals(flowr.whereExpr.findText<QNameContext>(), "id", "Where clause variable name")
        assertEquals(flowr.whereExpr.findText<StringLiteralContext>(), "'1'", "Where clause string")
        assertEquals(flowr.returnExpr.findText<FunctionNameContext>(), "xdmp:redirect-response", "Return clause function name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFLOWRBindTypes() {
        log("testFLOWRBindTypes():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "for \$article as element(article) in /article",
                "let \$title as xs:string? := \$article/title",
                "return \$title"
            )
        )

        val flowr: FlworExprContext = tree.find()

        assertEquals(flowr.forClause()[0].forVar.typeText(), "element(article)", "For clause type")
        assertEquals(flowr.letClause()[0].letVar.name.text, "title", "First let clause variable name")
        assertEquals(flowr.letClause()[0].letVar.typeText(), "xs:string", "First let clause variable type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFLOWROrderBy() {
        log("testFLOWROrderBy():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "for \$article in /article",
                "order by \$article/title ascending, \$article/sequence descending",
                "return \$article"
            )
        )

        val flowr: FlworExprContext = tree.find()

        assertEquals(flowr.forClause(0).forVar.name.text, "article", "For clause variable name")
        assertEquals(flowr.orderByClause().orderSpec(0).value.text, "\$article/title", "First order specification expression")
        assertEquals(flowr.orderByClause().orderSpec(1).order.text, "descending", "Second order specification modifier")
        assertEquals(flowr.returnExpr.findText<QNameContext>(), "article", "Return clause variable name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionCall() {
        log("testFunctionCall():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "xdmp:eval('fn:current-dateTime()')"
            )
        )

        val functionCall: FunctionCallContext = tree.find()

        assertEquals(functionCall.functionName().text, "xdmp:eval", "Function name")
        assertEquals(functionCall.argumentList().argument(0).findText<StringLiteralContext>(), "'fn:current-dateTime()'", "Function argument string")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionCallInXPath() {
        log("testFunctionCallInXPath():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article/title/fn:base-uri()"
            )
        )
        val functionCall: FunctionCallContext = tree.find()
        assertEquals(functionCall.functionName().text, "fn:base-uri", "Function name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionCallNested() {
        log("testFunctionCallNested():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:substring-after(fn:substring-before(\$requestUrl, '.'), '?url=')"
            )
        )
        val functionCall: FunctionCallContext = tree.findIn(FunctionCallContext::class, ArgumentListContext::class)

        assertEquals(functionCall.functionName().text, "fn:substring-before", "Argument function name")

        val arguments = functionCall.argumentList().argument()
        assertEquals(arguments.size, 2, "Second function number of arguments")
        assertEquals(arguments[1].findText<StringLiteralContext>(), "'.'", "Second function second argument")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionCallWithEntity() {
        log("testFunctionCallWithEntity():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:substring-before(\$requestUrl, '&amp;')"
            )
        )

        val funcCall: FunctionCallContext = tree.find()

        assertEquals(funcCall.functionName().text, "fn:substring-before", "Function name")
        assertEquals(funcCall.argumentList().argument(1).findText<StringLiteralContext>(), "'&amp;'", "Function second arguments")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionTypeBinaryReturn() {
        log("testFunctionTypeBinaryReturn():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare function local:buildZip(\$files as element(), \$updatedFiles as element()) as binary() {",
                "    xdmp:filesystem-file(\$files/path)",
                "};",
                "local:getImage(<files><path>/test/image.jpg</path></files>, ())"
            )
        )

        val funcDecl: FunctionDeclContext = tree.find()

        assertEquals(funcDecl.functionName().text, "local:buildZip", "Function name")
        assertEquals(funcDecl.sequenceType().itemType().text, "binary()", "Return type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testImportOnFirstLine() {
        log("testImportOnFirstLine():")
        val tree = parse(
            code(
                "import module namespace feedback = 'http://lds.org/code/ldsorg/feedBackFunctions' at '/admin-ldsorg/feedback/modules/feedbackFunctions.xqy';",
                "fn:current-dateTime()"
            )
        )
        val import: ModuleImportContext = tree.find()

        assertEquals(import.prefix.text, "feedback", "Import module namespace name")
        assertEquals(import.nsURI.unquotedText(), "http://lds.org/code/ldsorg/feedBackFunctions", "Import module namespace")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testKeywordNamespaceElement() {
        log("testKeywordNamespace():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare namespace if = 'http://lds.org/code/test';",
                "element if:test {}"
            )
        )
        val elem: ElementConstructorContext = tree.find()
        assertEquals(elem.elementName.text, "if:test", "Element name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testKeywordNamespaceFunctionCall() {
        log("testKeywordNamespaceFunctionCall():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "import module namespace if = 'http://lds.org/code/test' at '/test.xqy';",
                "if:test()"
            )
        )

        val funcCall: FunctionCallContext = tree.find()
        assertEquals(funcCall.functionName().text, "if:test", "Function name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testModuleDeclaration() {
        log("testModuleDeclaration():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';"
            )
        )
        val modDecl: ModuleDeclContext = tree.find()

        assertEquals(modDecl.prefix.text, "test", "Delcaration prefix")
        assertEquals(modDecl.uri.unquotedText(), "http://lds.org/code/test", "Module namespace")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testModuleImport() {
        log("testModuleImport():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "import module namespace admin = 'http://marklogic.com/xdmp/admin' at '/MarkLogic/admin.xqy';",
                "()"
            )
        )

        val modImport: ModuleImportContext = tree.find()

        assertEquals(modImport.prefix.text, "admin", "Namepsace name")
        assertEquals(modImport.nsURI.unquotedText(), "http://marklogic.com/xdmp/admin", "Namespace string")
        assertEquals(modImport.locations[0].unquotedText(), "/MarkLogic/admin.xqy", "First namespace 'at hint'")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testNamespaceAxis() {
        log("testNamespaceAxis():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "(/article/namespace::*)[. ne 'http://lds.org/code/test']"
            )
        )
        val forwardExpr: ForwardAxisContext = tree.findIn(
            ParenthesizedExprContext::class,
            StepExprContext::class,
            ForwardStepContext::class
        )

        assertEquals(forwardExpr.text, "namespace::", "XPath expression")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testOptionDeclaration() {
        log("testOptionDeclaration():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare option xdmp:mapping 'true';",
                "()"
            )
        )
        var optionDecl: OptionDeclContext = tree.find()

        assertEquals(optionDecl.name.text, "xdmp:mapping", "Option name")
        assertEquals(optionDecl.value.unquotedText(), "true", "Option value")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testPredicate() {
        log("testPredicate():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "/article[publication eq 'Ensign'][status eq 'published']"
            )
        )

        val predicates: PredicateListContext = tree.find()

        assertEquals(predicates.predicate(0).findText<QNameContext>(), "publication", "First predicate variable name")
        assertEquals(predicates.predicate(0).findText<StringLiteralContext>(), "'Ensign'", "First predicate string")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testPrivateVariable() {
        log("testPrivateVariable():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare %private variable \$test as xs:string := 'test';",
                "\$test"
            )
        )
        val varDecl: VarDeclContext = tree.find()
        assertEquals(varDecl.name.text, "test", "Variable name")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testPropertyAxis() {
        log("testPropertyAxis():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:doc('/test/bubba.xml')/property::prop:last-modified"
            )
        )
        val forAxis: ForwardAxisContext = tree.find()
        assertEquals(forAxis.text, "property::", "Property axis")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testSelfAxis() {
        log("testSelfAxis():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "(: Current Date :)",
                "(",
                "    for \$text in ('iPhone','iPod')",
                "    return",
                "        fn:contains(\$userAgent, \$text)",
                ")[self::node() eq fn:true()]"
            )
        )
        val predicate: PredicateContext = tree.find()
        assertEquals(predicate.findText<ForwardAxisContext>(), "self::", "Self axis")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testSequenceComparison() {
        log("testSequenceComparison():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "2 > (1, 2, 3)"
            )
        )

        val sequence: ParenthesizedExprContext = tree.findIn(ComparisonContext::class)
        assertThat(sequence.text).isEqualTo("(1,2,3)")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testStepWithPeriod() {
        log("testStepWithPeriod():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "\$articles/.[name eq 'bob']"
            )
        )
        val predicate: PredicateContext = tree.find()
        assertEquals(predicate.expr.findText<QNameContext>(), "name", "Predicate QName")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testStringLiteralLineNumbers() {
        log("testStringLiteralLineNumbers():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "let \$id := 'ids'",
                "",
                "",
                "",
                "return",
                "    \$id"
            )
        )

        val string: StringLiteralContext = tree.findIn(LetClauseContext::class)
        assertEquals(string.getLine(), 2, "String line number")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testTransactionalSeparator() {
        log("testTransactionalSeparator():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "fn:current-dateTime()",
                ";",
                "xquery version '1.0-ml';",
                "fn:current-date()"
            )
        )

        val module: ModuleContext = tree.find()
        assertThat(module.transactions).hasSize(2)
        assertThat(module.transactions[1].findText<FunctionCallContext>()).isEqualTo("fn:current-date()")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testTryCatch() {
        log("testTryCatch():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "try {",
                "    fn:current-dateTime()",
                "} catch (\$error) {",
                "   xdmp:log(\$error),",
                "   fn:current-date()",
                "}"
            )
        )

        val tryCatchExpr: TryCatchExprContext = tree.find()
        assertThat(tryCatchExpr.tryClause().findText<FunctionCallContext>()).isEqualTo("fn:current-dateTime()")
        assertThat(tryCatchExpr.catchClause(0).findText<QNameContext>()).isEqualTo("error")
        assertThat(tryCatchExpr.catchClause(0).enclosedExpr().findText<FunctionNameContext>()).isEqualTo("xdmp:log")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testTypeswitchLineNumbers() {
        log("testTypeswitchLineNumbers():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "typeswitch(\$address)",
                "    case \$address",
                "        as element(USAddress)",
                "            return",
                "                1",
                "    default return 0"
            )
        )

        val caseClause: CaseClauseContext = tree.find()
        assertThat(caseClause.getLine()).isEqualTo(3)
    }

    @Test
    @Throws(RecognitionException::class)
    fun testTypeswitchTypes() {
        log("testTypeswitchTypes():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "typeswitch(\$address)",
                "    case \$address as element(USAddress) return 1",
                "    case \$address as element(CanadaAddress) return 2",
                "    case \$address as element(UKAddress) return 3",
                "    default return 0"
            )
        )
        val typeSwitch: TypeswitchExprContext = tree.find()
        typeSwitch.caseClause(0).apply {
            assertThat(typeText()).isEqualTo("element(USAddress)")
            assertThat(returnExpr.findText<IntegerContext>()).isEqualTo("1")
        }
        typeSwitch.caseClause(2).apply {
            assertThat(typeText()).isEqualTo("element(UKAddress)")
            assertThat(returnExpr.findText<IntegerContext>()).isEqualTo("3")
        }
    }

    @Test
    @Throws(RecognitionException::class)
    fun testVariableDeclarationWithNull() {
        log("testVariableDeclarationWithNull():")
        val tree = parse(
            code(
                "xquery version '1.0-ml';",
                "declare variable \$null as xs:string := 'null';",
                "\$null"
            )
        )
        val varDecl: VarDeclContext = tree.find()
        assertThat(varDecl.value.findText<StringLiteralContext>()).isEqualTo("'null'")
    }

    @Test
    fun `Should allow an empty expression with just a comment`() {
        log("Should allow an empty expression with just a comment:")
        trace()
        val tree = parse(
            code(
                """<div>""",
                """{(: ABC-1234 <span class="date">{ ${"$"}progress }%</span>:)}""",
                """<h4 class="truncate"><a href="{${"$"}link}" title="{${"$"}set}">{${"$"}set}</a></h4>""",
                """<p class="truncate">{${"$"}name}</p>""",
                """</div>"""
            )
        )
    }
}
