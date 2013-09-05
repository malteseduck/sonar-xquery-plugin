/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.api;


public interface XQueryConstants {
    static String XQUERY_LANGUAGE_KEY = "xquery";
    static String FILE_EXTENSIONS_KEY = "sonar.marklogic.fileExtensions";
    static String SOURCE_DIRECTORY_KEY = "sonar.marklogic.sourceDirectory";
    static String XQTEST_REPORTS_DIRECTORY_KEY = "sonar.xqtest.reportsPath";

    static String XQUERY_LANGUAGE_NAME = "XQuery";
    static String[] DEFAULT_FILE_EXTENSIONS = { "xqy", "xquery", "xq" , "xqi", "xql", "xqm", "xqws"};
    static String DEFAULT_FILE_EXTENSIONS_STRING = "xqy, xquery, xq, xqi, xql, xqm, xqws";
    static String DEFAULT_SOURCE_DIRECTORY = "src/main/xquery";
    static String DEFAULT_XQTEST_DIRECTORY = "target/xqtest-reports";
}
