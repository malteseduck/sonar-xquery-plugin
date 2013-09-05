lexer grammar XMLLexer;

options {
	tokenVocab=XQueryLexer;
	superClass='AbstractXQueryLexer';
}

tokens {
	// Imported tokens
	L_QuotAttrContentChar;
	L_AposAttrContentChar;
	L_ElementContentChar;
	L_PredefinedEntityRef;
	L_CharRef;
	ESCAPE_LBRACKET;
	ESCAPE_RBRACKET;
	ESCAPE_APOS;
	ESCAPE_QUOT;
	CDATA_START;
	CDATA_END;
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
	
	// when we start, the '<' has already been eaten by the other lexer
	boolean inElem = true;
	boolean inAposAttr = false;
	boolean inQuotAttr = false;
	
	public boolean isInElement()
	{
	   return inElem;
	}
	
	public boolean isInAposAttribute()
	{
	   return inAposAttr;
	}
	
	public boolean isInQuotAttr()
	{
	   return inQuotAttr;
	}
	    
	@Override
	public void addToStack(List<AbstractXQueryLexer> stack) {
		if (!inAposAttr && !inQuotAttr)
			inElem = false;
		stack.add(this);
	} 
	
	private boolean log() {
		System.out.println("inApos:\t" + inAposAttr);
		System.out.println("inQuot:\t" + inQuotAttr);
		System.out.println("inElem:\t" + inElem);
		System.out.println("---------------------");
		return false;
	};
}

QUOT	:	{ inElem || inQuotAttr }? => '"' { if (!inAposAttr) inQuotAttr = (!inQuotAttr); };
APOS	:	{ inElem || inAposAttr }? => '\'' { if (!inQuotAttr) inAposAttr = !inAposAttr; };

L_QuotAttrContentChar
	:	{ inQuotAttr }? =>
		('\u0009' | '\u000A' | '\u000D' | '\u0020' | '\u0021' | '\u0023'..'\u0025' 
		| '\u0028'..'\u003B' | '\u003D'..'\u007A' | '\u007C'..'\u007C' | '\u007E'..'\uD7FF' |
		'\uE000'..'\uFFFD')+
	;

L_AposAttrContentChar
	:	{ inAposAttr }? =>
		('\u0009' | '\u000A' | '\u000D' | '\u0020' | '\u0021' | '\u0023'..'\u0025' 
		| '\u0028'..'\u003B' | '\u003D'..'\u007A' | '\u007C'..'\u007C' | '\u007E'..'\uD7FF' |
		'\uE000'..'\uFFFD')+
	;

L_ElementContentChar
//	:	 '\UFF02';
	:	{ !inElem }? =>
		('\u0009' | '\u000A' | '\u000D' | '\u0020'..'\u0025' | '\u0027'..'\u003B' 
		| '\u003D'..'\u007A' | '\u007C' | '\u007E'..'\uD7FF' | '\uE000'..'\uFFFD')+
	;


GREATER
	:	{ inElem }? => '>' { inElem = false; }
	;

EMPTY_CLOSE_TAG
	:	{ inElem }? => '/>' { inElem = false; }
	;

S
	:	{ inElem }? => (' ' | '\t' | '\r' | '\n')+
	;

//QName	:	{ inElem  }? => NCName (':' NCName)?;

L_NCName
	:	{ inElem }? => NCNameUnprotected
	;

fragment NCNameUnprotected
	:	NCNameStartChar NCNameChar*
	;

fragment NCNameStartChar
	:	Letter | '_'
	;

fragment NCNameChar
	:	Letter | XMLDigit | '.' | '-' | '_'
	; //| CombiningChar | Extender;

fragment Letter
	:	'a'..'z' | 'A'..'Z'
	;

fragment XMLDigit
	:	'0'..'9'
	;

//fragment Letter	:	{ CharHelper.isLetter(LA(1) }? =>  .;
//fragment BaseChar
//		:	{ CharHelper.isBaseChar(LA(1) }? =>  .;
//fragment Ideographic	
//		:	{ CharHelper.isIdeographic(LA(1)) }? =>  .;
//fragment XMLDigit
//		:	{ CharHelper.isXMLDigit(LA(1)) }? =>  .;
//fragment CombiningChar
//		:	{ CharHelper.isCombiningChar(LA(1)) }? =>  .;
//fragment Extender
//		:	{ CharHelper.isExtender(LA(1)) }? =>  .;

EQUAL	:	{ inElem  }? => '=';
ESCAPE_APOS	:	{ inAposAttr }? => '\'\'';
ESCAPE_QUOT	:	{ inQuotAttr }? => '""';

ESCAPE_LBRACKET
	:	{ !inElem || inAposAttr || inQuotAttr }? => '{{'
	;

ESCAPE_RBRACKET
	:	{ !inElem || inAposAttr || inQuotAttr }? => '}}'
	;

LBRACKET	:	{ !inElem || inAposAttr || inQuotAttr }? => '{';
RBRACKET	:	{ !inElem || inAposAttr || inQuotAttr }? => '}';
SMALLER :	'<';
CLOSE_TAG	:	{ !inElem }? => '</' { inElem = true; };

CDATA_START	: '<![CDATA[';
CDATA_END		: ']]>';

//[107]	/* ws: explicit */
L_CDataSection
		:	{ !inElem }? => CDATA_START (options {greedy=false;} : .*) CDATA_END
		;
		
//[108]	/* ws: explicit */ - resolved in the previous production
//CDataSectionContents

// [145]
// Modified to add additional entity references supported by MarkLogic:
L_PredefinedEntityRef
	:	{ !inElem || inAposAttr || inQuotAttr }? => '&' ( 		       
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
	:	{ !inElem || inAposAttr || inQuotAttr }? => '&#' ('0'..'9')+ ';' | '&#x' ('0'..'9'|'a'..'f'|'A'..'F')+ ';'
	;

L_DirCommentConstructor	
	:	{ !inElem }? => '<!--' (options {greedy=false;} : .* ) '-->'	/* ws: explicit */ ;

L_DirPIConstructor	
	:	{ !inElem }? => 
		'<?' SU? NCNameUnprotected (SU (options {greedy=false;} : .*))? '?>'	/* ws: explicit */ 
	;

fragment SU
	:	(' ' | '\t' | '\n' | '\r')+
	;
	
COLON	: ':';
