/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.sonar.api.resources.AbstractLanguage
import org.sonar.plugins.xquery.api.XQueryConstants

/**
 * This class defines the XQuery language.
 *
 * @since 1.0
 */
/**
 * Default constructor.
 */
class XQuery : AbstractLanguage(XQueryConstants.XQUERY_LANGUAGE_KEY, XQueryConstants.XQUERY_LANGUAGE_NAME) {

    /**
     * Gets the file suffixes.
     *
     * @return the file suffixes
     * @see org.sonar.api.resources.Language.getFileSuffixes
     */
    override fun getFileSuffixes(): Array<String> {
        return XQueryConstants.DEFAULT_FILE_EXTENSIONS
    }

    companion object {

        val KEYWORDS_ARRAY = arrayOf("xquery", "where", "version", "variable", "union", "typeswitch", "treat", "to", "then", "text", "stable", "sortby", "some", "self", "schema", "satisfies", "returns", "return", "ref", "processing-instruction", "preceding-sibling", "preceding", "precedes", "parent", "only", "of", "node", "namespace", "module", "let", "item", "intersect", "instance", "in", "import", "if", "function", "for", "follows", "following-sibling", "following", "external", "except", "every", "else", "element", "descending", "descendant-or-self", "descendant", "define", "default", "declare", "comment", "child", "cast", "case", "before", "attribute", "assert", "ascending", "as", "ancestor-or-self", "ancestor", "after")
        val TYPES_ARRAY = arrayOf("xs:yearMonthDuration", "xs:unsignedLong", "xs:time", "xs:string", "xs:short", "xs:QName", "xs:Name", "xs:long", "xs:integer", "xs:int", "xs:gYearMonth", "xs:gYear", "xs:gMonthDay", "xs:gDay", "xs:float", "xs:duration", "xs:double", "xs:decimal", "xs:dayTimeDuration", "xs:dateTime", "xs:date", "xs:byte", "xs:boolean", "xs:anyURI", "xf:yearMonthDuration")

        /**
         * A XQuery instance.
         */
        val INSTANCE = XQuery()
    }
}
