/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.node

import java.util.ArrayList

class Import(name: String, namespace: String) : Declaration(name, namespace) {

    private var atHints: MutableList<String>? = null

    val atHint: String?
        get() = getAtHint(0)

    init {
        atHints = ArrayList()
    }

    fun addAtHint(hint: String) {
        atHints!!.add(hint)
    }

    fun getAtHint(index: Int): String? {
        return if (atHints!!.size > index) {
            atHints!![index]
        } else null
    }

    fun getAtHints(): List<String>? {
        return atHints
    }

    fun setAtHints(atHints: MutableList<String>) {
        this.atHints = atHints
    }
}
