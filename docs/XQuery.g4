grammar XQuery; // XQuery 3.1

// options {tokenVocab='grammar/xquery31Lexer';}

import xcore;

// XQuery 3.1 grammar, adapted from the original grammar @ https://www.w3.org/TR/xquery-3/#nt-bnf

xquery : module EOF?;
module : versionDecl? (libraryModule | mainModule) ;
versionDecl :
        'xquery' 'version' version=StringLiteral ('encoding' StringLiteral)?
        Separator ;
mainModule : prolog queryBody ;
libraryModule : moduleDecl prolog ;
moduleDecl : 'module' 'namespace' ncName '=' uriLiteral Separator ;
prolog :
        ((defaultNamespaceDecl | setter | namespaceDecl | importGeneral) Separator)*
        ((contextItemDecl | annotatedDecl | optionDecl) Separator)* ;
Separator : ';' ;                                // Terminal rule
setter : BoundarySpaceDecl
    | defaultCollationDecl
    | baseURIDecl
    | ConstructionDecl
    | OrderingModeDecl
    | EmptyOrderDecl
    | copyNamespacesDecl
    | decimalFormatDecl ;
BoundarySpaceDecl : 'declare' 'boundary-space' ('preserve' | 'strip') ; // Terminal rule
defaultCollationDecl : 'declare' 'default' 'collation' uriLiteral ;
baseURIDecl : 'declare' 'base-uri' uriLiteral ;
ConstructionDecl : 'declare' 'construction' ('strip' | 'preserve') ;    // Terminal rule
OrderingModeDecl : 'declare' 'ordering' ('ordered' | 'unordered') ;     // Terminal rule
EmptyOrderDecl : 'declare' 'default' 'order' 'empty' ('greatest' | 'least') ; // Terminal rule
copyNamespacesDecl : 'declare' 'copy-namespaces' PreserveMode ',' InheritMode ;
PreserveMode : 'preserve' | 'no-preserve' ;    // Terminal rule
InheritMode : 'inherit' | 'no-inherit' ;       // Terminal rule
decimalFormatDecl :
        'declare' (('decimal-format' eqName) | ('default' 'decimal-format'))
        (DfPropertyName '=' StringLiteral)* ;
DfPropertyName : 'decimal-separator'
    | 'grouping-separator'
    | 'infinity'
    | 'minus-sign'
    | 'NaN'
    | 'percent'
    | 'per-mille'
    | 'zero-digit'
    | 'digit'
    | 'pattern-separator'
    | 'exponent-separator' ;                // Terminal rule
importGeneral : schemaImport | moduleImport ;
schemaImport :
        'import' 'schema' schemaPrefix? uriLiteral
        ('at' uriLiteral (',' uriLiteral)*)? ;
schemaPrefix : ('namespace' ncName '=') | ('default' 'element' 'namespace') ;
moduleImport :
        'import' 'module' ('namespace' ncName '=')? uriLiteral
        ('at' uriLiteral (',' uriLiteral)*)? ;
namespaceDecl : 'declare' 'namespace' ncName '=' uriLiteral ;
defaultNamespaceDecl :
        'declare' 'default' ('element' | 'function') 'namespace' uriLiteral ;
annotatedDecl : 'declare' annotation* (varDecl | functionDecl) ;
annotation : '%' eqName ('(' literal (',' literal)* ')')? ;
varDecl :
        'variable' '$' varName typeDeclaration?
        ((':=' varValue) | ('external' (':=' varDefaultValue)?)) ;
varValue : exprSingle ;
varDefaultValue : exprSingle ;
contextItemDecl :
        'declare' 'context' 'item' ('as' itemType)?
        ((':=' varValue) | ('external' (':=' varDefaultValue)?)) ;
functionDecl :
        'function' eqName '(' paramList? ')' ('as' sequenceType)?
        (functionBody | 'external')  /* xgc: reserved-function-names */ ;
paramList : param (',' param)* ;
param : '$' eqName typeDeclaration? ;
functionBody : enclosedExpr ;
enclosedExpr : '{' expr? '}' ;
optionDecl : 'declare' 'option' eqName StringLiteral ;
queryBody : expr ;
expr : exprSingle (',' exprSingle)* ;
exprSingle : flworExpr
    | quantifiedExpr
    | switchExpr
    | typeswitchExpr
    | ifExpr
    | tryCatchExpr
    | orExpr ;
flworExpr : initialClause (intermediateClause*) returnClause ;
initialClause : forClause | letClause | windowClause ;
intermediateClause : initialClause
    | whereClause
    | groupByClause
    | orderByClause
    | countClause ;
forClause : 'for' forBinding (',' forBinding)* ;
forBinding :
        '$' varName typeDeclaration? AllowingEmpty? positionalVar?
        'in' exprSingle ;
AllowingEmpty : 'allowing' 'empty' ;                                 // Terminal rule
positionalVar : 'at' '$' varName ;
letClause : 'let' letBinding (',' letBinding)* ;
letBinding : '$' varName typeDeclaration? ':=' exprSingle ;
windowClause : 'for' (tumblingWindowClause | slidingWindowClause) ;
tumblingWindowClause :
        'tumbling' 'window' '$' varName typeDeclaration?
        'in' exprSingle windowStartCondition windowEndCondition? ;
slidingWindowClause :
        'sliding' 'window' '$' varName typeDeclaration?
        'in' exprSingle windowStartCondition windowEndCondition ;
windowStartCondition : 'start' windowVars 'when' exprSingle ;
windowEndCondition : 'only'? 'end' windowVars 'when' exprSingle ;
windowVars :
        ('$' currentItem)? positionalVar? ('previous' '$' previousItem)?
        ('next' '$' nextItem)? ;
currentItem : eqName ;
previousItem : eqName ;
nextItem : eqName ;
countClause : 'count' '$' varName ;
whereClause : 'where' exprSingle ;
groupByClause : 'group' 'by' groupingSpecList ;
groupingSpecList : groupingSpec (',' groupingSpec)* ;
groupingSpec :
        groupingVariable (typeDeclaration? ':=' exprSingle)?
        ('collation' uriLiteral)? ;
groupingVariable : '$' varName ;
orderByClause : (('order' 'by') | ('stable' 'order' 'by')) orderSpecList ;
orderSpecList : orderSpec (',' orderSpec)* ;
orderSpec : exprSingle orderModifier ;
orderModifier :
        ('ascending' | 'descending')?
        ('empty' ('greatest' | 'least'))?
        ('collation' uriLiteral)? ;
returnClause : 'return' exprSingle ;
quantifiedExpr :
        ('some' | 'every') '$' varName typeDeclaration?
        'in' exprSingle
        (',' '$' varName typeDeclaration? 'in' exprSingle)*
        'satisfies' exprSingle ;
switchExpr :
        'switch' '(' expr ')' switchCaseClause+ 'default' 'return' exprSingle ;
switchCaseClause : ('case' switchCaseOperand)+ 'return' exprSingle ;
switchCaseOperand : exprSingle ;
typeswitchExpr :
        'typeswitch' '(' expr ')' caseClause+ 'default' ('$' varName)?
        'return' exprSingle ;
caseClause : 'case' ('$' varName 'as')? sequenceTypeUnion 'return' exprSingle ;
sequenceTypeUnion : sequenceType ('|' sequenceType)* ;
ifExpr : 'if' '(' expr ')' 'then' exprSingle 'else' exprSingle ;
tryCatchExpr : tryClause catchClause+ ;
tryClause : 'try' enclosedTryTargetExpr ;
enclosedTryTargetExpr : enclosedExpr ;
catchClause : 'catch' catchErrorList enclosedExpr ;
catchErrorList : nameTest ('|' nameTest)* ;
orExpr : andExpr ( 'or' andExpr )* ;
andExpr : comparisonExpr ( 'and' comparisonExpr )* ;
comparisonExpr : stringConcatExpr ((ValueComp| GeneralComp| NodeComp) stringConcatExpr)? ;
stringConcatExpr : rangeExpr ( '||' rangeExpr )* ;
rangeExpr : additiveExpr ( 'to' additiveExpr )? ;
additiveExpr : multiplicativeExpr ( ('+' | '-') multiplicativeExpr )* ;
multiplicativeExpr : unionExpr ( ('*' | 'div' | 'idiv' | 'mod') unionExpr )* ;
unionExpr : intersectExceptExpr ( ('union' | '|') intersectExceptExpr )* ;
intersectExceptExpr : instanceofExpr ( ('intersect' | 'except') instanceofExpr )* ;
instanceofExpr : treatExpr ( 'instance' 'of' sequenceType )? ;
treatExpr : castableExpr ( 'treat' 'as' sequenceType )? ;
castableExpr : castExpr ( 'castable' 'as' singleType )? ;
castExpr : arrowExpr ( 'cast' 'as' singleType )? ;
arrowExpr : unaryExpr ( '=>' arrowFunctionSpecifier argumentList )* ;
unaryExpr : ('-' | '+')* valueExpr ;
valueExpr : validateExpr | extensionExpr | simpleMapExpr ;
GeneralComp : '=' | '!=' | '<' | '<=' | '>' | '>=' ;                  // Terminal rule
ValueComp : 'eq' | 'ne' | 'lt' | 'le' | 'gt' | 'ge' ;                 // Terminal rule
NodeComp : 'is' | '<<' | '>>' ;                                       // Terminal rule
validateExpr : 'validate' (ValidationMode | ('type' typeName))? enclosedExpr ;
ValidationMode : 'lax' | 'strict' ;                                   // Terminal rule
extensionExpr : pragma+ enclosedExpr ;
pragma : '(#' S? eqName (S pragmaContents)? '#)'  /* ws: explicit */ ;
// pragmaContents : (Char* ~ (Char* '#)' Char*)) ; // This is the original rule
pragmaContents : (Char* ~ PragmaContentsInternal) ;
PragmaContentsInternal : (Char* '#)' Char*) ;
simpleMapExpr : pathExpr ('!' pathExpr)* ;
pathExpr : ('/' relativePathExpr?)
    | ('//' relativePathExpr)
    | relativePathExpr  /* xgc: leading-lone-slash */ ;
relativePathExpr : stepExpr (('/' | '//') stepExpr)* ;
stepExpr : postfixExpr | axisStep ;
axisStep : (reverseStep | forwardStep) predicateList ;
forwardStep : (ForwardAxis nodeTest) | abbrevForwardStep ;
ForwardAxis : ('child' '::')
    | ('descendant' '::')
    | ('attribute' '::')
    | ('self' '::')
    | ('descendant-or-self' '::')
    | ('following-sibling' '::')
    | ('following' '::') ;                 // Terminal rule
abbrevForwardStep : '@'? nodeTest ;
reverseStep : (ReverseAxis nodeTest) | AbbrevReverseStep ;
ReverseAxis : ('parent' '::')
    | ('ancestor' '::')
    | ('preceding-sibling' '::')
    | ('preceding' '::')
    | ('ancestor-or-self' '::') ;          // Terminal rule
AbbrevReverseStep : '..' ;                 // Terminal rule
nodeTest : kindTest | nameTest ;
nameTest : eqName | wildcard ;
wildcard : '*'
    | (ncName ':' '*')
    | ('*' ':' ncName)
    | (bracedURILiteral '*')  /* ws: explicit */ ;
postfixExpr : primaryExpr (predicate | argumentList | lookup)* ;
argumentList : '(' (argument (',' argument)*)? ')' ;
predicateList : predicate* ;
predicate : '[' expr ']' ;
lookup : '?' keySpecifier ;
keySpecifier : ncName | IntegerLiteral | parenthesizedExpr | '*' ;
arrowFunctionSpecifier : eqName | varRef | parenthesizedExpr ;
primaryExpr : literal
    | varRef
    | parenthesizedExpr
    | ContextItemExpr
    | functionCall
    | orderedExpr
    | unorderedExpr
    | nodeConstructor
    | functionItemExpr
    | mapConstructor
    | arrayConstructor
    | stringConstructor
    | unaryLookup ;
literal : numericLiteral | StringLiteral ;
numericLiteral : IntegerLiteral | DecimalLiteral | DoubleLiteral ;
varRef : '$' varName ;
varName : eqName ;
parenthesizedExpr : '(' expr? ')' ;
ContextItemExpr : '.' ;                // Terminal rule
orderedExpr : 'ordered' enclosedExpr ;
unorderedExpr : 'unordered' enclosedExpr ;
functionCall : eqName argumentList  /* xgc: reserved-function-names */ ;
/* gn: parens */
argument : exprSingle | ArgumentPlaceholder ;
ArgumentPlaceholder : '?' ;            // Terminal rule
nodeConstructor : directConstructor | computedConstructor ;
directConstructor : dirElemConstructor | dirCommentConstructor | dirPIConstructor ;
dirElemConstructor :
        '<' qName dirAttributeList
        ('/>' | ('>' dirElemContent* '</' qName S? '>'))  /* ws: explicit */ ;
dirAttributeList :
        (S (qName S? '=' S? dirAttributeValue)?)*  /* ws: explicit */ ;
dirAttributeValue : ('"'  (EscapeQuot | quotAttrValueContent)* '"')
                  | ('\'' (EscapeApos | aposAttrValueContent)* '\'')  /* ws: explicit */ ;
quotAttrValueContent : ContentChar | commonContent ;
aposAttrValueContent : ContentChar | commonContent ;
dirElemContent : directConstructor
    | cDataSection
    | commonContent
    | ContentChar ;
commonContent : PredefinedEntityRef | CharRef | '{{' | '}}' | enclosedExpr ;
dirCommentConstructor : '<!--' dirCommentContents '-->'  /* ws: explicit */ ;
// dirCommentContents : ((Char - '-') | ('-' (Char - '-')))*  /* ws: explicit */ ; // This is the original rule
dirCommentContents : ((Char ~ '-') | ('-' (Char ~ '-')))*  /* ws: explicit */ ;
dirPIConstructor : '<?' piTarget (S dirPIContents)? '?>'  /* ws: explicit */ ;
// dirPIContents : (Char* - (Char* '?>' Char*))  /* ws: explicit */ ;  // This is the original rule
dirPIContents : (Char* ~ DirPIContentsInternal)  /* ws: explicit */ ;
DirPIContentsInternal : (Char* '?>' Char*) ;        // Terminal rule
cDataSection : '<![CDATA[' cDataSectionContents ']]>'  /* ws: explicit */ ;
// cDataSectionContents : (Char* - (Char* ']]>' Char*))  /* ws: explicit */ ; // This is the original rule
cDataSectionContents : (Char* ~ CDataSectionContentsInternal)  /* ws: explicit */ ;
CDataSectionContentsInternal : (Char* ']]>' Char*) ;    // Terminal rule
computedConstructor : compDocConstructor
    | compElemConstructor
    | compAttrConstructor
    | compNamespaceConstructor
    | compTextConstructor
    | compCommentConstructor
    | compPIConstructor ;
compDocConstructor : 'document' enclosedExpr ;
compElemConstructor : 'element' (eqName | ('{' expr '}')) enclosedContentExpr ;
enclosedContentExpr : enclosedExpr ;
compAttrConstructor : 'attribute' (eqName | ('{' expr '}')) enclosedExpr ;
compNamespaceConstructor :
        'namespace' (prefix | enclosedPrefixExpr) enclosedURIExpr ;
prefix : ncName ;
enclosedPrefixExpr : enclosedExpr ;
enclosedURIExpr : enclosedExpr ;
compTextConstructor : 'text' enclosedExpr ;
compCommentConstructor : 'comment' enclosedExpr ;
compPIConstructor :
        'processing-instruction' (ncName | ('{' expr '}')) enclosedExpr ;
functionItemExpr : namedFunctionRef | inlineFunctionExpr ;
namedFunctionRef : eqName '#' IntegerLiteral  /* xgc: reserved-function-names */ ;
inlineFunctionExpr :
        annotation* 'function' '(' paramList? ')'
        ('as' sequenceType)? functionBody ;
mapConstructor :
        'map' '{' (mapConstructorEntry (',' mapConstructorEntry)*)? '}' ;
mapConstructorEntry : mapKeyExpr ':' mapValueExpr ;
mapKeyExpr : exprSingle ;
mapValueExpr : exprSingle ;
arrayConstructor : squareArrayConstructor | curlyArrayConstructor ;
squareArrayConstructor : '[' (exprSingle (',' exprSingle)*)? ']' ;
curlyArrayConstructor : 'array' '{' expr? '}' ;
stringConstructor : '``[' stringConstructorContent ']``'  /* ws: explicit */ ;
stringConstructorContent :
        stringConstructorChars
        (stringConstructorInterpolation stringConstructorChars)*
        /* ws: explicit */ ;
// stringConstructorChars :
//         (Char* - (Char* ('`{' | ']``') Char*))  /* ws: explicit */ ;  // This is the original rule
stringConstructorChars :
        (Char* ~ StringConstructorCharsInternal)  /* ws: explicit */ ;
StringConstructorCharsInternal : (Char* ('`{' | ']``') Char*) ; // Terminal rule
stringConstructorInterpolation : '`{' expr? '}`' ;
unaryLookup : '?' keySpecifier ;
singleType : simpleTypeName '?'? ;
typeDeclaration : 'as' sequenceType ;
sequenceType : ('empty-sequence' '(' ')') | (itemType occurrenceIndicator?) ;
occurrenceIndicator : '?' | '*' | '+'  /* xgc: occurrence-indicators */ ;
itemType : kindTest
    | ('item' '(' ')')
    | functionTest
    | mapTest
    | arrayTest
    | atomicOrUnionType
    | parenthesizedItemType ;
atomicOrUnionType : eqName ;
kindTest : documentTest
    | elementTest
    | attributeTest
    | schemaElementTest
    | schemaAttributeTest
    | piTest
    | CommentTest
    | TextTest
    | NamespaceNodeTest
    | AnyKindTest ;
AnyKindTest : 'node' '(' ')' ;                     // Terminal rule
documentTest : 'document-node' '(' (elementTest | schemaElementTest)? ')' ;
TextTest : 'text' '(' ')' ;                        // Terminal rule
CommentTest : 'comment' '(' ')' ;                  // Terminal rule
NamespaceNodeTest : 'namespace-node' '(' ')' ;     // Terminal rule
piTest : 'processing-instruction' '(' (ncName | StringLiteral)? ')' ;
attributeTest : 'attribute' '(' (attribNameOrWildcard (',' typeName)?)? ')' ;
attribNameOrWildcard : attributeName | '*' ;
schemaAttributeTest : 'schema-attribute' '(' attributeDeclaration ')' ;
attributeDeclaration : attributeName ;
elementTest : 'element' '(' (elementNameOrWildcard (',' typeName '?'?)?)? ')' ;
elementNameOrWildcard : elementName | '*' ;
schemaElementTest : 'schema-element' '(' elementDeclaration ')' ;
elementDeclaration : elementName ;
attributeName : eqName ;
elementName : eqName ;
simpleTypeName : typeName ;
typeName : eqName ;
functionTest : annotation* (AnyFunctionTest | typedFunctionTest) ;
AnyFunctionTest : 'function' '(' '*' ')' ;         // Terminal rule
typedFunctionTest :
        'function' '(' (sequenceType (',' sequenceType)*)? ')' 'as' sequenceType ;
mapTest : AnyMapTest | typedMapTest ;
AnyMapTest : 'map' '(' '*' ')' ;                   // Terminal rule
typedMapTest : 'map' '(' atomicOrUnionType ',' sequenceType ')' ;
arrayTest : AnyArrayTest | typedArrayTest ;
AnyArrayTest : 'array' '(' '*' ')' ;               // Terminal rule
typedArrayTest : 'array' '(' sequenceType ')' ;
parenthesizedItemType : '(' itemType ')' ;
uriLiteral : StringLiteral ;
eqName : qName | uriQualifiedName ;

// Terminals:
IntegerLiteral : Digits ;
DecimalLiteral : ('.' Digits) | (Digits '.' [0-9]*) /* ws: explicit */ ;
DoubleLiteral :
        (('.' Digits) | (Digits ('.' [0-9]*)?)) [eE] [+-]? Digits
        /* ws: explicit */ ;
StringLiteral :
        ('"'  (PredefinedEntityRef | CharRef | EscapeQuot | [^"&])* '"')   |
        ('\'' (PredefinedEntityRef | CharRef | EscapeApos | [^'&])* '\'')
        /* ws: explicit */ ;
uriQualifiedName : bracedURILiteral ncName /* ws: explicit */ ;
bracedURILiteral :
        'Q' '{' (PredefinedEntityRef | CharRef | BracedURILiteralInternal)* '}'
        /* ws: explicit */ ;
BracedURILiteralInternal : [^&{}] ;
PredefinedEntityRef :
        '&' ('lt' | 'gt' | 'amp' | 'quot' | 'apos') ';'      // Terminal rule
        /* ws: explicit */ ;
EscapeQuot : '""' ;
EscapeApos : '\'\'' ;
// ElementContentChar : (Char - [{}<&]) ;  // This is the original rule
// QuotAttrContentChar : (Char - [\"{}<&]) ;  // This is the original rule
// AposAttrContentChar : (Char - [\'{}<&]) ;  // This is the original rule
//ElementContentChar  : (Char ~ [{}<&]) ;
//QuotAttrContentChar : (Char ~ ["{}<&]) ;
//AposAttrContentChar : (Char ~ ['{}<&]) ;
ContentChar:  ~["'{}<&]  ;