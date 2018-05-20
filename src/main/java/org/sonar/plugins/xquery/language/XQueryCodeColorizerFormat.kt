/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.sonar.api.web.CodeColorizerFormat
import org.sonar.colorizer.KeywordsTokenizer
import org.sonar.colorizer.RegexpTokenizer
import org.sonar.colorizer.StringTokenizer
import org.sonar.colorizer.Tokenizer
import org.sonar.plugins.xquery.api.XQueryConstants
import org.sonar.plugins.xquery.language.XQuery.Companion.KEYWORDS_ARRAY
import org.sonar.plugins.xquery.language.XQuery.Companion.TYPES_ARRAY

import java.util.*

class XQueryCodeColorizerFormat : CodeColorizerFormat(XQueryConstants.XQUERY_LANGUAGE_KEY) {

    private val tokenizers = ArrayList<Tokenizer>()
    /*
	 * Style Classes for colors:
	 * a: annotations (tan bold)
	 * k: keywords (blue bold)
	 * c: global statics? (purple italic)
	 * s: string (green bold)
	 * j: javascript (grey)
	 * cd: (grey)
	 * cppd: (grey)
	 * h: (blue)
	 * p: (green)
	 */

    init {
        val tagAfter = "</span>"
        tokenizers.addAll(listOf(
            // Keywords and types
            KeywordsTokenizer("<span class=\"k\">", tagAfter, KEYWORDS),
            KeywordsTokenizer("<span class=\"j\">", tagAfter, TYPES),

            // Comments
            RegexpTokenizer("<span class=\"cd\">", tagAfter, "\\(:.*(:\\))?"),

            // XML tags
            RegexpTokenizer("<span class=\"c\">", tagAfter, "</?\\p{L}*>?"),
            RegexpTokenizer("<span class=\"c\">", tagAfter, ">"),

            // XML attributes
            RegexpTokenizer("<span class=\"a\">", tagAfter, "\\w+\\s*=\\s*\".*?\""),

            // Function calls
            //        RegexpTokenizer("<span style=\"color: #FA6400\">", tagAfter, "(([a-zA-Z0-9]+:)?[a-zA-Z0-9]+)(?=\\()"),

            // Strings
            StringTokenizer("<span class=\"s\">", tagAfter)
        ))

    }

    override fun getTokenizers(): List<Tokenizer> {
        return tokenizers
    }

    companion object {
        private val KEYWORDS = HashSet<String>()
        private val TYPES = HashSet<String>()

        init {
            Collections.addAll(KEYWORDS, *KEYWORDS_ARRAY)
            Collections.addAll(TYPES, *TYPES_ARRAY)
        }
    }
}
