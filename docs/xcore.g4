grammar xcore; // The core grammar rules for XQuery and XPath

// Digits:
Digits : [0-9]+ ;

// Processing Instructions:
pi : '<?' piTarget (S (Char* ~ PIInternal))? '?>' ;
PIInternal : (Char* '?>' Char*) ;
piTarget : Name ~ PITargetInternal ; /* xgc: xml-version */
PITargetInternal : ('X'|'x')  ('M'|'m')  ('L'|'l') ;

// Names and Tokens:
NameStartChar : ':'
    | [A-Z]
    | '_'
    | [a-z]
    | [\u00C0-\u00D6]
    | [\u00D8-\u00F6]
    | [\u00F8-\u02FF]
    | [\u0370-\u037D]
    | [\u037F-\u1FFF]
    | [\u200C-\u200D]
    | [\u2070-\u218F]
    | [\u2C00-\u2FEF]
    | [\u3001-\uD7FF]
    | [\uF900-\uFDCF]
    | [\uFDF0-\uFFFD]
    // Unfortunately, java escapes can't handle this conveniently,
    // as they're limited to 4 hex digits. TODO.
    //| [\u10000-\uEFFFF]
    ;
NameChar : NameStartChar | '-' | '.' | [0-9] | '\u00B7'
    | [\u0300-\u036F] | [\u203F-\u2040] ;
Name : NameStartChar (NameChar)* ;
//Names : Name ('\u0020' Name)* ;
Nmtoken : (NameChar)+ ;
//Nmtokens : Nmtoken ('\u0020' Nmtoken)* ;

// Character references:
CharRef : '&#' [0-9]+ ';'  |  '&#x' [0-9a-fA-F]+ ';' ;  /* xgc: xml-version */

// Qualified names:
qName : prefixedName | unprefixedName ; /* xgc: xml-version */
prefixedName : prefix ':' localPart ;
unprefixedName : localPart ;
prefix : ncName ;
localPart : ncName ;

// Namespaces declarations:
nsAttName : prefixedAttName | DefaultAttName ;
prefixedAttName : 'xmlns:' ncName ;
DefaultAttName : 'xmlns' ;
ncName : Name ~ NCNameInternal ; /* xgc: xml-version */    /* An XML Name, minus the ":" */
NCNameInternal : (Char* ':' Char*) ;

// Whitespace:
S : ([\u0020]          // space
        | [\u0009]     // horizontal tab
        | [\u000D]     // carriage return
        | [\u000A])+ -> skip ; // line feed / new line

// Characters:
Char : [\u0009]            // horizontal tab
    |  [\u000A]            // line feed
    |  [\u000D]            // carriage return / enter
    |  [\u0020-\uD7FF]     // As well as any other Unicode char, except
    |  [\uE000-\uFFFD]     // for the surrogate blocks, FFFE, and FFFF
    |  [\uE000-\uFFFD] ;

// Comments:
comment : '(:' (commentContents | comment)* ':)' ;
commentContents : (Char+ ~ CommentContentsInternal) ;
CommentContentsInternal : Char* ('(:' | ':)') Char* ;