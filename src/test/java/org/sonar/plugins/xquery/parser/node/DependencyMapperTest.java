/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.antlr.runtime.RecognitionException;
import org.sonar.plugins.xquery.AbstractSonarTest;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DependencyMapperTest extends AbstractSonarTest {

    public DependencyMapper doMapping(SourceCode code) throws RecognitionException {
        return doMapping(code, true);
    }

    public DependencyMapper doMapping(SourceCode code, boolean popStack) throws RecognitionException {
        return doMapping(code, popStack, null);
    }

    public DependencyMapper doMapping(SourceCode code, boolean popStack, DependencyMapper mapper) throws RecognitionException {
        if (mapper == null) {
            mapper = new DependencyMapper();
        }
        XQueryTree tree = parse(code);
        mapper.enterSource(code, tree, null);
        visit(tree, mapper, popStack);
        if (popStack) {
            mapper.exitSource(tree);
        }
        return mapper;
    }

    public DependencyMapper doMapping(SourceCode code, DependencyMapper mapper) throws RecognitionException {
        return doMapping(code, true, mapper);
    }

    @Test
    public void testFLWORDeclaration() throws RecognitionException {
        log("testFLWORDeclaration():");
        DependencyMapper mapper = new DependencyMapper("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "for $article as element(article) in /article",
                "let $published as xs:boolean := $article/@published",
                "return",
                "    if ($published) then",
                "        $article",
                "    else",
                "        ()"
            ),
            false,
            mapper
        );

        Declaration decl = mapper.getVariableDeclaration("published", null);
        Assert.assertNotNull(decl, "Variable declaration mapping");
        Assert.assertEquals(decl.getLine(), 3, "Variable declaration line");
        Assert.assertEquals(decl.getType(), "xs:boolean", "Variable declaration type");
    }

    @Test
    public void testFullGlobalVariableDeclaration() throws RecognitionException {
        log("testFullGlobalVariableDeclaration():");
        DependencyMapper mapper = importModule(
            code(
                "xquery version '1.0-ml';",
                "",
                "module namespace test = 'http://lds.org/code/test';",
                "",
                "declare variable $status as xs:boolean := fn:true();"
            )
        );
        mapper.setMode("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                "if ($test:status) then",
                "    $test:status",
                "else",
                "    ()"
            ),
            false,
            mapper
        );

        Declaration decl = mapper.getVariableDeclaration("status", "http://lds.org/code/test");
        Assert.assertNotNull(decl, "Variable declaration mapping");
        Assert.assertEquals(decl.getLine(), 5, "Variable declaration line");
        Assert.assertEquals(decl.getType(), "xs:boolean", "Variable declaration type");
    }
    
    @Test
    public void testLocalStack() throws RecognitionException {
        log("testLocalStack():");
        DependencyMapper mapper = importModule(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare variable $status as xs:boolean := fn:true();"
            )
        );
        mapper.setMode("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "if (fn:true()) then",
                "    let $status as xs:boolean := fn:true()",
                "    return",
                "        $status",
                "else",
                "    'false'"
            ),
            false,
            mapper
        );

        Stack<HashMap<Integer, Declaration>> decls = mapper.getDeclarations();
        Assert.assertNotNull(decls, "Stack declarations");
        Assert.assertEquals(decls.size(), 3, "Stack declarations size");
    }

    @Test
    public void testFunctionBodyDeclaration() throws RecognitionException {
        log("testFunctionBodyDeclaration():");
        DependencyMapper mapper = new DependencyMapper("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "declare function local:test($id as xs:unsignedLong)",
                "as xs:unsignedLong {",
                "    $id",
                "};",
                "local:test(11111)"
            ),
            false,
            mapper
        );

        Declaration decl = mapper.getVariableDeclaration("id", null);
        Assert.assertNotNull(decl, "Variable declaration mapping");
        Assert.assertEquals(decl.getLine(), 2, "Variable declaration line");
        Assert.assertEquals(decl.getType(), "xs:unsignedLong", "Variable declaration type");
    }

    @Test
    public void testFunctionDelcaration() throws RecognitionException {
        log("testFunctionDelcaration():");
        DependencyMapper mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare function test:test()",
                "as xs:string {",
                "    'test'",
                "};"
            )
        );

        Function decl = mapper.getFunctionDeclaration("test:test", "http://lds.org/code/test");
        Assert.assertNotNull(decl, "Function declaration mapping");
        Assert.assertEquals(decl.getLine(), 3, "Function declaration line");
        Assert.assertEquals(decl.getType(), "xs:string", "Function declaration return type");
    }

    @Test
    public void testFunctionDelcarationParameters() throws RecognitionException {
        log("testFunctionDelcarationParameters():");
        DependencyMapper mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare function test:add($a as xs:integer?, $b as xs:integer?)",
                "as xs:integer? {",
                "    $a + $b",
                "};"
            )
        );

        Function decl = mapper.getFunctionDeclaration("add", "http://lds.org/code/test");
        Assert.assertNotNull(decl, "Function declaration mapping");

        Map<String, Declaration> parameters = decl.getParameters();
        Assert.assertEquals(parameters.size(), 2, "Function parameters");

        Declaration parameter = decl.getParameter("a");
        Assert.assertNotNull(parameter, "First function parameter");
        Assert.assertEquals(parameter.getType(), "xs:integer", "Function parameter type");
    }

    @Test
    public void testFunctionDelcarationWithElementReturnType() throws RecognitionException {
        log("testFunctionDelcarationWithElementReturnType():");
        DependencyMapper mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare function test:test()",
                "as element(test) {",
                "    <test/>",
                "};"
            )
        );

        Function decl = mapper.getFunctionDeclaration("test", "http://lds.org/code/test");
        Assert.assertNotNull(decl, "Function declaration mapping");
        Assert.assertEquals(decl.getLine(), 3, "Function declaration line");
        Assert.assertEquals(decl.getType(), "element(test)", "Function declaration return type");
    }

    @Test
    public void testGlobalFunctionDeclaration() throws RecognitionException {
        log("testGlobalVariableDeclaration():");
        DependencyMapper mapper = importModule(
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
        );  
        mapper.setMode("local");
        mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                "if (test:status()) then",
                "    $test:status",
                "else",
                "    ()"
            ),
            false,
            mapper
        );

        Function decl = mapper.getFunctionDeclaration("status", "http://lds.org/code/test");
        Assert.assertNotNull(decl, "Function declaration mapping");
        Assert.assertEquals(decl.getLine(), 5, "Function declaration line");
        Assert.assertEquals(decl.getType(), "xs:boolean", "Function declaration type");
    }

    @Test
    public void testGlobalModeLocalFunctionDelcaration() throws RecognitionException {
        log("testGlobalModeLocalFunctionDelcaration():");
        DependencyMapper mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "declare function local:test()",
                "as xs:string {",
                "    'test'",
                "};",
                "()"
            )
        );

        Stack<HashMap<Integer, Declaration>> decls = mapper.getDeclarations();
        Assert.assertNotNull(decls, "Declarations stack");
        Assert.assertEquals(decls.size(), 1, "Declarations stack size");
        Assert.assertEquals(decls.peek().size(), 0, "Declarations map size");
    }

    @Test
    public void testGlobalModeLocalVariableDelcaration() throws RecognitionException {
        log("testLocalVariableDelcaration():");
        DependencyMapper mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "declare variable $test as xs:string := 'bubba';",
                "()"
            )
        );

        Stack<HashMap<Integer, Declaration>> decls = mapper.getDeclarations();
        Assert.assertNotNull(decls, "Declarations stack");
        Assert.assertEquals(decls.size(), 1, "Declarations stack size");
        Assert.assertEquals(decls.peek().size(), 0, "Declarations map size");
    }

    @Test
    public void testGlobalVariableDeclaration() throws RecognitionException {
        log("testGlobalVariableDeclaration():");
        DependencyMapper mapper = doMapping(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare variable $test as xs:string := 'test';"
            )
        );

        Declaration decl = mapper.getVariableDeclaration("test", "http://lds.org/code/test");
        Assert.assertNotNull(decl, "Variable declaration mapping");
        Assert.assertEquals(decl.getLine(), 3, "Variable declaration line");
        Assert.assertEquals(decl.getType(), "xs:string", "Variable declaration type");
    }

    @Test
    public void testImports() throws RecognitionException {
        log("testImports():");
        DependencyMapper mapper = new DependencyMapper("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                "import module namespace rest = 'http://lds.org/code/rest' at '/rest.xqy';",
                "rest:test()"
            ),
            false,
            mapper
        );

        Assert.assertEquals(mapper.getImports().size(), 2, "Number of imports");

        Import imported = mapper.getImport("rest");
        Assert.assertEquals(imported.getLine(), 3, "Import line");
        Assert.assertEquals(imported.getNamespace(), "http://lds.org/code/rest", "Import namespace");
        Assert.assertEquals(imported.getAtHint(), "/rest.xqy", "Import location hint");
    }

    @Test
    public void testLocalFunctionDelcaration() throws RecognitionException {
        log("testLocalFunctionDelcaration():");
        DependencyMapper mapper = new DependencyMapper("local");
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
        );

        Function decl = mapper.getFunctionDeclaration("local:test", null);
        Assert.assertNotNull(decl, "Function declaration mapping");
        Assert.assertEquals(decl.getLine(), 2, "Function declaration line");
        Assert.assertEquals(decl.getType(), "xs:string", "Function declaration return type");
    }

    @Test
    public void testLocalVariableDelcaration() throws RecognitionException {
        log("testLocalVariableDelcaration():");
        DependencyMapper mapper = new DependencyMapper();
        mapper.setMode("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "declare variable $test as xs:string := 'bubba';",
                "()"
            ),
            false,
            mapper
        );

        Declaration decl = mapper.getVariableDeclaration("test", null);
        Assert.assertNotNull(decl, "Variable declaration mapping");
        Assert.assertEquals(decl.getLine(), 2, "Variable declaration line");
        Assert.assertEquals(decl.getType(), "xs:string", "Variable declaration type");
    }
    
    @Test
    public void testLocalVariableDelcarationInGlobal() throws RecognitionException {
        log("testLocalVariableDelcarationInGlobal():");
        DependencyMapper mapper = new DependencyMapper();
        mapper.setMode("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "module namespace test = 'http://lds.org/code/test';",
                "declare variable $test:test as xs:string := 'bubba';"
            ),
            false,
            mapper
        );

        Declaration decl = mapper.getVariableDeclaration("test:test", mapper.resolvePrefixNamespace("test:test"));
        Assert.assertNotNull(decl, "Variable declaration mapping");
        Assert.assertEquals(decl.getLine(), 3, "Variable declaration line");
        Assert.assertEquals(decl.getType(), "xs:string", "Variable declaration type");
    }   

    @Test
    public void testResolvePrefixNamespace() throws RecognitionException {
        log("testResolvePrefixNamespace():");
        DependencyMapper mapper = new DependencyMapper();
        mapper.setMode("local");
        doMapping(
            code(
                "xquery version '1.0-ml';",
                "import module namespace test = 'http://lds.org/code/test' at '/test.xqy';",
                "import module namespace rest = 'http://lds.org/code/rest' at '/rest.xqy';",
                "rest:test()"
            ),
            mapper
        );

        Assert.assertEquals(mapper.resolvePrefixNamespace("test"), "http://lds.org/code/test", "Namespace for prefix 'test'");
        Assert.assertEquals(mapper.resolvePrefixNamespace("rest"), "http://lds.org/code/rest", "Namespace for prefix 'rest'");
    }

    protected void visit(XQueryTree root, XQueryAstVisitor visitor, boolean popStack) {
        visitor.enterExpression(root);
        for (int i = 0; i < root.getChildCount(); i++) {
            XQueryTree child = root.getChild(i);
            visit(child, visitor, popStack);
        }
        if (popStack) {
            visitor.exitExpression(root);
        }
    }
}
