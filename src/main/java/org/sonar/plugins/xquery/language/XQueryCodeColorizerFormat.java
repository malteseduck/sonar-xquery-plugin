/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.colorizer.StringTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.plugins.xquery.api.XQueryConstants;

import java.util.*;

import static org.sonar.plugins.xquery.language.XQuery.KEYWORDS_ARRAY;
import static org.sonar.plugins.xquery.language.XQuery.TYPES_ARRAY;

public class XQueryCodeColorizerFormat extends CodeColorizerFormat {

	private final List<Tokenizer> tokenizers = new ArrayList<Tokenizer>();
	private static final Set<String> KEYWORDS = new HashSet<String>();
	private static final Set<String> TYPES = new HashSet<String>();

	static {
		Collections.addAll(KEYWORDS, KEYWORDS_ARRAY);
		Collections.addAll(TYPES, TYPES_ARRAY);
	}

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

	public XQueryCodeColorizerFormat() {
		super(XQueryConstants.XQUERY_LANGUAGE_KEY);
		String tagAfter = "</span>";
		
		// Keywords and types
		tokenizers.add(new KeywordsTokenizer("<span style=\"color: #0000FA\">", tagAfter, KEYWORDS));
		tokenizers.add(new KeywordsTokenizer("<span class=\"j\">", tagAfter, TYPES));
		
        // Comments
        tokenizers.add(new RegexpTokenizer("<span style=\"color: #008C00\">", tagAfter, "\\(:.*(:\\))?"));
        
        // XML tags
		tokenizers.add(new RegexpTokenizer("<span style=\"color: #000096\">", tagAfter, "</?\\p{L}*>?"));
        tokenizers.add(new RegexpTokenizer("<span style=\"color: #000096\">", tagAfter, ">"));

        // XML attributes
        tokenizers.add(new RegexpTokenizer("<span style=\"color: #F5844C\">", tagAfter, "\\w+\\s*=\\s*\".*?\""));

        // Function calls
//        tokenizers.add(new RegexpTokenizer("<span style=\"color: #FA6400\">", tagAfter, "(([a-zA-Z0-9]+:)?[a-zA-Z0-9]+)(?=\\()"));
        
        // Strings
		tokenizers.add(new StringTokenizer("<span style=\"color: #660E80\">", tagAfter));
	}

	@Override
	public List<Tokenizer> getTokenizers() {
		return tokenizers;
	}
}
