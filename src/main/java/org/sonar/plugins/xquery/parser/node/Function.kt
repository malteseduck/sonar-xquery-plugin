/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node

/**
 * Holds basic information about a function call or declaration so that it can
 * be used to make decisions when parsing files.
 *
 * @author cieslinskice
 */
class Function(name: String, namespace: String?) : Declaration(name, namespace) {
    var parameters: MutableMap<String, Declaration> = mutableMapOf()

    fun addParameter(variable: Declaration) {
        parameters[variable.name] = variable
    }

    fun getParameter(name: String): Declaration? {
        return parameters[name]
    }
}
