parser grammar XQueryParser;
options {
  superClass=MultiChannelBaseParser;
  tokenVocab=XQueryLexer;
}

// Mostly taken from http://www.w3.org/TR/xquery/#id-grammar, with some
// simplifications:
//
// 1. The parser itself doesn't really enforce ws:explicit except for some easy
//    cases (QNames and wildcards).  Walkers will need to do this (and also parse
//    wildcards a bit).
//
// 2. When collecting element content, we will need to check the HIDDEN
//    channel as well, for whitespace and XQuery comments (these should be
//    treated as regular text inside elements).

// MODULE HEADER ///////////////////////////////////////////////////////////////

module: transactions+=moduleTransaction (';' transactions+=moduleTransaction)* ;

moduleTransaction: versionDecl? (libraryModule | mainModule) ;

versionDecl: 'xquery' (
        ('encoding' encoding=stringLiteral)
        | ('version' version=stringLiteral ('encoding' encoding=stringLiteral)?)
     ) ';' ;

mainModule: prolog queryBody;

libraryModule: moduleDecl prolog;

moduleDecl: 'module' 'namespace' prefix=ncName '=' uri=stringLiteral ';' ;

// MODULE PROLOG ///////////////////////////////////////////////////////////////

prolog: ((defaultNamespaceDecl | setter | namespaceDecl | schemaImport | moduleImport) ';')*
        ((contextItemDecl | annotatedDecl | optionDecl) ';')* ;

defaultNamespaceDecl: 'declare' 'default'
                      type=('element' | 'function')
                      'namespace'
                      uri=stringLiteral;

annotatedDecl : 'declare' annotation* (varDecl | functionDecl) ;

annotation : '%' eqName ('(' literal (',' literal)* ')')? ;

setter: 'declare' 'boundary-space' type=('preserve' | 'strip')          # boundaryDecl
      | 'declare' 'default' 'collation' stringLiteral                   # defaultCollationDecl
      | 'declare' 'base-uri' stringLiteral                              # baseURIDecl
      | 'declare' 'construction' type=('strip' | 'preserve')            # constructionDecl
      | 'declare' 'ordering' type=('ordered' | 'unordered')             # orderingModeDecl
      | 'declare' 'default' 'order' 'empty' type=('greatest' | 'least') # emptyOrderDecl
      | 'declare' 'copy-namespaces'
                  preserve=('preserve' | 'no-preserve')
                  ','
                  inherit=('inherit' | 'no-inherit')                    # copyNamespacesDecl
      |  'declare' (('decimal-format' eqName) | ('default' 'decimal-format'))
                (dfPropertyName '=' stringLiteral)*                     # decimalFormatDecl
      ;

namespaceDecl: 'declare' 'namespace' prefix=ncName '=' uri=stringLiteral ;

schemaImport: 'import' 'schema'
              ('namespace' prefix=ncName '=' | 'default' 'element' 'namespace')?
              nsURI=stringLiteral
              ('at' locations+=stringLiteral (',' locations+=stringLiteral)*)? ;

moduleImport: 'import' 'module'
              ('namespace' prefix=ncName '=')?
              nsURI=stringLiteral
              ('at' locations+=stringLiteral (',' locations+=stringLiteral)*)? ;

varDecl: 'variable' '$' name=qName type=typeDeclaration?
         (':=' value=exprSingle | 'external') ;

contextItemDecl :
        'declare' 'context' 'item' ('as' itemType)?
        ((':=' value=exprSingle) | ('external' (':=' defaultValue=exprSingle)?)) ;

functionDecl: 'function' functionName '(' (params+=param (',' params+=param)*)? ')'
              ('as' type=sequenceType)?
              (functionBody | 'external') ;

functionBody: '{' body=expr '}' ;

optionDecl: 'declare' 'option' name=qName value=stringLiteral ;

queryBody: expr ;

param: '$' name=qName type=typeDeclaration? ;

// EXPRESSIONS /////////////////////////////////////////////////////////////////

expr: {disable(WHITESPACE);} exprSingle (',' exprSingle)* ;

exprSingle: flworExpr | quantifiedExpr | switchExpr | typeswitchExpr | tryCatchExpr | ifExpr | orExpr ;

flworExpr: (forClause | letClause | windowClause)+
           ('where' whereExpr=exprSingle)?
           groupByClause?
           orderByClause?
           ('count' '$' varName=qName)?
           'return' returnExpr=exprSingle ;

forClause: 'for' vars+=forVar (',' vars+=forVar)* ;

forVar: '$' name=qName type=typeDeclaration? ('allowing' 'empty')? ('at' '$' pvar=qName)?
        'in' in=exprSingle ;

letClause: 'let'  vars+=letVar (',' vars+=letVar)* ;

letVar: '$' name=qName type=typeDeclaration? ':=' value=exprSingle ;

windowClause : 'for' (tumblingWindowClause | slidingWindowClause) ;
tumblingWindowClause :
        'tumbling' 'window' '$' varName=qName typeDeclaration?
        'in' exprSingle windowStartCondition windowEndCondition? ;
slidingWindowClause :
        'sliding' 'window' '$' varName=qName typeDeclaration?
        'in' exprSingle windowStartCondition windowEndCondition ;
windowStartCondition : 'start' windowVars 'when' exprSingle ;
windowEndCondition : 'only'? 'end' windowVars 'when' exprSingle ;
windowVars :
        ('$' currentItem=eqName)? ('at' '$' pvar=qName)? ('previous' '$' previousItem=eqName)?
        ('next' '$' nextItem=eqName)? ;

groupByClause : 'group' 'by' groupingSpecList ;
groupingSpecList : groupingSpec (',' groupingSpec)* ;
groupingSpec :
        groupingVariable (typeDeclaration? ':=' exprSingle)?
        ('collation' collation=stringLiteral)? ;
groupingVariable : '$' varName=qName ;

orderByClause: 'stable'? 'order' 'by' specs+=orderSpec (',' specs+=orderSpec)* ;

orderSpec: value=exprSingle
           order=('ascending' | 'descending')?
           ('empty' empty=('greatest'|'least'))?
           ('collation' collation=stringLiteral)?
         ;

quantifiedExpr: quantifier=('some' | 'every') vars+=quantifiedVar (',' vars+=quantifiedVar)*
                'satisfies' value=exprSingle ;

quantifiedVar: '$' name=qName type=typeDeclaration? 'in' exprSingle ;

switchExpr : 'switch' '(' switchingExpr=expr ')'
              clause=switchCaseClause+
              'default' 'return' returnExpr=exprSingle ;

switchCaseClause : ('case' switchCaseOperand)+ 'return' exprSingle ;

switchCaseOperand : exprSingle ;

typeswitchExpr: 'typeswitch' '(' switchingExpr=expr ')'
                clauses=caseClause+
                'default' ('$' var=qName)? 'return' returnExpr=exprSingle ;

caseClause: 'case' ('$' var=qName 'as')? type=sequenceType 'return'
            returnExpr=exprSingle ;

ifExpr: 'if' '(' conditionExpr=expr ')'
        'then' thenExpr=exprSingle
        'else' elseExpr=exprSingle ;

tryCatchExpr : tryClause catchClause+ ;

tryClause : 'try' enclosedTryTargetExpr ;

enclosedTryTargetExpr : enclosedExpr ;

catchClause : 'catch' catchErrorList enclosedExpr ;

catchErrorList : mlCatchError | (nameTest ('|' nameTest)*) ;

mlCatchError : '(' '$' qName ')' ;

enclosedExpr : '{' expr? '}' ;

// Here we use a bit of ANTLR4's new capabilities to simplify the grammar
orExpr:
        ('-'|'+') orExpr                                    # unary
      | orExpr op='cast' 'as' singleType                    # cast
      | l=orExpr op='castable' 'as' r=singleType            # castable
      | l=orExpr op='treat' 'as' r=sequenceType             # treat
      | l=orExpr op='instance' 'of' r=sequenceType          # instanceOf
      | l=orExpr op=('intersect' | 'except') r=orExpr       # intersect
      | l=orExpr op=(KW_UNION | '|') r=orExpr               # union
      | l=orExpr op=('*' | 'div' | 'idiv' | 'mod') r=orExpr # mult
      | l=orExpr op=('+' | '-') r=orExpr                    # add
      | l=orExpr op='to' r=orExpr                           # range
      | l=orExpr ('eq' | 'ne' | 'lt' | 'le' | 'gt' | 'ge'
               | '=' | '!=' | '<' | '<' '=' | '>' | '>' '='
               | 'is' | '<' '<' | '>' '>') r=orExpr         # comparison
      | l=orExpr op='and' r=orExpr                          # and
      | l=orExpr op='or' r=orExpr                           # or
      | orExpr arrowExpr                                    # arrow
      | 'validate' vMode=('lax' | 'strict')? '{' expr '}'   # validate
      | PRAGMA+ '{' expr? '}'                               # extension
      | l=orExpr op='||' r=orExpr                           # strConcat
      | relativePathExpr ('!' relativePathExpr)*            # simpleMap
      | '/' relativePathExpr?                               # rootedPath
      | '//' relativePathExpr                               # allDescPath
      | relativePathExpr                                    # relative
      ;

primaryExpr: IntegerLiteral     # integer
           | DecimalLiteral     # decimal
           | DoubleLiteral      # double
           | stringLiteral      # string
           | '$' qName          # var
           | parenthesizedExpr  # paren
           | '.'                # current
           | functionCall       # funCall
           | functionItemExpr   # funItem
           | 'ordered' '{' expr '}'   # ordered
           | 'unordered' '{' expr '}' # unordered
           | constructor              # ctor
           ;

// PATHS ///////////////////////////////////////////////////////////////////////

parenthesizedExpr : '(' expr? ')' ;

relativePathExpr: stepExpr (sep=('/'|'//') stepExpr)* ;

stepExpr: axisStep | filterExpr ;

axisStep: (reverseStep | forwardStep) predicateList ;

forwardStep: forwardAxis nodeTest | abbrevForwardStep ;

forwardAxis: ( 'child'
             | 'descendant'
             | 'attribute'
             | 'self'
             | 'descendant-or-self'
             | 'following-sibling'
             | 'following'
             | 'namespace'
             | 'property' ) ':' ':' ;

abbrevForwardStep: '@'? nodeTest ;

reverseStep: reverseAxis nodeTest | abbrevReverseStep ;

reverseAxis: ( 'parent'
             | 'ancestor'
             | 'preceding-sibling'
             | 'preceding'
             | 'ancestor-or-self' ) ':' ':';

abbrevReverseStep: '..' ;

nodeTest: nameTest | kindTest ;

nameTest: qName          # exactMatch
        | '*'            # allNames
        | NCNameWithLocalWildcard  # allWithNS    // walkers must strip out the trailing :*
        | NCNameWithPrefixWildcard # allWithLocal // walkers must strip out the leading *:
        ;

filterExpr: primaryExpr (predicate | argumentList | lookup)* ;

predicateList: predicate*;

predicate: '[' predicates+=expr ']';

lookup : '?' keySpecifier ;

keySpecifier : ncName | IntegerLiteral | parenthesizedExpr | '*' ;

functionCall : functionName argumentList  /* xgc: reserved-function-names */ ;

argumentList : '(' (argument (',' argument)*)? ')' ;

argument : exprSingle | '?' ;

functionItemExpr : namedFunctionRef | inlineFunctionExpr ;

namedFunctionRef : eqName '#' IntegerLiteral  /* xgc: reserved-function-names */ ;

inlineFunctionExpr :
        annotation* 'function' '(' (params+=param (',' params+=param)*)? ')'
        ('as' sequenceType)? functionBody ;


eqName: qName | uriQualifiedName;

uriQualifiedName : bracedURILiteral ncName /* ws: explicit */ ;

bracedURILiteral :
        Q '{' (PredefinedEntityRef | CharRef | BracedURILiteralInternal)* '}'
        /* ws: explicit */ ;

// CONSTRUCTORS ////////////////////////////////////////////////////////////////

constructor: directConstructor | computedConstructor ;

directConstructor: dirElemConstructorOpenClose
                 | dirElemConstructorSingleTag
                 | (COMMENT | PI)
                 ;

// [96]: we don't check that the closing tag is the same here. It should be
// done elsewhere, if we really want to know.
dirElemConstructorOpenClose: '<' openName=qName dirAttributeList endOpen='>'
                             dirElemContent*
                             startClose='<' slashClose='/' closeName=qName '>' ;

dirElemConstructorSingleTag: '<' openName=qName dirAttributeList slashClose='/' '>' ;

// [97]: again, ws:explicit is better handled through the walker.
dirAttributeList: (qName '=' dirAttributeValue)* ;

dirAttributeValue: '"'  ( commonContent
                        | '"' '"'
                        // ~["{}<&] = ' + ~['"{}<&]
                        | Apos
                        | noQuotesNoBracesNoAmpNoLAng
                        | '<'  // Why can't we have angle braces inside an attribute value?
                        | '>'
                        )*
                   '"'
                 | '\'' (commonContent
                        | '\'' '\''
                        // ~['{}<&] = " + ~['"{}<&"]
                        | Quot
                        | noQuotesNoBracesNoAmpNoLAng
                        | '<'
                        | '>'
                        )*
                   '\''
                 ;

dirElemContent: directConstructor
              | commonContent
              | CDATA
              // ~[{}<&] = '" + ~['"{}<&]
              | Quot
              | Apos
              | noQuotesNoBracesNoAmpNoLAng
              ;

commonContent: (PredefinedEntityRef | CharRef ) | '{' '{' | '}' '}' | '{' expr? '}' ;

computedConstructor: 'document' '{' expr '}'                            # docConstructor
                   | 'element'
                     (elementName=qName | '{' elementExpr=expr '}')
                     '{' contentExpr=expr? '}'                          # elementConstructor
                   | 'namespace'
                     (name=qName | '{' nameExpr=expr '}')
                     '{' uriExpr=expr? '}'                              # namespaceConstructor
                   | 'attribute'
                     (attrName=qName | ('{' attrExpr=expr '}'))
                     '{' contentExpr=expr? '}'                          # attrConstructor
                   | 'text' '{' expr '}'                                # textConstructor
                   | 'array-node' '{' expr '}'                          # arrayNodeConstructor
                   | 'boolean-node' '{' expr '}'                        # booleanNodeConstructor
                   | 'number-node' '{' expr '}'                         # numberNodeConstructor
                   | 'object-node' '{' properties+=objectNodeExpr* '}'  # objectNodeConstructor
                   | 'null-node' '{' '}'                                # nullNodeConstructor
                   | 'comment' '{' expr '}'                             # commentConstructor
                   | 'processing-instruction'
                     (piName=ncName | '{' piExpr=expr '}')
                     '{' contentExpr=expr? '}'                          # piConstructor
                   | 'binary' '{' binaryExpr=expr '}'                   # binaryConstructor
                   | 'map' '{'
                     (mapConstructorEntry (',' mapConstructorEntry)*)?
                     '}'                                                # mapConstructor
                   | 'array'
                     (squareArrayConstructor | curlyArrayConstructor)   # arrayConstructor
                   ;

objectNodeExpr: l=expr ':' r=expr ','? ;

// TYPES AND TYPE TESTS ////////////////////////////////////////////////////////

singleType: qName '?'? ;

typeDeclaration: 'as' sequenceType ;

sequenceType: 'empty-sequence' '(' ')' | itemType occurrence=('?'|'*'|'+')? ;

itemType : kindTest
    | ('item' '(' ')')
    | functionTest
    | mapTest
    | arrayTest
    | atomicOrUnionType
    | parenthesizedItemType ;

kindTest: documentTest | elementTest | attributeTest | schemaElementTest
        | schemaAttributeTest | piTest | binaryTest | commentTest | textTest
        | namespaceNodeTest | arrayNodeTest | booleanNodeTest | nullNodeTest
        | numberNodeTest | objectNodeTest | anyKindTest
        ;

atomicOrUnionType : eqName ;

functionTest : annotation* (anyFunctionTest | typedFunctionTest) ;

anyFunctionTest : 'function' '(' '*' ')' ;

typedFunctionTest :
        'function' '(' (sequenceType (',' sequenceType)*)? ')' 'as' sequenceType ;

mapTest : anyMapTest | typedMapTest ;

anyMapTest : 'map' '(' '*' ')' ;

typedMapTest : 'map' '(' atomicOrUnionType ',' sequenceType ')' ;

arrayTest : anyArrayTest | typedArrayTest ;

anyArrayTest : 'array' '(' '*' ')' ;

typedArrayTest : 'array' '(' sequenceType ')' ;

parenthesizedItemType : '(' itemType ')' ;

documentTest: 'document-node' '(' (elementTest | schemaElementTest)? ')' ;

elementTest: 'element' '(' (
                (name=qName | wildcard='*')
                (',' type=qName optional='?'?)?
             )? ')' ;

attributeTest: 'attribute' '(' (
                (name=qName | wildcard='*')
                (',' type=qName)?
               )? ')' ;

schemaElementTest: 'schema-element' '(' qName ')' ;

schemaAttributeTest: 'schema-attribute' '(' qName ')' ;

piTest: 'processing-instruction' '(' (ncName | stringLiteral)? ')' ;

binaryTest: 'binary' '(' ')' ;

commentTest: 'comment' '(' ')' ;

textTest: 'text' '(' ')' ;

namespaceNodeTest : 'namespace-node' '(' ')' ;

arrayNodeTest: 'array-node' '(' name=stringLiteral? ')' ;

booleanNodeTest: 'boolean-node' '(' ')' ;

nullNodeTest: 'null-node' '(' ')' ;

numberNodeTest: 'number-node' '(' ')' ;

objectNodeTest: 'object-node' '(' ')' ;

anyKindTest: 'node' '(' ')' ;

arrowExpr: ( '=>' (eqName | '$' varName=eqName | parenthesizedExpr ) argumentList )+ ;

mapConstructorEntry : mapKeyExpr=exprSingle ':' mapValueExpr=exprSingle ;

squareArrayConstructor : '[' (exprSingle (',' exprSingle)*)? ']' ;

curlyArrayConstructor : 'array' '{' expr? '}' ;

// NAMES ///////////////////////////////////////////////////////////////////////

// walkers need to split into prefix+localpart by the ':'
qName: FullQName | ncName ;

ncName: NCName | keyword ;

functionName: FullQName | NCName | keywordOKForFunction ;

keyword: keywordOKForFunction | keywordNotOKForFunction ;

keywordNotOKForFunction:
         KW_ARRAY
       | KW_ATTRIBUTE
       | KW_COMMENT
       | KW_DOCUMENT_NODE
       | KW_ELEMENT
       | KW_EMPTY_SEQUENCE
       | KW_IF
       | KW_ITEM
       | KW_MAP
       | KW_NAMESPACE_NODE
       | KW_NODE
       | KW_PI
       | KW_SCHEMA_ATTR
       | KW_SCHEMA_ELEM
       | KW_SWITCH
       | KW_TEXT
       | KW_TYPESWITCH
       ;

keywordOKForFunction: KW_ALLOWING
       | KW_ANCESTOR
       | KW_ANCESTOR_OR_SELF
       | KW_AND
       | KW_ARRAY_NODE
       | KW_AS
       | KW_ASCENDING
       | KW_AT
       | KW_BASE_URI
       | KW_BINARY
       | KW_BOOLEAN_NODE
       | KW_BOUNDARY_SPACE
       | KW_BY
       | KW_CASE
       | KW_CAST
       | KW_CASTABLE
       | KW_CATCH
       | KW_CHILD
       | KW_COLLATION
       | KW_CONTEXT
       | KW_CONSTRUCTION
       | KW_COPY_NS
       | KW_COUNT
       | KW_DECLARE
       | KW_DECIMAL_FORMAT
       | KW_DECIMAL_SEPARATOR
       | KW_DEFAULT
       | KW_DESCENDANT
       | KW_DESCENDANT_OR_SELF
       | KW_DESCENDING
       | KW_DIGIT
       | KW_DIV
       | KW_DOCUMENT
       | KW_ELSE
       | KW_EMPTY
       | KW_ENCODING
       | KW_END
       | KW_EQ
       | KW_EVERY
       | KW_EXCEPT
       | KW_EXTERNAL
       | KW_EXPONENT_SEPARATOR
       | KW_FOLLOWING
       | KW_FOLLOWING_SIBLING
       | KW_FOR
       | KW_FUNCTION
       | KW_GE
       | KW_GREATEST
       | KW_GROUP
       | KW_GROUPING_SEPARATOR
       | KW_GT
       | KW_IDIV
       | KW_IMPORT
       | KW_IN
       | KW_INFINITY
       | KW_INHERIT
       | KW_INSTANCE
       | KW_INTERSECT
       | KW_IS
       | KW_LAX
       | KW_LE
       | KW_LEAST
       | KW_LET
       | KW_LT
       | KW_MOD
       | KW_MODULE
       | KW_NAMESPACE
       | KW_NE
       | KW_NEXT
       | KW_NO_INHERIT
       | KW_NO_PRESERVE
       | KW_NULL_NODE
       | KW_NUMBER_NODE
       | KW_OBJECT_NODE
       | KW_OF
       | KW_ONLY
       | KW_OPTION
       | KW_OR
       | KW_ORDER
       | KW_ORDERED
       | KW_ORDERING
       | KW_PARENT
       | KW_PATTERN_SEPARATOR
       | KW_PER_MILLE
       | KW_PRECEDING
       | KW_PRECEDING_SIBLING
       | KW_PRESERVE
       | KW_PREVIOUS
       | KW_PROPERTY
       | KW_RETURN
       | KW_SATISFIES
       | KW_SCHEMA
       | KW_SELF
       | KW_SLIDING
       | KW_SOME
       | KW_STABLE
       | KW_START
       | KW_STRICT
       | KW_STRIP
       | KW_THEN
       | KW_TO
       | KW_TREAT
       | KW_TRY
       | KW_TUMBLING
       | KW_UNION
       | KW_UNORDERED
       | KW_VALIDATE
       | KW_VARIABLE
       | KW_VERSION
       | KW_WHEN
       | KW_WHERE
       | KW_WINDOW
       | KW_XQUERY
       | KW_ZERO_DIGIT
       ;

dfPropertyName : KW_DECIMAL_SEPARATOR
    | KW_GROUPING_SEPARATOR
    | KW_INFINITY
    | MINUS
    | KW_NAN
    | PERCENT
    | KW_PER_MILLE
    | KW_ZERO_DIGIT
    | KW_DIGIT
    | KW_PATTERN_SEPARATOR
    | KW_EXPONENT_SEPARATOR ;

// LITERALS /////////////////////////////////////////////////////////////

literal : numericLiteral | stringLiteral ;

numericLiteral : IntegerLiteral | DecimalLiteral | DoubleLiteral ;

stringLiteral: '"' {enable(WHITESPACE);} ('"' '"'
                   | PredefinedEntityRef
                   | CharRef
                   // ~["&] = '{}< + ~['"{}<&]
                   | Apos
                   | LANGLE
                   | '{' '{'
                   | '}' '}'
                   | noQuotesNoBracesNoAmpNoLAng
                   | WS
                   // WS and XQComment are in the HIDDEN channel
                   )*
               {disable(WHITESPACE);} '"'
             | '\'' {enable(WHITESPACE);} ('\'' '\''
                    | PredefinedEntityRef
                    | CharRef
                    // ~['&] = "{}< + ~['"{}<&]
                    | Quot
                    | LANGLE
                    | '{' '{'
                    | '}' '}'
                    | noQuotesNoBracesNoAmpNoLAng
                    | WS
                    // WS and XQComment are in the HIDDEN channel
                    )* {disable(WHITESPACE);} '\''
             ;

// ~['"{}<&]: a very common (and long!) subexpression in the W3C EBNF grammar //

noQuotesNoBracesNoAmpNoLAng:
                   ( keyword
                   | ( IntegerLiteral
                     | DecimalLiteral
                     | DoubleLiteral
                     | PRAGMA
                     | EQUAL
                     | NOT_EQUAL
                     | LPAREN
                     | RPAREN
                     | LBRACKET
                     | RBRACKET
                     | STAR
                     | PLUS
                     | MINUS
                     | COMMA
                     | DOT
                     | DDOT
                     | COLON
                     | COLON_EQ
                     | SEMICOLON
                     | SLASH
                     | DSLASH
                     | VBAR
                     | DBAR
                     | RANGLE
                     | ARROW
                     | BANG
                     | QUESTION
                     | AT
                     | DOLLAR
                     | PERCENT
                     | POUND
                     | FullQName
                     | NCNameWithLocalWildcard
                     | NCNameWithPrefixWildcard
                     | NCName
                     | ContentChar
                     )
                   )+
 ;