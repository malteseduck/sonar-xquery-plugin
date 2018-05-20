/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node

import org.antlr.v4.runtime.ParserRuleContext
import org.sonar.plugins.xquery.language.SourceCode
import org.sonar.plugins.xquery.parser.XQueryParser.*
import org.sonar.plugins.xquery.parser.getLine
import org.sonar.plugins.xquery.parser.unquotedText
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter
import org.sonar.plugins.xquery.parser.typeText
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor
import java.util.*

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
 */
class DependencyMapper(mode: String = "global") : XQueryAstVisitor {

    var declarations: Stack<HashMap<Int, Declaration>> = Stack()
    var namespace: String? = null
    var prefix: String? = null
    private var imports: MutableMap<String, Import> = mutableMapOf()
    var mode: String? = mode
        set(mode) = if ("local" == mode || "global" == mode) {
            field = mode
        } else {
            throw RuntimeException("Invalid mode for dependency mapper - must be either 'global' or 'local'")
        }

    init {
        // Set up "global" stack
        declarations.push(HashMap())
    }

    /**
     * Adds a variable or function declaration to the declarations on the
     * current stack.
     *
     * @param declaration
     * The variable/function declaration information
     */
    protected fun addDeclaration(declaration: Declaration) {
        val decls = declarations.peek()
        decls[declaration.hashCode()] = declaration
    }

    override fun checkReport(reporter: ProblemReporter) {
        // By default do nothing
    }

    override fun enterExpression(node: ParserRuleContext) {
        // Process global mode operations
        if ("global" == mode) {
            enterGlobalExpression(node)
        } else if ("local" == mode) {
            enterLocalExpression(node)
        }
    }

    /**
     * Process the expression in "global" mode
     *
     * @param node
     * The AST node
     */
    protected fun enterGlobalExpression(node: ParserRuleContext) {
        when (node) {
        // Update the namespace with the module namespace
            is ModuleDeclContext -> {
                prefix = node.prefix.text
                namespace = node.uri.unquotedText()
            }
        // If the current node is a function declaration then add a new
        // declaration to the map (only if it is a "global" function - set in a
        // module library). Local function declarations will be added by the
        // ScopedVisitor class.
            is FunctionDeclContext -> if (namespace != null) {
                mapFunctionDeclaration(node)
            }
        // If the current node is a function call then add a new
        // declaration to the map
            is FunctionCallContext -> {
            }
        // If the current node is a variable declaration then add a new
        // declaration to the map (only if it is a "global" variable - set in a
        // module library). Local variable declarations will be added by the
        // ScopedVisitor class.
            is VarDeclContext -> if (namespace != null) {
                mapVariableDeclaration(node)
            }
        // If the current node is a let clause (variable declaration) then add a
        // new declaration to the map
            is LetClauseContext -> {
            }
        }
    }

    /**
     * Process the expression in "local" mode
     *
     * @param node
     * The AST node
     */
    protected fun enterLocalExpression(node: ParserRuleContext) {


        when (node) {
        // Update the mapper namespace with the module namespace
            is ModuleDeclContext -> enterGlobalExpression(node)

        // Certain types of expressions are containers for scope, if we have
        // reached one of these then add a new stack. The function body one
        // needs to add "local" variables to the stack.
            is FunctionBodyContext -> {
                enterStack()
                mapFunctionVariableDeclarations(node.getParent() as FunctionDeclContext)
            }
            is FlworExprContext -> enterStack()

        // Add other relevant "local" declarations of variables into the current
        // stack
            is LetClauseContext -> mapLetVariableDeclaration(node)

        // Add local variable and function declarations to the local stack
        // within the current namespace so we can easily find them.
            is VarDeclContext -> mapVariableDeclaration(node)
            is FunctionDeclContext -> mapFunctionDeclaration(node)

        // Add all the module imports so we can use them to find declarations
        // from references
            is ModuleImportContext -> mapImport(node)
        }
    }

    override fun enterSource(code: SourceCode, node: ParserRuleContext, mapper: DependencyMapper) {
        // Clear out the imports when we enter a new source file
        imports = HashMap()

        // Reset the module namespace information
        prefix = null
        namespace = null

        // Add a local stack as we enter the source (we don't want to add local
        // declarations to the "global" stacks)
        if ("local" == mode) {
            enterStack()
        }
    }

    /**
     * Adds another level to the declarations stack
     */
    fun enterStack() {
        declarations.push(HashMap())
    }

    override fun exitExpression(node: ParserRuleContext) {
        // Certain types of expressions are containers for scope, if we have
        // reached one of these and are in "local" mode then remote the current
        // stack
        if ("local" == mode) {
            when (node) {
                is FunctionBodyContext -> exitStack()
                is FlworExprContext -> exitStack()

            // In order to support multiple transactions in a main module we
            // have to reset the stack after exiting a main module
                is MainModuleContext -> resetStack()
            }
        }
    }

    override fun exitSource(node: ParserRuleContext) {
        if ("local" == mode) {
            resetStack()
        }
    }

    /**
     * Removes a level from the declarations stack
     */
    fun exitStack() {
        if (declarations.size > RESERVED_STACKS) {
            declarations.pop()
        }
    }

    /**
     * Looks for the declaration for the specified key. The key can be a
     * variable or a function. If no declaration is found in any of the stacks
     * then returns null.
     *
     * @param key
     * Variable key created with name/namespace
     * @return
     */
    fun getDeclaration(key: Declaration): Declaration? {
        for (i in declarations.indices.reversed()) {
            val stack: Map<Int, Declaration> = declarations[i]
            val declaration = stack[key.hashCode()]
            if (declaration != null) {
                return declaration
            }
        }
        return null
    }

    fun getFunctionCall(name: String, namespace: String): Function? {
        // Variable key = new Variable(name, namespace);
        return null
    }

    /**
     * Looks for a function declaration with the specified name/namespace key
     *
     * @param name
     * The name of the function
     * @param namespace
     * The namespace of the function
     * @return The declaration information, if any is found.
     */
    fun getFunctionDeclaration(name: String, namespace: String?): Function? {
        val key = Declaration(name, namespace)
        val decl = getDeclaration(key)
        return decl as? Function
    }

    fun getImport(name: String): Import? {
        return imports[name]
    }

    fun getImports(): Map<String, Import>? {
        return imports
    }

    /**
     * Looks for a variable declaration with the specified name/namespace key
     *
     * @param name
     * The name of the variable
     * @param namespace
     * The namespace of the variable
     * @return The declaration information, if any is found.
     */
    fun getVariableDeclaration(name: String, namespace: String?): Declaration? {
        val key = Declaration(name, namespace)
        return getDeclaration(key)
    }

    fun getVariableReference(name: String, namespace: String): Declaration? {
        // Variable key = new Variable(name, namespace);
        return null
    }

    /**
     * Gets the function information from the specified node and adds it to the
     * function declaration map.
     *
     * @param node
     * A function declaration node
     */
    fun mapFunctionDeclaration(node: FunctionDeclContext) {
        val line = node.getLine()
        val name = node.functionName()?.text
        val type = node.type?.itemType()?.text

        name?.let {
            val decl = Function(
                name.run { if (contains(':')) substringAfter(':') else this },
                namespace
            )
            decl.line = line
            decl.type = type

            node.params?.forEach {
                val pName = it.name?.text
                val pType = it.type?.sequenceType()?.itemType()?.text

                pName?.let {
                    val param = Declaration(pName, null)
                    param.type = pType
                    param.line = line
                    decl.addParameter(param)
                }
            }

            addDeclaration(decl)
        }

    }

    /**
     * Gets the variable information from the specified function call and adds
     * its parameters to the variable declaration map.
     *
     * @param node
     * A function call node
     */
    fun mapFunctionVariableDeclarations(node: FunctionDeclContext) {
        val parameters = node.params
        parameters?.forEach { parameter ->
            val pName = parameter.name?.text
            val pType = parameter.typeText()

            pName?.let {
                val param = Declaration(pName, null)
                param.type = pType
                param.line = parameter.getStart().line
                addDeclaration(param)
            }
        }
    }

    /**
     * Gets the module import information from the specified module import node
     * and adds it to the imports map. Currently only supports a single
     * "at hint" as part of XQuery 1.1 (instead of the multiple allowed in 3.0).
     *
     * @param node
     * A module import node
     */
    fun mapImport(node: ModuleImportContext) {
        val line = node.getLine()
        val name = node.prefix.text
        val namespace = node.nsURI.unquotedText()

        val decl = Import(name, namespace)
        decl.line = line
        node.locations.forEach { decl.addAtHint(it.unquotedText()) }
        imports[name] = decl
    }

    /**
     * Gets the variable information from the specified let clause node and adds
     * it to the variable declaration map.
     *
     * @param node
     * A let clause node
     */
    fun mapLetVariableDeclaration(node: LetClauseContext) {
        val line = node.getLine()
        val name = node.letVar?.name?.text
        val type = node.letVar?.typeText()

        name?.let {
            val decl = Declaration(name, null)
            decl.line = line
            decl.type = type

            addDeclaration(decl)
        }
    }

    /**
     * Gets the variable information from the specified node and adds it to the
     * variable declaration map.
     *
     * @param node
     * A variable declaration node
     */
    fun mapVariableDeclaration(node: VarDeclContext) {
        val line = node.getLine()
        val name = node.name?.text
        val type = node.typeText()

        name?.let {
            val decl = Declaration(name, namespace)
            decl.line = line
            decl.type = type

            addDeclaration(decl)
        }
    }

    /**
     * Rolls back the stack - removing all local stacks and just keeping the
     * "reserved" stacks.
     */
    fun resetStack() {
        for (i in declarations.size downTo RESERVED_STACKS + 1) {
            declarations.pop()
        }
    }

    /**
     * Uses the prefix on the variable or function QName to determine the
     * namespace. This namespace can then be used to look up the variable or
     * function declaration.
     *
     * @param qName
     * The name of a function or variable (of the form prefix:name),
     * or just a prefix
     * @return The namespace associated with the prefix
     */
    fun resolvePrefixNamespace(qName: String): String? {
        val prefix = qName.substringBefore(':')

        if (prefix == this.prefix) return namespace

        // Check the imports for the prefix
        getImport(prefix)?.let {
            return it.namespace
        }
        return null
    }

    companion object {
        private val RESERVED_STACKS = 1
    }
}
