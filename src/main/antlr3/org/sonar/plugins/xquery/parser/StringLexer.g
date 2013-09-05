lexer grammar StringLexer;

options {
	superClass='AbstractXQueryLexer';
	tokenVocab=XQueryLexer;
}


@header {
/*******************************************************************************
 * Copyright (c) 2008, 2009 28msec Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gabriel Petrovay (28msec) - initial API and implementation
 *     Sam Neth (Mark Logic)
 *
 * Modified
 *     Chris Cieslinski
 *******************************************************************************/
package org.sonar.plugins.xquery.parser;
}

@lexer::members {
	// dummy list for warning elimination
	List<Stack<Object>> dummy = new ArrayList<Stack<Object>>();
	
	boolean inQuotStr = false;
	boolean inAposStr = false;
	
	public StringLexer(CharStream input, boolean isAposStr) {
		this(input, new RecognizerSharedState());
		this.inAposStr = isAposStr;
		this.inQuotStr = !isAposStr;
		setIsWsExplicit(true);
	}
}

QUOT	:	{ inQuotStr }? => '"' { inQuotStr = !inQuotStr; };
APOS	:	{ inAposStr }? => '\'' { inAposStr = !inAposStr; };
ESCAPE_QUOT	:	{ inQuotStr }? => '""';
ESCAPE_APOS	:	{ inAposStr }? => '\'\'';

// [145]
// Modified to add additional entity references supported by MarkLogic:
//      nbsp, hellip
L_PredefinedEntityRef
    :   { inQuotStr | inAposStr }? => '&' (              
           // Predefined in the grammar 
           'lt' | 'gt' | 'apos' | 'quot' | 'amp'
           
           // Additional special entities supported in MarkLogic (removed ML "checking")
           | 'bdquo' | 'brvbar' | 'bull' | 'circ' | 'copy' | 'emsp' | 'ensp' | 'hellip' | 'iexcl' | 'iquest' | 'laquo' | 'ldquo' | 'lsaquo' | 'lsquo' | 'mdash' | 'nbsp' | 'ndash' | 'oline' | 'prime' | 'Prime' | 'raquo' | 'rdquo' | 'rsaquo' | 'rsquo' | 'sbquo' | 'thinsp' | 'tilde' | 'uml'
           
           // Additional ISO 8859-1 entities supported in MarkLogic (removed ML "checking")
           | 'acute' | 'cedil' | 'cent' | 'curren' | 'deg' | 'divide' | 'macr' | 'micro' | 'middot' | 'not' | 'ordf' | 'ordm' | 'para' | 'plusmn' | 'pound' | 'sect' | 'times' | 'yen'            
        ) ';'
    ;

//[153]
L_CharRef
	:	{ inQuotStr | inAposStr }? => '&#' '0'..'9'+ ';' | '&#x' ('0'..'9'|'a'..'f'|'A'..'F')+ ';'
	;

L_QuotStringLiteralChar
	:	{ inQuotStr }? =>
		('\u0009' | '\u000A' | '\u000D' | '\u0020'..'\u0021' | '\u0023'..'\u0025' 
		| '\u0027'..'\uD7FF' |	'\uE000'..'\uFFFD')+
	;

L_AposStringLiteralChar
	:	{ inAposStr }? =>
		('\u0009' | '\u000A' | '\u000D' | '\u0020'..'\u0025'
		| '\u0028'..'\uD7FF' | '\uE000'..'\uFFFD')+
	;

//L_AnyChar 
//	:	{!inQuotStr && !inAposStr}? => .;

L_AnyChar
//  :    '\UFF02';
    :   { !inQuotStr && !inAposStr }? =>
        ('\u0009' | '\u000A' | '\u000D' | '\u0020'..'\u0025' | '\u0027'..'\u003B' 
        | '\u003D'..'\u007A' | '\u007C' | '\u007E'..'\uD7FF' | '\uE000'..'\uFFFD')+
    ;
	