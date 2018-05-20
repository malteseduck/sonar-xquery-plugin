/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.api

interface XQueryConstants {
    companion object {
        const val XQUERY_LANGUAGE_KEY = "xquery"
        const val FILE_EXTENSIONS_KEY = "sonar.xquery.fileExtensions"
        const val SOURCE_DIRECTORY_KEY = "sonar.xquery.sourceDirectory"
        const val XQTEST_REPORTS_DIRECTORY_KEY = "sonar.xqtest.reportsPath"

        const val XQUERY_LANGUAGE_NAME = "XQuery"
        val DEFAULT_FILE_EXTENSIONS = arrayOf("xqy", "xquery", "xq", "xqi", "xql", "xqm", "xqws")
        const val DEFAULT_FILE_EXTENSIONS_STRING = "xqy, xquery, xq, xqi, xql, xqm, xqws"
        const val DEFAULT_SOURCE_DIRECTORY = "src/main/xquery"
        const val DEFAULT_XQTEST_DIRECTORY = "target/xqtest-reports"
    }
}
