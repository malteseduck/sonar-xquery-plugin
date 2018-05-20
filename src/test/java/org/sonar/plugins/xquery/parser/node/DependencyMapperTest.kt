/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node

import org.antlr.runtime.RecognitionException
import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.AbstractSonarTest
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class DependencyMapperTest : AbstractSonarTest() {

    @Throws(RecognitionException::class)
    fun doMapping(code: SourceCode, popStack: Boolean = true, mapper: DependencyMapper = DependencyMapper()): DependencyMapper {
        val tree = parse(code)
        mapper.enterSource(code, tree, mapper)
        visit(tree, mapper, popStack)
        if (popStack) {
            mapper.exitSource(tree)
        }
        return mapper
    }

    @Throws(RecognitionException::class)
    fun doMapping(code: SourceCode, mapper: DependencyMapper): DependencyMapper {
        return doMapping(code, true, mapper)
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFLWORDeclaration() {
        log("testFLWORDeclaration():")
        val mapper = DependencyMapper("local")
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "for \$article as element(article) in /article",
                        "let \$published as xs:boolean := \$article/@published",
                        "return",
                        "    if (\$published) then",
                        "        \$article",
                        "    else",
                        "        ()"
                ),
                false,
                mapper
        )

        val decl = mapper.getVariableDeclaration("published", null)
        Assert.assertNotNull(decl, "Variable declaration mapping")
        assertEquals(decl!!.line, 3, "Variable declaration line")
        assertEquals(decl.type, "xs:boolean", "Variable declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFullGlobalVariableDeclaration() {
        log("testFullGlobalVariableDeclaration():")
        val mapper = importModule(
                code(
                        "xquery version '1.0-ml';",
                        "",
                        "module namespace test = 'http://lds.org/code/test';",
                        "",
                        "declare variable \$status as xs:boolean := fn:true();"
                )
        )
        mapper.mode = "local"
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                        "if (\$test:status) then",
                        "    \$test:status",
                        "else",
                        "    ()"
                ),
                false,
                mapper
        )

        val decl = mapper.getVariableDeclaration("status", "http://lds.org/code/test")
        Assert.assertNotNull(decl, "Variable declaration mapping")
        assertEquals(decl!!.line, 5, "Variable declaration line")
        assertEquals(decl.type, "xs:boolean", "Variable declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testLocalStack() {
        log("testLocalStack():")
        val mapper = importModule(
                code(
                        "xquery version '1.0-ml';",
                        "module namespace test = 'http://lds.org/code/test';",
                        "declare variable \$status as xs:boolean := fn:true();"
                )
        )
        mapper.mode = "local"
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "if (fn:true()) then",
                        "    let \$status as xs:boolean := fn:true()",
                        "    return",
                        "        \$status",
                        "else",
                        "    'false'"
                ),
                false,
                mapper
        )

        val decls = mapper.declarations
        Assert.assertNotNull(decls, "Stack declarations")
        assertEquals(decls.size, 3, "Stack declarations size")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionBodyDeclaration() {
        log("testFunctionBodyDeclaration():")
        val mapper = DependencyMapper("local")
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "declare function local:test(\$id as xs:unsignedLong)",
                        "as xs:unsignedLong {",
                        "    \$id",
                        "};",
                        "local:test(11111)"
                ),
                false,
                mapper
        )

        val decl = mapper.getVariableDeclaration("id", null)
        Assert.assertNotNull(decl, "Variable declaration mapping")
        assertEquals(decl!!.line, 2, "Variable declaration line")
        assertEquals(decl.type, "xs:unsignedLong", "Variable declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionDelcaration() {
        log("testFunctionDelcaration():")
        val mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "module namespace test = 'http://lds.org/code/test';",
                        "declare function test:test()",
                        "as xs:string {",
                        "    'test'",
                        "};"
                )
        )

        val decl = mapper.getFunctionDeclaration("test", "http://lds.org/code/test")
        Assert.assertNotNull(decl, "Function declaration mapping")
        assertEquals(decl!!.line, 3, "Function declaration line")
        assertEquals(decl.type, "xs:string", "Function declaration return type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionDelcarationParameters() {
        log("testFunctionDelcarationParameters():")
        val mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "module namespace test = 'http://lds.org/code/test';",
                        "declare function test:add(\$a as xs:integer?, \$b as xs:integer?)",
                        "as xs:integer? {",
                        "    \$a + \$b",
                        "};"
                )
        )

        val decl = mapper.getFunctionDeclaration("add", "http://lds.org/code/test")
        Assert.assertNotNull(decl, "Function declaration mapping")

        val parameters = decl!!.parameters
        assertEquals(parameters.size, 2, "Function parameters")

        val parameter = decl.getParameter("a")
        Assert.assertNotNull(parameter, "First function parameter")
        assertEquals(parameter?.type, "xs:integer", "Function parameter type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testFunctionDelcarationWithElementReturnType() {
        log("testFunctionDelcarationWithElementReturnType():")
        val mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "module namespace test = 'http://lds.org/code/test';",
                        "declare function test:test()",
                        "as element(test) {",
                        "    <test/>",
                        "};"
                )
        )

        val decl = mapper.getFunctionDeclaration("test", "http://lds.org/code/test")
        Assert.assertNotNull(decl, "Function declaration mapping")
        assertEquals(decl!!.line, 3, "Function declaration line")
        assertEquals(decl.type, "element(test)", "Function declaration return type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testGlobalFunctionDeclaration() {
        log("testGlobalVariableDeclaration():")
        var mapper = importModule(
                code(
                        "xquery version '1.0-ml';",
                        "",
                        "module namespace test = 'http://lds.org/code/test';",
                        "",
                        "declare function test:status()",
                        "as xs:boolean",
                        "{",
                        "    fn:true()",
                        "};"
                )
        )
        mapper.mode = "local"
        mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                        "if (test:status()) then",
                        "    \$test:status",
                        "else",
                        "    ()"
                ),
                false,
                mapper
        )

        val decl = mapper.getFunctionDeclaration("status", "http://lds.org/code/test")
        Assert.assertNotNull(decl, "Function declaration mapping")
        assertEquals(decl!!.line, 5, "Function declaration line")
        assertEquals(decl.type, "xs:boolean", "Function declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testGlobalModeLocalFunctionDelcaration() {
        log("testGlobalModeLocalFunctionDelcaration():")
        val mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "declare function local:test()",
                        "as xs:string {",
                        "    'test'",
                        "};",
                        "()"
                )
        )

        val decls = mapper.declarations
        Assert.assertNotNull(decls, "Declarations stack")
        assertEquals(decls.size, 1, "Declarations stack size")
        assertEquals(decls.peek().size, 0, "Declarations map size")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testGlobalModeLocalVariableDelcaration() {
        log("testLocalVariableDelcaration():")
        val mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "declare variable \$test as xs:string := 'bubba';",
                        "()"
                )
        )

        val decls = mapper.declarations
        Assert.assertNotNull(decls, "Declarations stack")
        assertEquals(decls.size, 1, "Declarations stack size")
        assertEquals(decls.peek().size, 0, "Declarations map size")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testGlobalVariableDeclaration() {
        log("testGlobalVariableDeclaration():")
        val mapper = doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "module namespace test = 'http://lds.org/code/test';",
                        "declare variable \$test as xs:string := 'test';"
                )
        )

        val decl = mapper.getVariableDeclaration("test", "http://lds.org/code/test")
        Assert.assertNotNull(decl, "Variable declaration mapping")
        assertEquals(decl!!.line, 3, "Variable declaration line")
        assertEquals(decl.type, "xs:string", "Variable declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testImports() {
        log("testImports():")
        val mapper = DependencyMapper("local")
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                        "import module namespace rest = 'http://lds.org/code/rest' at '/rest.xqy';",
                        "rest:test()"
                ),
                false,
                mapper
        )

        assertEquals(mapper.getImports()!!.size, 2, "Number of imports")

        val imported = mapper.getImport("rest")
        assertEquals(imported!!.line, 3, "Import line")
        assertEquals(imported.namespace, "http://lds.org/code/rest", "Import namespace")
        assertEquals(imported.atHint, "/rest.xqy", "Import location hint")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testLocalFunctionDelcaration() {
        log("testLocalFunctionDelcaration():")
        val mapper = DependencyMapper("local")
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "declare function local:test()",
                        "as xs:string {",
                        "    'test'",
                        "};",
                        "()"
                ),
                false,
                mapper
        )

        val decl = mapper.getFunctionDeclaration("test", null)
        Assert.assertNotNull(decl, "Function declaration mapping")
        assertEquals(decl!!.line, 2, "Function declaration line")
        assertEquals(decl.type, "xs:string", "Function declaration return type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testLocalVariableDelcaration() {
        log("testLocalVariableDelcaration():")
        val mapper = DependencyMapper()
        mapper.mode = "local"
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "declare variable \$test as xs:string := 'bubba';",
                        "()"
                ),
                false,
                mapper
        )

        val decl = mapper.getVariableDeclaration("test", null)
        Assert.assertNotNull(decl, "Variable declaration mapping")
        assertEquals(decl!!.line, 2, "Variable declaration line")
        assertEquals(decl.type, "xs:string", "Variable declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testLocalVariableDelcarationInGlobal() {
        log("testLocalVariableDelcarationInGlobal():")
        val mapper = DependencyMapper()
        mapper.mode = "local"
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "module namespace test = 'http://lds.org/code/test';",
                        "declare variable \$test:test as xs:string := 'bubba';"
                ),
                false,
                mapper
        )

        val decl = mapper.getVariableDeclaration("test:test", mapper.resolvePrefixNamespace("test:test")!!)
        Assert.assertNotNull(decl, "Variable declaration mapping")
        assertEquals(decl!!.line, 3, "Variable declaration line")
        assertEquals(decl.type, "xs:string", "Variable declaration type")
    }

    @Test
    @Throws(RecognitionException::class)
    fun testResolvePrefixNamespace() {
        log("testResolvePrefixNamespace():")
        val mapper = DependencyMapper()
        mapper.mode = "local"
        doMapping(
                code(
                        "xquery version '1.0-ml';",
                        "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                        "import module namespace rest = 'http://lds.org/code/rest' at '/rest.xqy';",
                        "rest:test()"
                ),
                mapper
        )

        assertEquals(mapper.resolvePrefixNamespace("test"), "http://lds.org/code/test", "Namespace for prefix 'test'")
        assertEquals(mapper.resolvePrefixNamespace("rest"), "http://lds.org/code/rest", "Namespace for prefix 'rest'")
    }

    protected fun visit(root: ParserRuleContext, visitor: XQueryAstVisitor, popStack: Boolean) {
        visitor.enterExpression(root)
        root.children
                ?.filter { it is ParserRuleContext }
                ?.forEach { child -> visit(child as ParserRuleContext, visitor, popStack) }
        if (popStack) {
            visitor.exitExpression(root)
        }
    }
}
