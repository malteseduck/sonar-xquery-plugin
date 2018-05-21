lexer grammar XQueryLexer;

channels { WHITESPACE }

// Note: string syntax depends on syntactic context, so they are
// handled by the parser and not the lexer.

// NUMBERS

IntegerLiteral: Digits ;
DecimalLiteral: '.' Digits | Digits '.' [0-9]* ;
DoubleLiteral: ('.' Digits | Digits ('.' [0-9]*)?) [eE] [+-]? Digits ;

fragment
Digits: [0-9]+ ;

// This could be checked elsewhere: http://www.w3.org/TR/REC-xml/#wf-Legalchar
PredefinedEntityRef: '&' (
    'lt' | 'gt' | 'amp' | 'quot' | 'apos'

    // Additional special entities supported in MarkLogic (removed ML "checking")
    | 'bdquo' | 'brvbar' | 'bull' | 'circ' | 'copy' | 'emsp' | 'ensp' | 'hellip' | 'iexcl' | 'iquest' | 'laquo' | 'ldquo' | 'lsaquo' | 'lsquo' | 'mdash' | 'nbsp' | 'ndash' | 'oline' | 'prime' | 'Prime' | 'raquo' | 'rdquo' | 'rsaquo' | 'rsquo' | 'sbquo' | 'thinsp' | 'tilde' | 'uml'

    // Additional ISO 8859-1 entities supported in MarkLogic (removed ML "checking")
    | 'acute' | 'cedil' | 'cent' | 'curren' | 'deg' | 'divide' | 'macr' | 'micro' | 'middot' | 'not' | 'ordf' | 'ordm' | 'para' | 'plusmn' | 'pound' | 'sect' | 'times' | 'yen'
    ) ';'
;

// CharRef is additionally limited by http://www.w3.org/TR/REC-xml/#NT-Char,
CharRef: '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';' ;

// Escapes are handled as two Quot or two Apos tokens, to avoid maximal
// munch lexer ambiguity.
Quot: '"'  ;
Apos: '\'' ;

// XML-SPECIFIC

COMMENT: '<!--' ('-' ~[-] | ~[-])* '-->' ;
XMLDECL: '<?' [Xx] [Mm] [Ll] ([ \t\r\n] .*?)? '?>' ;
PI:      '<?' NCName ([ \t\r\n] .*?)? '?>' ;
CDATA:   '<![CDATA[' .*? ']]>' ;
PRAGMA:  '(#' WS? (NCName ':')? NCName (WS .*?)? '#)' ;

// WHITESPACE

// S ::= (#x20 | #x9 | #xD | #xA)+
WS: [ \t\r\n]+ -> channel(WHITESPACE);

// OPERATORS

EQUAL:     '='  ;
NOT_EQUAL: '!=' ;
LPAREN:    '(' ;
RPAREN:    ')' ;
LBRACKET:  '[' ;
RBRACKET:  ']' ;
LBRACE:    '{' ;
RBRACE:    '}' ;

STAR:  '*' ;
PLUS:  '+' ;
MINUS: '-' ;

COMMA:     ',' ;
DOT:       '.' ;
DDOT:      '..' ;
COLON:     ':' ;
COLON_EQ:  ':=' ;
SEMICOLON: ';' ;

SLASH:  '/'  ;
DSLASH: '//' ;
VBAR:   '|'  ;
DBAR:   '||' ;

LANGLE:    '<'  ;
RANGLE:    '>'  ;
LE:        '<=' ;
GE:        '>=' ;
ANGLEIN:   '<<' ;
ANGLEOUT:  '>>' ;
ARROW:     '=>' ;

QUESTION: '?' ;
AT: '@' ;
PERCENT: '%';
DOLLAR: '$' ;
POUND: '#';
Q: 'Q' ;

// KEYWORDS

KW_ALLOWING:           'allowing';
KW_ANCESTOR:           'ancestor';
KW_ANCESTOR_OR_SELF:   'ancestor-or-self';
KW_AND:                'and';
KW_ARRAY:              'array';
KW_AS:                 'as';
KW_ASCENDING:          'ascending';
KW_AT:                 'at';
KW_ATTRIBUTE:          'attribute';
KW_BASE_URI:           'base-uri';
KW_BINARY:             'binary';
KW_BOUNDARY_SPACE:     'boundary-space';
KW_BY:                 'by';
KW_CASE:               'case';
KW_CAST:               'cast';
KW_CASTABLE:           'castable';
KW_CATCH:              'catch';
KW_CHILD:              'child';
KW_COLLATION:          'collation';
KW_COMMENT:            'comment';
KW_CONSTRUCTION:       'construction';
KW_CONTEXT:            'context';
KW_COPY_NS:            'copy-namespaces';
KW_COUNT:              'count';
KW_DECIMAL_FORMAT:     'decimal-format';
KW_DECIMAL_SEPARATOR:  'decimal-separator';
KW_DECLARE:            'declare';
KW_DEFAULT:            'default';
KW_DESCENDANT:         'descendant';
KW_DESCENDANT_OR_SELF: 'descendant-or-self';
KW_DESCENDING:         'descending';
KW_DIGIT:              'digit';
KW_DIV:                'div';
KW_DOCUMENT:           'document';
KW_DOCUMENT_NODE:      'document-node';
KW_ELEMENT:            'element';
KW_ELSE:               'else';
KW_EMPTY:              'empty';
KW_EMPTY_SEQUENCE:     'empty-sequence';
KW_ENCODING:           'encoding';
KW_END:                'end';
KW_EQ:                 'eq';
KW_EVERY:              'every';
KW_EXCEPT:             'except';
KW_EXPONENT_SEPARATOR: 'exponent-separator';
KW_EXTERNAL:           'external';
KW_FOLLOWING:          'following';
KW_FOLLOWING_SIBLING:  'following-sibling';
KW_FOR:                'for';
KW_FUNCTION:           'function';
KW_GE:                 'ge';
KW_GREATEST:           'greatest';
KW_GROUP:              'group';
KW_GROUPING_SEPARATOR: 'grouping-separator';
KW_GT:                 'gt';
KW_IDIV:               'idiv';
KW_IF:                 'if';
KW_IMPORT:             'import';
KW_IN:                 'in';
KW_INFINITY:           'infinity';
KW_INHERIT:            'inherit';
KW_INSTANCE:           'instance';
KW_INTERSECT:          'intersect';
KW_IS:                 'is';
KW_ITEM:               'item';
KW_LAX:                'lax';
KW_LE:                 'le';
KW_LEAST:              'least';
KW_LET:                'let';
KW_LT:                 'lt';
KW_MAP:                'map';
KW_MOD:                'mod';
KW_MODULE:             'module';
KW_NAMESPACE:          'namespace';
KW_NAMESPACE_NODE:     'namespace-node';
KW_NAN:                'NaN';
KW_NE:                 'ne';
KW_NEXT:               'next';
KW_NO_INHERIT:         'no-inherit';
KW_NO_PRESERVE:        'no-preserve';
KW_NODE:               'node';
KW_OF:                 'of';
KW_ONLY:               'only';
KW_OPTION:             'option';
KW_OR:                 'or';
KW_ORDER:              'order';
KW_ORDERED:            'ordered';
KW_ORDERING:           'ordering';
KW_PARENT:             'parent';
KW_PATTERN_SEPARATOR:  'pattern-separator';
KW_PER_MILLE:          'per-mille';
KW_PRECEDING:          'preceding';
KW_PRECEDING_SIBLING:  'preceding-sibling';
KW_PRESERVE:           'preserve';
KW_PREVIOUS:           'previous';
KW_PI:                 'processing-instruction';
KW_PROPERTY:           'property';
KW_RETURN:             'return';
KW_SATISFIES:          'satisfies';
KW_SCHEMA:             'schema';
KW_SCHEMA_ATTR:        'schema-attribute';
KW_SCHEMA_ELEM:        'schema-element';
KW_SELF:               'self';
KW_SLIDING:            'sliding';
KW_SOME:               'some';
KW_STABLE:             'stable';
KW_START:              'start';
KW_STRICT:             'strict';
KW_STRIP:              'strip';
KW_SWITCH:             'switch';
KW_TEXT:               'text';
KW_THEN:               'then';
KW_TO:                 'to';
KW_TREAT:              'treat';
KW_TRY:                'try';
KW_TUMBLING:           'tumbling';
KW_TYPESWITCH:         'typeswitch';
KW_UNION:              'union';
KW_UNORDERED:          'unordered';
KW_VALIDATE:           'validate';
KW_VARIABLE:           'variable';
KW_VERSION:            'version';
KW_WHEN:               'when';
KW_WHERE:              'where';
KW_WINDOW:             'window';
KW_XQUERY:             'xquery';
KW_ZERO_DIGIT:         'zero-digit';

// NAMES

// We create these basic variants in order to honor ws:explicit in some basic cases
FullQName: NCName ':' NCName ;
NCNameWithLocalWildcard:  NCName ':' '*' ;
NCNameWithPrefixWildcard: '*' ':' NCName ;

// According to http://www.w3.org/TR/REC-xml-names/#NT-NCName,
// it is 'an XML Name, minus the ":"'
NCName: NameStartChar NameChar*;

BracedURILiteralInternal : [^&{}] ;

fragment
NameStartChar: [_a-zA-Z]
    | '\u00C0'..'\u00D6'
    | '\u00D8'..'\u00F6'
    | '\u00F8'..'\u02FF'
    | '\u0370'..'\u037D'
    | '\u037F'..'\u1FFF'
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ;

fragment
NameChar: NameStartChar
        | '-'
        | '.'
        | [0-9]
        | '\u00B7'
        | '\u0300'..'\u036F'
        | '\u203F'..'\u2040'
        ;

// XQuery comments
//
// Element content can have an unbalanced set of (: :) pairs (as XQuery
// comments do not really exist inside them), so it is better to treat
// this as a single token with a recursive rule, rather than using a
// mode.

XQComment: '(' ':' (XQComment | '(' ~[:] | ':' ~[)] | ~[:(])* ':'+ ')' -> channel(HIDDEN);

// This is an intersection of:
//
// [148] ElementContentChar  ::= Char - [{}<&]
// [149] QuotAttrContentChar ::= Char - ["{}<&]
// [150] AposAttrContentChar ::= Char - ['{}<&]
//
// Therefore, we would have something like:
//
// ElementContentChar  ::= ContentChar | ["']
// QuotAttrContentChar ::= ContentChar | [']
// AposAttrContentChar ::= ContentChar | ["]
//
// This rule needs to be the very last one, so it has the lowest priority.

ContentChar:  ~["'{}<&]  ;