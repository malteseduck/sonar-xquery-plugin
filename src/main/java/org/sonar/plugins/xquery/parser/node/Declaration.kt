/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node

import org.apache.commons.lang.StringUtils

/**
 * Holds basic information about a declaration (by default, a variable) so that
 * it can be used for further reference in parsing files. References are also
 * treated as "declarations" since they "declare" a use of a function or
 * variable.
 *
 * @author cieslinskice
 */
open class Declaration(name: String, var namespace: String?) {
    var name: String = name
        set(name) {
            field = if (name.contains(":")) {
                name.substringAfter(':')
            } else name
        }

    var type: String? = null
    var line: Int = 0

    override fun equals(obj: Any?): Boolean {
        return obj!!.hashCode() == this.hashCode()
    }

    override fun hashCode(): Int {
        var hash = 0
        if (this.name.isNotBlank()) {
            hash += this.name.hashCode()
        }
        if (!namespace.isNullOrBlank()) {
            hash += namespace!!.hashCode()
        }
        return hash
    }

    override fun toString(): String {
        val buffer = StringBuffer("Declaration [")
        if (!namespace.isNullOrBlank()) {
            buffer.append(namespace).append(":")
        }
        buffer.append(this.name)
        if (!type.isNullOrBlank()) {
            buffer.append("=").append(type)
        }
        buffer.append(" (").append(line).append(")]")
        return buffer.toString()
    }

}
