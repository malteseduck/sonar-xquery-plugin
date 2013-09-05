/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.parser.XQueryParser;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Keeps track of all declarations and references of functions and variables for
 * a project. This information can be used to make more informed decisions when
 * doing code analysis, and can be used to add metrics for dependencies.
 * 
 * By default runs in "global" mode and only saves global function/variable
 * declarations. It can be set to a "local" mode so that it will track all
 * declarations and manage the scope stack as it traverses nodes.
 * 
 * @author cieslinskice
 * 
 */
public class DependencyMapper implements XQueryAstVisitor {

    private Stack<HashMap<Integer, Declaration>> declarations;
    private String namespace;
    private String prefix;
    private Map<String, Import> imports;
    private static final int RESERVED_STACKS = 1;
    private String mode;

    public DependencyMapper() {
        this("global");
    }

    public DependencyMapper(String mode) {
        // Set up "global" stack
        declarations = new Stack<HashMap<Integer, Declaration>>();
        declarations.push(new HashMap<Integer, Declaration>());
        imports = new HashMap<String, Import>();

        // Default to running in "global" mode
        setMode(mode);
    }

    /**
     * Adds a variable or function declaration to the declarations on the
     * current stack.
     * 
     * @param declaration
     *            The variable/function declaration information
     */
    protected void addDeclaration(Declaration declaration) {
        Map<Integer, Declaration> decls = declarations.peek();
        decls.put(declaration.hashCode(), declaration);
    }

    @Override
    public void checkReport(ProblemReporter reporter) {
        // By default do nothing        
    }

    @Override
    public void enterExpression(XQueryTree node) {
        // Process global mode operations
        if ("global".equals(getMode())) {
            enterGlobalExpression(node);
        } else if ("local".equals(getMode())) {
            enterLocalExpression(node);
        }
    }

    /**
     * Process the expression in "global" mode
     * 
     * @param node
     *            The AST node
     */
    protected void enterGlobalExpression(XQueryTree node) {
        switch (node.getType()) {
        // Update the namespace with the module namespace
        case XQueryParser.ModuleDecl:
            setPrefix(node.getTextValue("ModulePrefix"));
            setNamespace(node.getValue("StringLiteral"));
            break;
        // If the current node is a function declaration then add a new
        // declaration to the map (only if it is a "global" function - set in a
        // module library). Local function declarations will be added by the
        // ScopedVisitor class.
        case XQueryParser.FunctionDecl:
            if (getNamespace() != null) {
                mapFunctionDeclaration(node);
            }
            break;
        // If the current node is a function call then add a new
        // declaration to the map
        case XQueryParser.FunctionCall:
            break;
        // If the current node is a variable declaration then add a new
        // declaration to the map (only if it is a "global" variable - set in a
        // module library). Local variable declarations will be added by the
        // ScopedVisitor class.
        case XQueryParser.VarDecl:
            if (getNamespace() != null) {
                mapVariableDeclaration(node);
            }
            break;
        // If the current node is a let clause (variable declaration) then add a
        // new declaration to the map
        case XQueryParser.LetClause:
            break;
        }
    }

    /**
     * Process the expression in "local" mode
     * 
     * @param node
     *            The AST node
     */
    protected void enterLocalExpression(XQueryTree node) {

        switch (node.getType()) {
        // Update the mapper namespace with the module namespace
        case XQueryParser.ModuleDecl:
            enterGlobalExpression(node);
            break;

        // Certain types of expressions are containers for scope, if we have
        // reached one of these then add a new stack. The function body one
        // needs to add "local" variables to the stack.
        case XQueryParser.FunctionBody:
            enterStack();
            mapFunctionVariableDeclarations((XQueryTree) node.getParent());
            break;
        case XQueryParser.FLOWRExpr:
            enterStack();
            break;

        // Add other relevant "local" declarations of variables into the current
        // stack
        case XQueryParser.LetClause:
            mapLetVariableDeclaration(node);
            break;

        // Add local variable and function declarations to the local stack
        // within the current namespace so we can easily find them.
        case XQueryParser.VarDecl:
            mapVariableDeclaration(node);
            break;
        case XQueryParser.FunctionDecl:
            mapFunctionDeclaration(node);
            break;

        // Add all the module imports so we can use them to find declarations
        // from references
        case XQueryParser.ModuleImport:
            mapImport(node);
            break;
        }
    }

    @Override
    public void enterSource(SourceCode code, XQueryTree node, DependencyMapper mapper) {
        // Clear out the imports when we enter a new source file
        imports = new HashMap<String, Import>();
        
        // Reset the module namespace information
        setPrefix(null);
        setNamespace(null);

        // Add a local stack as we enter the source (we don't want to add local
        // declarations to the "global" stacks)
        if ("local".equals(getMode())) {
            enterStack();
        }

    }

    /**
     * Adds another level to the declarations stack
     */
    public void enterStack() {
        declarations.push(new HashMap<Integer, Declaration>());
    }

    @Override
    public void exitExpression(XQueryTree node) {
        // Certain types of expressions are containers for scope, if we have
        // reached one of these and are in "local" mode then remote the current
        // stack
        if ("local".equals(getMode())) {
            switch (node.getType()) {
            case XQueryParser.FunctionBody:
                exitStack();
                break;
            case XQueryParser.FLOWRExpr:
                exitStack();
                break;      
                
            // In order to support multiple transactions in a main module we 
            // have to reset the stack after exiting a main module
            case XQueryParser.MainModule:
                resetStack();
                break;
            }
        }
    }

    @Override
    public void exitSource(XQueryTree node) {
        if ("local".equals(getMode())) {
            resetStack();
        }
    }

    /**
     * Removes a level from the declarations stack
     */
    public void exitStack() {
        if (declarations.size() > RESERVED_STACKS) {
            declarations.pop();
        }
    }

    /**
     * Looks for the declaration for the specified key. The key can be a
     * variable or a function. If no declaration is found in any of the stacks
     * then returns null.
     * 
     * @param key
     *            Variable key created with name/namespace
     * @return
     */
    public Declaration getDeclaration(Declaration key) {
        for (int i = declarations.size() - 1; i >= 0; i--) {
            Map<Integer, Declaration> stack = declarations.get(i);
            Declaration declaration = stack.get(key);
            if (declaration != null) {
                return declaration;
            }
        }
        return null;
    }

    public Stack<HashMap<Integer, Declaration>> getDeclarations() {
        return declarations;
    }

    public Function getFunctionCall(String name, String namespace) {
        // Variable key = new Variable(name, namespace);
        return null;
    }

    /**
     * Looks for a function declaration with the specified name/namespace key
     * 
     * @param name
     *            The name of the function
     * @param namespace
     *            The namespace of the function
     * @return The declaration information, if any is found.
     */
    public Function getFunctionDeclaration(String name, String namespace) {
        Declaration key = new Declaration(name, namespace);
        Declaration decl = getDeclaration(key);
        if (decl instanceof Function) {
            return (Function) decl;
        }
        return null;
    }

    public Import getImport(String name) {
        return imports.get(name);
    }

    public Map<String, Import> getImports() {
        return imports;
    }

    public String getMode() {
        return mode;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Looks for a variable declaration with the specified name/namespace key
     * 
     * @param name
     *            The name of the variable
     * @param namespace
     *            The namespace of the variable
     * @return The declaration information, if any is found.
     */
    public Declaration getVariableDeclaration(String name, String namespace) {
        Declaration key = new Declaration(name, namespace);
        Declaration decl = getDeclaration(key);
        if (decl instanceof Declaration) {
            return decl;
        }
        return null;
    }

    public Declaration getVariableReference(String name, String namespace) {
        // Variable key = new Variable(name, namespace);
        return null;
    }

    /**
     * Gets the function information from the specified node and adds it to the
     * function declaration map.
     * 
     * @param node
     *            A function declaration node
     */
    public void mapFunctionDeclaration(XQueryTree node) {
        int line = node.getLine();
        String name = node.getTextValue("FunctionName.QName");
        String type = node.getTypeValue("ReturnType");

        Function decl = new Function(name, getNamespace());
        decl.setLine(line);
        decl.setType(type);

        XQueryTree parameters = node.find("ParamList");
        if (parameters != null && parameters.getChildCount() > 0) {
            for (XQueryTree parameter : parameters.getChildren()) {
                String pName = parameter.getTextValue("ParamName.QName");
                String pType = parameter.getTextValue("TypeDeclaration.QName");

                Declaration param = new Declaration(pName, null);
                param.setType(pType);
                param.setLine(line);
                decl.addParameter(param);
            }
        }

        addDeclaration(decl);
    }

    /**
     * Gets the variable information from the specified function call and adds
     * its parameters to the variable declaration map.
     * 
     * @param node
     *            A function call node
     */
    public void mapFunctionVariableDeclarations(XQueryTree node) {
        XQueryTree parameters = node.find("ParamList");
        if (parameters != null && parameters.getChildCount() > 0) {
            for (XQueryTree parameter : parameters.getChildren()) {
                String pName = parameter.getTextValue("ParamName.QName");
                String pType = parameter.getTextValue("TypeDeclaration.QName");

                Declaration param = new Declaration(pName, null);
                param.setType(pType);
                param.setLine(parameter.getLine());
                addDeclaration(param);
            }
        }
    }

    /**
     * Gets the module import information from the specified module import node
     * and adds it to the imports map. Currently only supports a single
     * "at hint" as part of XQuery 1.1 (instead of the multiple allowed in 3.0).
     * 
     * @param node
     *            A module import node
     */
    public void mapImport(XQueryTree node) {
        int line = node.getLine();
        String name = node.getTextValue("ModulePrefix");
        String namespace = node.getTextValue("ModuleNamespace.StringLiteral");
        String hint = node.getTextValue("ModuleAtHints.StringLiteral");

        Import decl = new Import(name, namespace);
        decl.setLine(line);
        decl.addAtHint(hint);

        imports.put(name, decl);
    }

    /**
     * Gets the variable information from the specified let clause node and adds
     * it to the variable declaration map.
     * 
     * @param node
     *            A let clause node
     */
    public void mapLetVariableDeclaration(XQueryTree node) {
        int line = node.getLine();
        String name = node.getTextValue("LetName.QName");
        String type = node.getTypeValue("LetType");

        Declaration decl = new Declaration(name, null);
        decl.setLine(line);
        decl.setType(type);

        addDeclaration(decl);
    }

    /**
     * Gets the variable information from the specified node and adds it to the
     * variable declaration map.
     * 
     * @param node
     *            A variable declaration node
     */
    public void mapVariableDeclaration(XQueryTree node) {
        int line = node.getLine();
        String name = node.getTextValue("VarName.QName");
        String type = node.getTypeValue("VarType");

        Declaration decl = new Declaration(name, getNamespace());
        decl.setLine(line);
        decl.setType(type);

        addDeclaration(decl);
    }

    /**
     * Rolls back the stack - removing all local stacks and just keeping the
     * "reserved" stacks.
     */
    public void resetStack() {
        for (int i = declarations.size(); i > RESERVED_STACKS; i--) {
            declarations.pop();
        }
    }

    /**
     * Uses the prefix on the variable or function QName to determine the
     * namespace. This namespace can then be used to look up the variable or
     * function declaration.
     * 
     * @param qName
     *            The name of a function or variable (of the form prefix:name),
     *            or just a prefix
     * @return The namespace associated with the prefix
     */
    public String resolvePrefixNamespace(String qName) {
        String prefix = StringUtils.substringBefore(qName, ":");
        
        if (StringUtils.isNotBlank(prefix)) {
            // Check the module namespace
            if (StringUtils.equals(getPrefix(), prefix)) {
                return getNamespace();
            }
            
            // Check the imports for the prefix
            Import imported = getImport(prefix);
            if (imported != null) {
                return imported.getNamespace();
            }
        }
        return null;
    }

    public void setDeclarations(Stack<HashMap<Integer, Declaration>> declarations) {
        this.declarations = declarations;
    }

    public void setMode(String mode) {
        if ("local".equals(mode) || "global".equals(mode)) {
            this.mode = mode;
        } else {
            throw new RuntimeException("Invalid mode for dependency mapper - must be either 'global' or 'local'");
        }
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
