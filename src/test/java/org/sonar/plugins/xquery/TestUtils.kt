/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery

import java.util.ArrayList

object TestUtils {

    fun list(vararg strings: String): List<String> {
        val list = ArrayList<String>()
        for (string in strings) {
            list.add(string)
        }
        return list
    }
}
