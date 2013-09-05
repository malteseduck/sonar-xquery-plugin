/*
Rule names
==========

All parser grammar rules are prefixed with 'px_' in order to comply with the
ANTLR naming scheme for grammar rules. The 'x' letter in the prefix is an
optional field to indicate the status of the grammar rule compared to the
original EBNF production. If missing, the rule is the same as in EBNF. Other
leters are:
m - The grammar rule is a modified version of the original EBNF production
g - The grammar rule is a helper rule needed to achieve different behaviour

The lexer rules were prefixed with 'L_'.

*/

parser grammar XQueryParser;

options {
	output=AST;
	ASTLabelType=XQueryTree;
	TokenLabelType=CommonToken;
	superClass='AbstractXQueryParser';
	tokenVocab=XQueryLexer;
}

tokens {
	// define the tokens from side-lexers (String and XML)
	// in order to avoid token ID overlapping
	L_QuotStringLiteralChar;
	L_AposStringLiteralChar;
	L_AnyChar;
	L_CDataSection;
	   L_DoctypeDecl;
	
	// Imaginary AST tree nodes
	LibraryModule;
	MainModule;
	VersionDecl;
	VersionEncoding;              // container - modified to maintain consistant naming
	VersionValue;                 // container - modified to maintain consistant naming
	ModuleDecl;
	Prolog;
	DefaultNamespaceDecls;        // container
	DefaultNamespaceDecl;
	Setters;                      // container
	Setter;
	NamespaceDecls;               // container
	NamespaceDecl;
	Imports;                      // container
	FTOptionDecls;                // container
	SchemaImport;
	SchemaPrefix;
	SchemaNamespace;
	SchemaAtHints;
	NamespaceName;                // container
	DefaultElementNamespace;
	ModuleAtHints;                      // container
	ModuleImport;
	ModuleNamespace;
	BaseURIDecl;
	OrderedDecls;                 // container
	VarDecl;
	VarType;                      // container
	VarValue;
	VarDefaultValue;
	VarConstantDecl;              // container
	VarVariableDecl;              // container
	FunctionDecl;
	ParamList;                    // container
	ReturnType;                   // container
	OptionDecl;
	TypeDeclaration;
	Param;
	EnclosedExpr;
	QueryBody;
	
	UnaryExpr;
	
	DirElemConstructor;
	DirAttributeList;
	DirAttributeValue;
	DirElemContent;
	CommonContent;
	
	SequenceType;
	EmptySequenceTest;
	KindTest;
	ItemTest;
	FunctionTest;
	//TODO: remove after Sausalito September release
	AtomicType;
	AtomicOrUnionType;
	
	StringLiteral;
	ElementContentChar;
	AttributeValueChar;
	QName;
	
	BlockExpr;
	
	// Mark Logic
	BinaryTest;
	
	// Additions added to support Sonar rules engine
	XQuery;
	ModulePrefix;
	DirComConstructor;
	ParenthesizedExpr;
	VarName;	
	ParamName;
	FLOWRExpr;                    
	ForClause;
	ForName;
	ForType;
	ForAt;
	ForBinding;
	LetClause;
	LetName;
	LetType;
	LetBinding;
	WhereClause;
	OrderByClause;
	OrderSpecList;
	OrderSpec;
	OrderModifier;
	ReturnClause;
	PathExpr;
	IfExpr;   
	IfPredicate;
	IfThen;
	IfElse; 
	FunctionName;
	FunctionBody;     
	FunctionCall;
	ArgumentList;
	Argument;
	TypeswitchExpr;
	TypeswitchPredicate;
	TypeswitchCases;
	CaseClause;
	CaseName;
	CaseType;
	CaseReturn;
	TypeswitchDefault;	
	PredicateList;
	Predicate;
	TryCatchExpr;
	TryClause;
	CatchClauses;
	CatchClause;
	CatchExpr;
	CatchError;
}

@parser::header {
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

@parser::members {
	// dummy list for warning elimination
	List<Stack<Object>> dummy1 = new ArrayList<Stack<Object>>();
	Map<Object, Object> dummy2 = new HashMap<Object, Object>();
}

// ******************************
// XQuery 3.0 Productions
// http://www.w3.org/TR/xquery-30
// ******************************

//[1]
// Update this to support multiple transactions in a script separated by "SEMICOLON"
// Even though this is only MarkLogic there is no way to check without the version
p_Module
        : mt+=pg_ModuleTransaction (SEMICOLON mt+=pg_ModuleTransaction)* EOF -> ^(XQuery $mt*) 
        ;
        
pg_ModuleTransaction
        : vd=p_VersionDecl?
            (
              lm=p_LibraryModule[$vd.tree] -> {$lm.tree}
            | mm=p_MainModule[$vd.tree]    -> {$mm.tree}
            )
        ;

//[2]
p_VersionDecl
        : k=XQUERY {ak($k);} ((k=ENCODING {ak($k);} enc=p_StringLiteral) | 
        			 (k=VERSION {ak($k);} ver=p_StringLiteral {setLanguageVersion(((XQueryTree)ver.getTree()).getChild(0).getText());} (k=ENCODING {ak($k);} enc=p_StringLiteral)?)) SEMICOLON?
                -> ^(VersionDecl ^(VersionValue $ver?) ^(VersionEncoding $enc?))
        ;

//[3]
p_MainModule [CommonTree vd]
        : pm_Prolog pm_QueryBody
                -> ^(MainModule {$vd} pm_Prolog ^(QueryBody pm_QueryBody))
        ;

//[4]
p_LibraryModule [CommonTree vd]
        : p_ModuleDecl pm_Prolog
                -> ^(LibraryModule {$vd} p_ModuleDecl pm_Prolog)
        ;

//[5]
p_ModuleDecl
        : k+=MODULE k+=NAMESPACE {ak($k);} p_NCName EQUAL p_StringLiteral SEMICOLON
                ->  ^(ModuleDecl ^(ModulePrefix p_NCName) ^(ModuleNamespace p_StringLiteral))
        ;

//[6]
// The SEMICOLON was pushed back in all the Prolog declarations
// in order to be contained by the declaration trees.
pm_Prolog
        : ((dnd+=pm_DefaultNamespaceDecl | s+=p_Setter | nd+=pm_NamespaceDecl | i+=p_Import | fto+=pm_FTOptionDecl))* od+=pg_OrderedDecl*
                ->  ^(Prolog
                                ^(DefaultNamespaceDecls $dnd*)
                                ^(Setters $s*)
                                ^(NamespaceDecls $nd*)
                                ^(Imports $i*)
                                ^(FTOptionDecls $fto*)
                                ^(OrderedDecls $od*)
                     )
        ;

// *************************************************
// This is not in the EBNF grammar.
// A special node is needed to keep track of the prolog
// declarations for which the order is important.
pg_OrderedDecl
        : pm_ContextItemDecl
        | pm_AnnotatedDecl
        | pm_OptionDecl
        ;
// *************************************************

//[7] covered by the SEMICOLON lexer rule
//Separator ::= ";"

//[8]
p_Setter
        : pm_BoundarySpaceDecl
        | pm_DefaultCollationDecl
        | pm_BaseURIDecl
        | pm_ConstructionDecl
        | pm_OrderingModeDecl
        | pm_EmptyOrderDecl
        | {lc(XQU)}?=> pm_RevalidationDecl
        | pm_CopyNamespacesDecl
        | pm_DecimalFormatDecl
        ;

//[9]
pm_BoundarySpaceDecl    
        : k=DECLARE! {ak($k);} k=BOUNDARY_SPACE {ak($k);} ( (k=PRESERVE {ak($k);}) | (k=STRIP {ak($k);}) ) SEMICOLON
        ;

//[10]
pm_DefaultCollationDecl
        : k=DECLARE! {ak($k);} k=DEFAULT {ak($k);} k=COLLATION {ak($k);} p_StringLiteral SEMICOLON
        ;
        
//[11]
pm_BaseURIDecl
        : k=DECLARE {ak($k);} k=BASE_URI {ak($k);} sl=p_StringLiteral SEMICOLON
                -> ^(BaseURIDecl $sl)
        ;

//[12]
pm_ConstructionDecl
        : k=DECLARE! {ak($k);} k=CONSTRUCTION {ak($k);} ( (k=STRIP | k=PRESERVE) {ak($k);} ) SEMICOLON
        ;

//[13]
pm_OrderingModeDecl
        : k=DECLARE! {ak($k);} k=ORDERING {ak($k);} ( (k=ORDERED | k=UNORDERED) {ak($k);} ) SEMICOLON
        ;

//[14]
pm_EmptyOrderDecl
        : k=DECLARE! {ak($k);} k=DEFAULT {ak($k);} k=ORDER {ak($k);} k=EMPTY {ak($k);} ( (k=GREATEST | k=LEAST) {ak($k);} ) SEMICOLON
        ;

//[15]
pm_CopyNamespacesDecl
        : k=DECLARE! {ak($k);} k=COPY_NAMESPACES {ak($k);} p_PreserveMode COMMA p_InheritMode SEMICOLON
        ;

//[16]
p_PreserveMode
        : (k+=PRESERVE | k+=NO_PRESERVE) {ak($k);}
        ;

//[17]
p_InheritMode
        : (k+=INHERIT | k+=NO_INHERIT) {ak($k);}
        ;
        
//[18]
pm_DecimalFormatDecl
        : k=DECLARE! {ak($k);} ((k=DECIMAL_FORMAT {ak($k);} p_QName) | (k=DEFAULT {ak($k);} k=DECIMAL_FORMAT {ak($k);})) (p_DFPropertyName EQ p_StringLiteral)* SEMICOLON
        ;

//[19]
p_DFPropertyName
        : k=DECIMAL_SEPARATOR
        | k=GROUPING_SEPARATOR
        | k=INFINITY
        | k=MINUS_SIGN
        | k=NAN
        | k=PERCENT
        | k=PER_MILLE
        | k=ZERO_DIGIT
        | k=DIGIT
        | k=PATTERN_SEPARATOR
          {ak($k);}
        ;

//[20]
p_Import
        : pm_SchemaImport 
        | pm_ModuleImport
        ;
        
//[21]
pm_SchemaImport
        : k=IMPORT {ak($k);} k=SCHEMA {ak($k);} sp=p_SchemaPrefix? us=p_StringLiteral (k=AT {ak($k);} ah+=p_StringLiteral (COMMA ah+=p_StringLiteral)*)? SEMICOLON
                -> ^(SchemaImport ^(SchemaPrefix $sp?) ^(SchemaNamespace $us) ^(SchemaAtHints $ah*))
        ;

//[22]
p_SchemaPrefix 
        : k=NAMESPACE {ak($k);} nn=p_NCName EQUAL
                -> ^(NamespaceName $nn)
        | k=DEFAULT {ak($k);} k=ELEMENT {ak($k);} k=NAMESPACE {ak($k);}
                -> DefaultElementNamespace
        ;

//[23]
pm_ModuleImport
        : k=IMPORT {ak($k);} k=MODULE {ak($k);} (k=NAMESPACE {ak($k);} nn=p_NCName EQUAL)? us=p_StringLiteral (k=AT {ak($k);} ah+=p_StringLiteral (COMMA ah+=p_StringLiteral)*)? SEMICOLON
                -> ^(ModuleImport ^(ModulePrefix $nn?) ^(ModuleNamespace $us) ^(ModuleAtHints $ah*))
        ;

//[24]
pm_NamespaceDecl
        : k=DECLARE {ak($k);} k=NAMESPACE {ak($k);} nn=p_NCName EQUAL us=p_StringLiteral SEMICOLON 
                -> ^(NamespaceDecl $nn $us)
        ;

//[25]
pm_DefaultNamespaceDecl
        : k=DECLARE! {ak($k);} k=DEFAULT {ak($k);} (k=ELEMENT | k=FUNCTION) {ak($k);} k=NAMESPACE {ak($k);} p_StringLiteral SEMICOLON
        ;

//[26]
pm_AnnotatedDecl
        : k=DECLARE! {ak($k);} p_Annotation* pg_AnnotatedDecl SEMICOLON!
        ;
pg_AnnotatedDecl
        : pm_VarDecl
        | pm_FunctionDecl
        | {lc(ZORBA)}?=> p_CollectionDecl
        | {lc(ZORBA)}?=> p_IndexDecl
        | {lc(ZORBA)}?=> p_ICDecl
        ;

//[27]
p_Annotation
        : ANN_PERCENT p_QName (LPAREN p_Literal (COMMA p_Literal)* RPAREN)?
        ;

//[28]
// Added support for "private" variables in MarkLogic
pm_VarDecl
        : ({lc(MLS)}?=> pr=PRIVATE {ak($pr);})? k=VARIABLE {ak($k);} DOLLAR qn=p_QName td=p_TypeDeclaration? ((BIND vv=p_VarValue) | (k=EXTERNAL {ak($k);} (BIND vdv=p_VarDefaultValue)?))
                -> ^(VarDecl ^( VarName $qn ) ^( VarType $td? ) ^( VarValue $vv? ) ^(VarDefaultValue $vdv?))
        ;

//[29]
p_VarValue
        : p_ExprSingle
        ;

//[30]
p_VarDefaultValue
        : p_ExprSingle
        ;

//[31]
pm_ContextItemDecl
        : k=DECLARE! {ak($k);} k=CONTEXT {ak($k);} k=ITEM {ak($k);} (k=AS {ak($k);} p_ItemType)? ((BIND p_VarValue) | (k=EXTERNAL {ak($k);} (BIND p_VarDefaultValue)?)) SEMICOLON
        ;

//[32]
//[32] new XQuery Scripting proposal
// Added support for "private" functions in MarkLogic
pm_FunctionDecl
        : ({lc(XQU)}?=> k=UPDATING {ak($k);})? ({lc(MLS)}?=> pr=PRIVATE {ak($pr);})? k=FUNCTION {ak($k);} qn=p_FQName LPAREN pl=p_ParamList? RPAREN (k=AS {ak($k);} st=p_SequenceType)? (LBRACKET soe=p_StatementsAndOptionalExpr RBRACKET | k=EXTERNAL {ak($k);} )
                -> ^( FunctionDecl $pr? ^( FunctionName $qn ) $pl? ^( ReturnType $st? ) ^( FunctionBody $soe? ) )
        ;

//[33]
p_ParamList
        : p+=p_Param (COMMA p+=p_Param)*
                -> ^( ParamList $p+ )
        ;
        
//[34]
p_Param
        : DOLLAR qn=p_QName td=p_TypeDeclaration?
                -> ^( Param ^( ParamName $qn ) $td? )
        ;

//[35]
pm_FunctionBody
        : p_EnclosedExpr
        ;


//[36]
p_EnclosedExpr
        : LBRACKET p_Expr RBRACKET
                -> ^(EnclosedExpr p_Expr)
        ;

//[37]
pm_OptionDecl
        : k=DECLARE {ak($k);} k=OPTION {ak($k);} qn=p_QName val=p_StringLiteral SEMICOLON
                -> ^( OptionDecl $qn $val )
        ;

//[38]
pm_QueryBody
        : {lc(XQS)}?=> p_Program
        | p_Expr
        ;

//[39]
p_Expr
        : p_ExprSingle (COMMA! p_ExprSingle)*
//TODO: disabled because of XQuery 3.0 grammar changes and ambiguities
//          ({lc(MLS)}?=> (SEMICOLON p_ConcatExpr)* | /* nothing */)
        ;

//[40]
//[22] new XQuery Scripting proposal
p_ExprSingle
        : ((FOR | LET) DOLLAR) => p_FLWORExpr
        | (IF LPAREN) =>          p_IfExpr
        | (TYPESWITCH LPAREN) =>  p_SwitchExpr
        | (TYPESWITCH LPAREN) =>  p_TypeswitchExpr
        | (TRY LBRACKET) =>       p_TryCatchExpr
        | {lc(ZORBA)}?=> p_EvalExpr
        | p_ExprSimple
        ;
        
//[41]
p_FLWORExpr
        : ic=p_InitialClause imc+=p_IntermediateClause* rc=p_ReturnClause
                -> ^(FLOWRExpr $ic $imc* $rc )
        ;

//[42]
p_InitialClause
        : p_ForClause 
        | p_LetClause 
        | p_WindowClause
        ;

//[43]
p_IntermediateClause
        : p_InitialClause 
        | p_WhereClause 
        | p_GroupByClause 
        | p_OrderByClause 
        | p_CountClause
        ;

//[44]
//[35] Full Text 1.0
p_ForClause
        : k=FOR {ak($k);} fb+=p_ForBinding (COMMA fb+=p_ForBinding)*
                -> ^( ForClause $fb+ )?
        ;

//[45]
p_ForBinding
        : DOLLAR vn=p_VarName td=p_TypeDeclaration? ae=p_AllowingEmpty? pv=p_PositionalVar? sv=p_FTScoreVar? k=IN {ak($k);} es=p_ExprSingle
                -> ^( ForName $vn ) ^( ForType $td )? $ae? ^( ForAt $pv? )? $sv? ^( ForBinding $es )
        ;

//[46]
p_AllowingEmpty
        : k=ALLOWING {ak($k);} k=EMPTY {ak($k);}
        ;

//[47]
p_PositionalVar
        : k=AT {ak($k);} DOLLAR p_VarName
        ;

//[48]
p_LetClause
        : k=LET {ak($k);} lb+=p_LetBinding (COMMA lb+=p_LetBinding)*
                -> ^( LetClause $lb+ )?
        ;

//[49]
//[38] Full Text 1.0
p_LetBinding
        : ( (DOLLAR vn=p_VarName td=p_TypeDeclaration?) | sv=p_FTScoreVar ) BIND es=p_ExprSingle
                -> ^( LetName $vn )? ^( LetType $td )? $sv? ^( LetBinding $es )
        ;

//[50]
p_WindowClause
        : k=FOR {ak($k);} (p_TumblingWindowClause | p_SlidingWindowClause)
        ;
        
//[51]
p_TumblingWindowClause
        : k=TUMBLING {ak($k);} k=WINDOW {ak($k);} DOLLAR p_VarName p_TypeDeclaration? k=IN {ak($k);} p_ExprSingle p_WindowStartCondition p_WindowEndCondition?
        ;

//[52]
p_SlidingWindowClause
        : k=SLIDING {ak($k);} k=WINDOW {ak($k);} DOLLAR p_VarName p_TypeDeclaration? k=IN {ak($k);} p_ExprSingle p_WindowStartCondition p_WindowEndCondition?
        ;

//[53]
p_WindowStartCondition
        : k=START {ak($k);} p_WindowVars k=WHEN {ak($k);} p_ExprSingle
        ;

//[54]
p_WindowEndCondition
        : (k=ONLY {ak($k);})? k=END {ak($k);} p_WindowVars k=WHEN {ak($k);} p_ExprSingle
        ;

//[55]
p_WindowVars
        : (DOLLAR p_CurrentItem)? p_PositionalVar? (k=PREVIOUS {ak($k);} DOLLAR p_PreviousItem)? (k=NEXT {ak($k);} DOLLAR p_NextItem)?
        ;

//[56]
p_CurrentItem
        : p_QName
        ;

//[57]
p_PreviousItem
        : p_QName
        ;

//[58]
p_NextItem
        : p_QName
        ;

//[59]
p_CountClause
        : k=COUNT {ak($k);} DOLLAR p_VarName
        ;
        
//[60]
p_WhereClause
        : k=WHERE {ak($k);} e=p_ExprSingle
                -> ^( WhereClause $e )
        ;

//[61]
p_GroupByClause
        : k=GROUP {ak($k);} k=BY {ak($k);} p_GroupingSpecList
        ;

//[62]
p_GroupingSpecList
        : p_GroupingSpec (COMMA p_GroupingSpec)*
        ;

//[63]
p_GroupingSpec
        : DOLLAR p_VarName (k=COLLATION {ak($k);} p_StringLiteral)?
        ;

//[64]
p_OrderByClause
        : ((k+=ORDER k+=BY) | (k+=STABLE k+=ORDER k+=BY)) {ak($k);} osl=p_OrderSpecList
                -> ^( OrderByClause $osl )
        ;

//[65]
p_OrderSpecList
        : os+=p_OrderSpec (COMMA os+=p_OrderSpec)*
                -> ^( OrderSpecList $os+ )
        ;

//[66]
p_OrderSpec
        : es=p_ExprSingle om=p_OrderModifier
                -> ^( OrderSpec $es $om? )
        ;

//[67]
p_OrderModifier
        : (k+=ASCENDING | k+=DESCENDING)? (k+=EMPTY (k+=GREATEST | k+=LEAST))? (k+=COLLATION col=p_StringLiteral)? {ak($k);}
                -> ^( OrderModifier $k+ )?
        ;

//[68]
p_ReturnClause
        : k=RETURN {ak($k);} e=p_ExprSingle
                -> ^( ReturnClause $e )
        ;

//[69]
p_QuantifiedExpr
        : (k=SOME | k=EVERY) {ak($k);} DOLLAR p_VarName p_TypeDeclaration? k=IN {ak($k);} p_ExprSingle (COMMA DOLLAR p_QName p_TypeDeclaration? k=IN {ak($k);} p_ExprSingle)* k=SATISFIES {ak($k);} p_ExprSingle
        ;

//[70]
p_SwitchExpr
        : k=SWITCH {ak($k);} LPAREN p_Expr RPAREN p_SwitchCaseClause+ k=DEFAULT {ak($k);} k=RETURN {ak($k);} p_ExprSingle
        ;

//[71]
p_SwitchCaseClause
        : (k=CASE {ak($k);} p_SwitchCaseOperand)+ k=RETURN {ak($k);} p_ExprSingle
        ;

//[72]
p_SwitchCaseOperand
        : p_ExprSingle
        ;

//[73]
p_TypeswitchExpr
        : k=TYPESWITCH {ak($k);} LPAREN exp=p_Expr RPAREN cc+=p_CaseClause+ k=DEFAULT {ak($k);} (DOLLAR var=p_VarName)? k=RETURN {ak($k);} dexp=p_ExprSingle
                -> ^( TypeswitchExpr ^( TypeswitchPredicate $exp ) ^( TypeswitchCases $cc+ ) ^( TypeswitchDefault $var? $dexp ) )
        ;

//[74]
p_CaseClause
        : k=CASE {ak($k);} (DOLLAR var=p_VarName k=AS {ak($k);})? st=p_SequenceTypeUnion k=RETURN {ak($k);} rexp=p_ExprSingle
                -> ^( CaseClause ^( CaseName $var? ) ^( CaseType $st ) ^( CaseReturn $rexp ) )
        ;

//[75]
p_SequenceTypeUnion
        : p_SequenceType (VBAR p_SequenceType)*
        ;

//[76]
p_IfExpr
        : k=IF {ak($k);} LPAREN e=p_Expr RPAREN k=THEN {ak($k);} tes=p_ExprSingle k=ELSE {ak($k);} ees=p_ExprSingle
                -> ^(IfExpr ^( IfPredicate $e ) ^( IfThen $tes ) ^( IfElse $ees ) )
        ;

//[77]
p_TryCatchExpr
        : tc=p_TryClause cc+=pm_CatchClause+
                -> ^( TryCatchExpr $tc ^( CatchClauses $cc+ ) )
        ;

//[78]
p_TryClause
        : k=TRY {ak($k);} LBRACKET tte=p_TryTargetExpr RBRACKET
                -> ^( TryClause $tte )
        ;

//[79]
p_TryTargetExpr
        : p_Expr
        ;

//[80]
// Updated so it works with MarkLogic - the catch error list doesn't have parens in 3.0?
// It does in 1.0...
pm_CatchClause
        : k=CATCH {ak($k);} LPAREN DOLLAR vn=p_VarName RPAREN LBRACKET ce=p_Expr RBRACKET
                -> ^( CatchClause ^( CatchError $vn ) ^( CatchExpr $ce ) )
        ;

//[81]
p_CatchErrorList
        : p_NameTest (VBAR p_NameTest)*
        | {lc(MLS)}?=> (/* nothing */)
        ;

//[82]
p_OrExpr
        : p_AndExpr ( k=OR {ak($k);} p_AndExpr )*
        ;

//[83]
p_AndExpr
        : p_ComparisonExpr ( k=AND {ak($k);} p_ComparisonExpr )*
        ;

//[84]
//[50] Full Text 1.0
p_ComparisonExpr
        : p_FTContainsExpr ( (p_ValueComp | p_GeneralComp | p_NodeComp) p_FTContainsExpr )?
        ;

//[85]
p_RangeExpr
        : p_AdditiveExpr ( k=TO {ak($k);} p_AdditiveExpr )?
        ;

//[86]
p_AdditiveExpr
        : p_MultiplicativeExpr ( (PLUS | MINUS) p_MultiplicativeExpr )*
        ;

//[87]
p_MultiplicativeExpr
        : p_UnionExpr ( (STAR | (k=DIV | k=IDIV | k=MOD) {ak($k);}) p_UnionExpr )*
        ;

//[88]
p_UnionExpr
        : p_IntersectExceptExpr ( (k=UNION {ak($k);} | VBAR) p_IntersectExceptExpr )*
        ;

//[89]
p_IntersectExceptExpr
        : p_InstanceofExpr ( (k=INTERSECT | k=EXCEPT) {ak($k);} p_InstanceofExpr )*
        ;

//[90]
p_InstanceofExpr
        : p_TreatExpr ( k=INSTANCE {ak($k);} k=OF {ak($k);} p_SequenceType)?
        ;

//[91]
p_TreatExpr
        : p_CastableExpr ( k=TREAT {ak($k);} k=AS {ak($k);} p_SequenceType )?
        ;
        
//[92]
p_CastableExpr
        : p_CastExpr ( k=CASTABLE {ak($k);} k=AS {ak($k);} p_SingleType )?
        ;
        
//[93]
p_CastExpr
        : p_UnaryExpr ( k=CAST {ak($k);} k=AS {ak($k);} p_SingleType )?
        ;

//[94]
p_UnaryExpr
        : (PLUS | MINUS)* p_ValueExpr
                -> ^(UnaryExpr PLUS* p_ValueExpr)
        ;

//[95]
p_ValueExpr
        : (VALIDATE ( p_ValidationMode | TYPE )?) => p_ValidateExpr
        | p_PathExpr
        | p_ExtensionExpr
        ;

//[96]
p_GeneralComp
        : EQUAL 
        | NOTEQUAL 
        | SMALLER 
        | SMALLEREQ 
        | GREATER 
        | GREATEREQ
        ;

//[97]
p_ValueComp
        : (k=EQ | k=NE | k=LT | k=LE | k=GT | k=GE) {ak($k);}
        ;

//[98]
p_NodeComp
        : k=IS {ak($k);} 
        | SMALLER_SMALLER 
        | GREATER_GREATER
        ;

//[99]
p_ValidateExpr
        : k=VALIDATE {ak($k);} ( p_ValidationMode | k=TYPE {ak($k);} p_TypeName )? LBRACKET p_Expr RBRACKET
        ;

//[100]
p_ValidationMode
        : (k=LAX | k=STRICT) {ak($k);}
        ;

//[101]
p_ExtensionExpr
        : L_Pragma+ LBRACKET p_Expr? RBRACKET
        ;

//[102] /* ws: explicit */
//Pragma ::= "(#" S? EQName (S PragmaContents)? "#)"
//L_Pragma

//[103]
//PragmaContents	   ::=   	(Char* - (Char* '#)' Char*))
//L_Pragma

//[104] /* xgc: leading-lone-slash */
p_PathExpr
        : (SLASH p_RelativePathExpr) => (SLASH rpe=p_RelativePathExpr) 
                -> ^(PathExpr SLASH $rpe?)
        | SLASH 
                -> ^(PathExpr SLASH)
        | SLASH_SLASH rpe=p_RelativePathExpr 
                -> ^(PathExpr SLASH_SLASH $rpe?)
        | rpe=p_RelativePathExpr
                -> ^(PathExpr $rpe?)
        ;

//[105]
p_RelativePathExpr  
        : pm_StepExpr ((SLASH | SLASH_SLASH) pm_StepExpr)*
        ;

//[106]
// Replace with logic from eXist XQuery parser?
pm_StepExpr
        // Gets entity strings and NameTest Wildcarded strings confused...
        : ( QUOT | APOS ) => p_PostfixExpr
        
        // Gets confused on axis steps
        | (pm_ForwardAxis) => p_AxisStep 
        
        // Constructors
        | ( ( ELEMENT | ATTRIBUTE | TEXT | DOCUMENT | PROCESSING_INSTRUCTION | COMMENT | ORDERED | UNORDERED ) LBRACKET ) => p_PostfixExpr
        | ( ( ELEMENT | ATTRIBUTE | PROCESSING_INSTRUCTION | NAMESPACE ) p_QName LBRACKET ) => p_PostfixExpr
        
        // MarkLogic binary constructor        
        | ( BINARY ) => p_PostfixExpr 
                
        // Since these are lexical elements we need to match directly
        | ( L_DirCommentConstructor | L_DirPIConstructor ) => p_PostfixExpr
        
        // The rest
        | ( DOLLAR | SELF | DOT | LPAREN | p_Literal | SMALLER ) => p_PostfixExpr
        | (p_KindTest) => p_AxisStep 
        | (p_QName LPAREN) => p_PostfixExpr        
        | p_AxisStep
        ;

//[107]
// Added '?' to predicate list to enable easier AST rewrite?
p_AxisStep
        : (p_ReverseStep | p_ForwardStep) p_PredicateList?
        ;

//[108]
// Update to have modified forward axis rule
p_ForwardStep
        : pm_ForwardAxis p_NodeTest
        | p_AbbrevForwardStep
        ;

//[109]
// Added support for MarkLogic "property" axis
// April 11, 2012 - Added support for MarkLogic "namespace" axis
pm_ForwardAxis
        : CHILD COLON_COLON
        | DESCENDANT COLON_COLON
        | ATTRIBUTE COLON_COLON
        | SELF COLON_COLON
        | DESCENDANT_OR_SELF COLON_COLON
        | FOLLOWING_SIBLING COLON_COLON
        | FOLLOWING COLON_COLON
        | {lc(MLS)}?=> PROPERTY COLON_COLON 
        | {lc(MLS)}?=> NAMESPACE COLON_COLON 
        ;

//[110]
p_AbbrevForwardStep
        : ATTR_SIGN? p_NodeTest
        ;

//[111]
p_ReverseStep
        : p_ReverseAxis p_NodeTest
        | p_AbbrevReverseStep
        ;

//[112]
p_ReverseAxis
        : PARENT COLON_COLON
        | ANCESTOR COLON_COLON
        | PRECEDING_SIBLING COLON_COLON
        | PRECEDING COLON_COLON
        | ANCESTOR_OR_SELF COLON_COLON
        ;

//[113]
p_AbbrevReverseStep
        : DOT_DOT
        ;

//[114]
p_NodeTest
        : p_KindTest 
        | p_NameTest
        ;

//[115]
p_NameTest
        : (p_Wildcard) => p_Wildcard 
        | (p_NCName COLON) => p_QName
        | (p_NCName) => p_QName
        ;

//[116] /* ws: explicit */
p_Wildcard @init{setWsExplicit(true);}
        : STAR (COLON p_NCName)?
        | p_NCName COLON STAR
        | p_StringLiteral COLON STAR
        ;
        finally {setWsExplicit(false);}

//[117]
p_PostfixExpr
        : p_PrimaryExpr (p_Predicate
//TODO
//          | p_ArgumentList
          )*
        ;

//[118]
p_ArgumentList
        : a+=p_Argument (COMMA a+=p_Argument)*
                -> ^( ArgumentList $a+ )
        ;
        
//[131] - moved from below
p_Argument
        : es=p_ExprSingle 
                -> ^( Argument $es? )
        | ap=p_ArgumentPlaceholder
                -> ^( Argument $ap? )
        ;

//[132] - moved from below
p_ArgumentPlaceholder
        : QUESTION
        ;
        
//[119]
// Update to match "p_ParamList" to try and make AST rewrite work
p_PredicateList
        : p+=p_Predicate (p+=p_Predicate)*
                -> ^( PredicateList $p+ )
        ;

//[120]
p_Predicate
        : LSQUARE e=p_Expr RSQUARE
                -> ^( Predicate $e )
        ;

//[121]
//[30] new XQuery Scripting proposal
//LL grammar
p_PrimaryExpr
        : (LPAREN) => p_ParenthesizedExpr
        | p_Literal
        | p_VarRef
        | p_ContextItemExpr
        | p_FunctionCall
        | p_OrderedExpr
        | p_UnorderedExpr
        | p_Constructor
//TODO
//        | p_FunctionItemExpr
//LL grammar
//        | p_BlockExpr
        ;

//[122]
p_Literal
        : p_NumericLiteral 
        | p_StringLiteral
        ;

//[123]
p_NumericLiteral
        : L_IntegerLiteral 
        | L_DecimalLiteral 
        | L_DoubleLiteral
        ;
        
//[124]
p_VarRef
        : DOLLAR p_VarName
        ;

//[125]
p_VarName
        : p_QName
        ;

//[126]
p_ParenthesizedExpr
        : LPAREN e=p_Expr? RPAREN
            -> ^( ParenthesizedExpr $e? )
        ;

//[127]
p_ContextItemExpr
        : DOT
        ;

//[128]
p_OrderedExpr
        : k=ORDERED {ak($k);} LBRACKET p_Expr RBRACKET
        ;

//[129]
p_UnorderedExpr
        : k=UNORDERED {ak($k);} LBRACKET p_Expr RBRACKET
        ;

//[130] /* xgs: reserved-function-names */ - resolved through p_FQName production
//      /* gn: parens */
p_FunctionCall
        : fn=p_FQName LPAREN al=p_ArgumentList? RPAREN 
                -> ^(FunctionCall ^( FunctionName $fn ) $al? )
        ;

//[133]
p_Constructor
        : p_DirectConstructor
        | p_ComputedConstructor
        ;

//[134]
p_DirectConstructor
        : p_DirElemConstructor
        | p_DirCommentConstructor
        | p_DirPIConstructor        
        ;

//[135] /* ws: explicit */ - resolved through the XMLLexer
p_DirElemConstructor //@init {setWsExplicit(true);}
        : SMALLER { pushXMLLexer(); }
          p_QName 
          p_DirAttributeList 
          (EMPTY_CLOSE_TAG | (GREATER pm_DirElemContent* CLOSE_TAG p_QName S? GREATER))
                -> ^(DirElemConstructor ^(DirAttributeList p_DirAttributeList*) ^(DirElemContent pm_DirElemContent*))
        ;
        finally { popLexer(); }

//[136] /* ws: explicit */ - resolved through the XMLLexer
p_DirAttributeList
        : (S (p_QName S? EQUAL S? p_DirAttributeValue)?)*
        ;

//[137] /* ws: explicit */ - resolved through the XMLLexer
p_DirAttributeValue
        : (QUOT (ESCAPE_QUOT | APOS | p_QuotAttrValueContent)* QUOT)
                -> ^(DirAttributeValue p_QuotAttrValueContent*)
        | (APOS (ESCAPE_APOS | QUOT | p_AposAttrValueContent)* APOS)
                -> ^(DirAttributeValue p_AposAttrValueContent*)
        ;

//[138]
p_QuotAttrValueContent
        : p_QuotAttrContentChar 
        | pm_CommonContent
        ;

//[139]
p_AposAttrValueContent
        : p_AposAttrContentChar 
        | pm_CommonContent
        ;

//[140]
pm_DirElemContent
        : p_DirectConstructor
        | p_CDataSection
        | pm_CommonContent
        | p_ElementContentChar
        ;

//[141]
//[24] new XQuery Scripting proposal
pm_CommonContent
        : L_PredefinedEntityRef
        | L_CharRef
        | ESCAPE_LBRACKET
        | ESCAPE_RBRACKET
        | pg_EnclosedExprXml
        ;

// *************************************************
// This is not in the EBNF grammar.
// This is needed in order to switch the lexer from
// XML back to XQuery
//[24] new XQuery Scripting proposal
pg_EnclosedExprXml
        :   LBRACKET { pushXQueryLexer(); }
            p_StatementsAndOptionalExpr
            RBRACKET { popLexer(); }
        ;
// *************************************************

//[142] /* ws: explicit */
p_DirCommentConstructor
        : con=L_DirCommentConstructor
                -> ^( DirComConstructor $con )
        ;   

//[143] /* ws: explicit */
//L_DirCommentContents

//[144] /* ws: explicit */
p_DirPIConstructor
        : L_DirPIConstructor
        ;    

//[145] /* ws: explicit */
//L_DirPIContents

//[146] /* ws: explicit */
p_CDataSection
        : L_CDataSection
        ;

//[147] /* ws: explicit */
//L_CDataSectionContents

//[148]
p_ComputedConstructor   
        : pm_CompDocConstructor
        | pm_CompElemConstructor
        | pm_CompAttrConstructor
        | p_CompNamespaceConstructor
        | p_CompTextConstructor
        | pm_CompCommentConstructor
        | pm_CompPIConstructor
        | {lc(MLS)}?=> p_CompBinaryConstructor
        ;

//[149]
//[26] new XQuery Scripting proposal
pm_CompDocConstructor
        : k=DOCUMENT {ak($k);} LBRACKET p_StatementsAndOptionalExpr RBRACKET
        ;
        
//[150]
pm_CompElemConstructor
        : k=ELEMENT {ak($k);} (p_QName | (LBRACKET p_Expr RBRACKET)) LBRACKET pm_ContentExpr RBRACKET
        ;

//[151]
//[25] new XQuery Scripting proposal
pm_ContentExpr
        : p_StatementsAndOptionalExpr
        ;

//[152]
//[27] new XQuery Scripting proposal
pm_CompAttrConstructor
        : k=ATTRIBUTE {ak($k);} (p_QName | (LBRACKET p_Expr RBRACKET)) LBRACKET p_StatementsAndOptionalExpr RBRACKET
        ;

//[153]
p_CompNamespaceConstructor
        : k=NAMESPACE {ak($k);} (p_Prefix | (LBRACKET p_PrefixExpr RBRACKET)) LBRACKET p_URIExpr? RBRACKET
        ;

//[154]
p_Prefix
        : p_NCName
        ;

//[155]
p_PrefixExpr
        : p_Expr
        ;

//[156]
p_URIExpr
        : p_Expr
        ;

//[157]
p_CompTextConstructor
        : k=TEXT {ak($k);} LBRACKET p_Expr RBRACKET
        ;

// MarkLogic Server Extension
p_CompBinaryConstructor
        : k=BINARY {ak($k);} LBRACKET p_Expr RBRACKET
        ;

//[158]
//[29] new XQuery Scripting proposal
pm_CompCommentConstructor
        : k=COMMENT {ak($k);} LBRACKET p_StatementsAndOptionalExpr RBRACKET
        ;

//[159]
//[28] new XQuery Scripting proposal
pm_CompPIConstructor
        : k=PROCESSING_INSTRUCTION {ak($k);} (p_NCName | (LBRACKET p_Expr RBRACKET)) LBRACKET p_StatementsAndOptionalExpr RBRACKET
        ;

//[160]
//TODO
//p_FunctionItemExpr
//        : p_LiteralFunctionItem | p_InlineFunction
//        ;

//[161] /* xgc: reserved-function-names */
//TODO
//p_LiteralFunctionItem
//        : p_FQName HASH L_IntegerLiteral
//        ;

//[162]
//TODO
//p_InlineFunction
//        : FUNCTION LPAREN p_ParamList? RPAREN (k=AS {ak($k);} p_SequenceType)? p_EnclosedExpr
//        ;

//[163]
p_SingleType
        : p_AtomicOrUnionType QUESTION?
        ;

//[164]
p_TypeDeclaration
        : k=AS {ak($k);} st=p_SequenceType
                -> ^(TypeDeclaration $st)
        ;

//[165]
p_SequenceType
        : k=EMPTY_SEQUENCE {ak($k);} l=LPAREN r=RPAREN
                -> ^(SequenceType ^(EmptySequenceTest $k $l $r))
        | it=p_ItemType ((p_OccurrenceIndicator) => oi=p_OccurrenceIndicator)?
                -> ^(SequenceType $it $oi?)
        ;

//[166] /* xgs: occurrence-indicators */ - resolved in the p_SequenceType production
p_OccurrenceIndicator   
        : QUESTION 
        | STAR 
        | PLUS
        ;
        
//[167]
p_ItemType
        : p_KindTest
                -> ^(KindTest p_KindTest)
        | {lc(MLS)}?=> (BINARY LPAREN RPAREN)
                -> ^(BinaryTest BINARY LPAREN RPAREN)
        | (ITEM LPAREN RPAREN)
                -> ^(ItemTest ITEM LPAREN RPAREN)
//TODO
//        | p_FunctionTest
//                -> ^(FunctionTest p_FunctionTest)
        | p_AtomicOrUnionType
        | p_ParenthesizedItemType
        ;

//[168]
p_AtomicOrUnionType
        : p_QName
                -> ^(AtomicOrUnionType p_QName)
        ;

//[169]
p_KindTest
        : p_DocumentTest
        | p_ElementTest
        | p_AttributeTest
        | p_SchemaElementTest
        | p_SchemaAttributeTest
        | p_PITest
        | p_CommentTest
        | p_TextTest
        | p_NamespaceNodeTest
        | p_AnyKindTest
        ;

//[170]
p_AnyKindTest
        : NODE LPAREN RPAREN
        ;

//[171]
p_DocumentTest
        : DOCUMENT_NODE LPAREN (p_ElementTest | p_SchemaElementTest)? RPAREN
        ;

//[172]
p_TextTest
        : TEXT LPAREN RPAREN
        ;

//[173]
p_CommentTest
        : COMMENT LPAREN RPAREN
        ;

//[174]
p_NamespaceNodeTest
        : NAMESPACE_NODE LPAREN RPAREN
        ;

//[175]
p_PITest
        : PROCESSING_INSTRUCTION LPAREN (p_NCName | p_StringLiteral)? RPAREN
        ;

//[176]
p_AttributeTest
        : ATTRIBUTE LPAREN (p_AttribNameOrWildcard (COMMA p_TypeName)?)? RPAREN
        ;

//[177]
p_AttribNameOrWildcard  
        : p_AttributeName 
        | STAR
        ;

//[178]
p_SchemaAttributeTest
        : SCHEMA_ATTRIBUTE LPAREN p_AttributeDeclaration RPAREN
        ;

//[179]
p_AttributeDeclaration
        : p_AttributeName
        ;

//[180]
p_ElementTest
        : ELEMENT LPAREN (p_ElementNameOrWildcard (COMMA p_TypeName QUESTION?)?)? RPAREN
        ;

//[181]
p_ElementNameOrWildcard
        : p_QName 
        | STAR ;

//[182]
p_SchemaElementTest
        : SCHEMA_ELEMENT LPAREN p_ElementDeclaration RPAREN
        ;

//[183]
p_ElementDeclaration
        : p_ElementName
        ;

//[184]
p_AttributeName
        : p_QName
        ;

//[185]
p_ElementName
        : p_QName
        ;

//[186]
p_TypeName
        : p_QName
        ;

//[187]
p_FunctionTest
        : p_Annotation* (p_AnyFunctionTest | p_TypedFunctionTest)
        ;

//[188]
p_AnyFunctionTest
        : FUNCTION LPAREN STAR RPAREN
        ;

//[189]
p_TypedFunctionTest
        : FUNCTION LPAREN (p_SequenceType (COMMA p_SequenceType)*)? RPAREN AS p_SequenceType
        ;

//[190]
p_ParenthesizedItemType
        : LPAREN p_ItemType RPAREN
        ;

//[191]
//URILiteral ::= StringLiteral

//[192]
//TODO
//EQName ::= QName | URIQualifiedName

//[193] /* ws: explicit */
//TODO
//URIQualifiedName ::= URILiteral ":" NCName


// ****************
// Terminal Symbols
// ****************

//[194]
//L_IntegerLiteral

//[195] /* ws: explicit */
//L_DecimalLiteral

//[196] /* ws: explicit */
//L_DoubleLiteral

//[197] /* ws: explicit */
p_StringLiteral
        : QUOT {pushStringLexer(false);} qc=p_QuotStringLiteralContent QUOT { popLexer(); }
                -> ^( StringLiteral $qc* )
        | APOS {pushStringLexer(true);} ac=p_AposStringLiteralContent APOS { popLexer(); }
                -> ^( StringLiteral $ac* )
        ;

// *************************************************
// This is not in the EBNF grammar.
// A special node is needed to keep track of the prolog
// declarations for which the order is important.
p_QuotStringLiteralContent
        : (ESCAPE_QUOT | L_CharRef | L_PredefinedEntityRef | ~(QUOT | AMP))*
        ;
// *************************************************

// *************************************************
// This is not in the EBNF grammar.
// A special node is needed to keep track of the prolog
// declarations for which the order is important.
p_AposStringLiteralContent
        : (ESCAPE_APOS | L_CharRef | L_PredefinedEntityRef | ~(APOS | AMP))*
        ;
// *************************************************

//[198] /* ws: explicit */
//L_PredefinedEntityRef

//[199]
//ESCAPE_QUOT

//[200]
//ESCAPE_APOS

//[201]
p_ElementContentChar
        : L_ElementContentChar
        ;

//[202]
p_QuotAttrContentChar
        : L_QuotAttrContentChar
                -> ^( AttributeValueChar L_QuotAttrContentChar )
        ;

//[203]
p_AposAttrContentChar
        : L_AposAttrContentChar
                -> ^( AttributeValueChar L_AposAttrContentChar )
        ;


//[204] /* ws: explicit */
//      /* gn: comments */
//L_Comment
        
//TODO
//[205] /* xgs: xml-version */
//PITarget ::= [http://www.w3.org/TR/REC-xml#NT-PITarget]

//[206]
//L_CharRef

//[207] /* xgc: xml-version */
p_QName @init {setWsExplicit(true);}
        : n=p_NCName ln=pg_LocalNCName
                -> ^( QName $n $ln? )
        ;
        finally {setWsExplicit(false);}
// additional production used to resolve the function name exceptions
p_FQName @init {setWsExplicit(true);}
        : n=p_FNCName ln=pg_LocalNCName
                -> ^( QName $n $ln? )
        ;
        finally {setWsExplicit(false);}

// rule needed in order to catch the missing
// COLON and restore to non-explicit mode
pg_LocalNCName
        : (COLON p_NCName)?
        ;

//[208] /* xgc: xml-version */
p_NCName
        : L_NCName
        // XQuery 1.0 keywords
        | ANCESTOR | ANCESTOR_OR_SELF | AND | AS | ASCENDING | AT | ATTRIBUTE | BASE_URI | BOUNDARY_SPACE | BY | CASE | CAST | CASTABLE | CHILD | COLLATION | COMMENT | CONSTRUCTION | COPY_NAMESPACES | DECLARE | DEFAULT | DESCENDANT | DESCENDANT_OR_SELF | DESCENDING | DIV | DOCUMENT | DOCUMENT_NODE | ELEMENT | ELSE | EMPTY | EMPTY_SEQUENCE | ENCODING | EQ | EVERY | EXCEPT | EXTERNAL | FOLLOWING | FOLLOWING_SIBLING | FOR | FUNCTION | GE | GREATEST | GT | IDIV | IF | IMPORT | IN | INHERIT | INSTANCE | INTERSECT | IS | ITEM | LAX | LE | LEAST | LET | LT | MOD | MODULE | NAMESPACE | NE | NO_INHERIT | NO_PRESERVE | NODE | OF | OPTION | OR | ORDER | ORDERED | ORDERING | PARENT | PRECEDING | PRECEDING_SIBLING | PRESERVE | PROCESSING_INSTRUCTION | RETURN | SATISFIES | SCHEMA | SCHEMA_ATTRIBUTE | SCHEMA_ELEMENT | SELF | SOME | STABLE | STRICT | STRIP | SWITCH | TEXT | THEN | TO | TREAT | TYPESWITCH | UNION | UNORDERED | VALIDATE | VARIABLE | VERSION | WHERE | XQUERY
        // XQuery 3.0 keywords
        | ALLOWING | CATCH | CONTEXT | COUNT | DECIMAL_FORMAT | DECIMAL_SEPARATOR | DIGIT | END | GROUP | GROUPING_SEPARATOR | INFINITY | MINUS_SIGN | NAMESPACE_NODE | NAN | NEXT | ONLY | PATTERN_SEPARATOR | PERCENT | PER_MILLE | PREVIOUS | SLIDING | START | TRY | TUMBLING | TYPE | WHEN | WINDOW | ZERO_DIGIT
        // XQuery Update 1.0 keywords
        | AFTER | BEFORE | COPY | DELETE | FIRST | INSERT | INTO | LAST | MODIFY | NODES | RENAME | REPLACE | REVALIDATION | SKIP | VALUE | WITH
        // XQuery Full Text 1.0 keywords
        | ALL | ANY | CONTAINS | CONTENT | DIACRITICS | DIFFERENT | DISTANCE | ENTIRE | EXACTLY | FROM | FT_OPTION | FTAND | FTNOT | FTOR | INSENSITIVE | LANGUAGE | LEVELS | LOWERCASE | MOST | NO | NOT | OCCURS | PARAGRAPH | PARAGRAPHS | PHRASE | RELATIONSHIP | SAME | SCORE | SENSITIVE | SENTENCE | SENTENCES | STEMMING | STOP | THESAURUS | TIMES | UPPERCASE | USING | WEIGHT | WILDCARDS | WITHOUT | WORD | WORDS
        // new XQuery Scripting proposal keywords
        | BREAK | CONTINUE | EXIT | LOOP | RETURNING | WHILE
        // Zorba keywords
        | EVAL
        // Zorba DDL keywords
        | CHECK | COLLECTION | CONSTRAINT | EXPLICITLY | FOREACH | FOREIGN | INDEX | INTEGRITY | KEY | ON
        // Mark Logic keywords
        | BINARY
        // entity references
        | AMP_ER | APOS_ER | QUOT_ER
        ;
p_FNCName
        : L_NCName
        // XQuery 1.0 keywords
        | ANCESTOR | ANCESTOR_OR_SELF | AND | AS | ASCENDING | AT | BASE_URI | BOUNDARY_SPACE | BY | CASE | CAST | CASTABLE | CHILD | COLLATION | CONSTRUCTION | COPY_NAMESPACES | DECLARE | DEFAULT | DESCENDANT | DESCENDANT_OR_SELF | DESCENDING | DIV | DOCUMENT | ELSE | EMPTY | ENCODING | EQ | EVERY | EXCEPT | EXTERNAL | FOLLOWING | FOLLOWING_SIBLING | FOR | FUNCTION | GE | GREATEST | GT | IDIV | IMPORT | IN | INHERIT | INSTANCE | INTERSECT | IS | LAX | LE | LEAST | LET | LT | MOD | MODULE | NAMESPACE | NE | NO_INHERIT | NO_PRESERVE | OF | OPTION | OR | ORDER | ORDERED | ORDERING | PARENT | PRECEDING | PRECEDING_SIBLING | PRESERVE | RETURN | SATISFIES | SCHEMA | SELF | SOME | STABLE | STRICT | STRIP | THEN | TO | TREAT | UNION | UNORDERED | VALIDATE | VARIABLE | VERSION | WHERE | XQUERY
        // XQuery 3.0 keywords
        | ALLOWING | CATCH | CONTEXT | COUNT | DECIMAL_FORMAT | DECIMAL_SEPARATOR | DIGIT | END | GROUP | GROUPING_SEPARATOR | INFINITY | MINUS_SIGN | NAN | NEXT | ONLY | PATTERN_SEPARATOR | PERCENT | PER_MILLE | PREVIOUS | SLIDING | START | TRY | TUMBLING | TYPE | WHEN | WINDOW | ZERO_DIGIT
        // XQuery Update 1.0 keywords
        | AFTER | BEFORE | COPY | DELETE | FIRST | INSERT | INTO | LAST | MODIFY | NODES | RENAME | REPLACE | REVALIDATION | SKIP | UPDATING | VALUE | WITH
        // XQuery Full Text 1.0 keywords
        | ALL | ANY | CONTAINS | CONTENT | DIACRITICS | DIFFERENT | DISTANCE | ENTIRE | EXACTLY | FROM | FT_OPTION | FTAND | FTNOT | FTOR | INSENSITIVE | LANGUAGE | LEVELS | LOWERCASE | MOST | NO | NOT | OCCURS | PARAGRAPH | PARAGRAPHS | PHRASE | RELATIONSHIP | SAME | SCORE | SENSITIVE | SENTENCE | SENTENCES | STEMMING | STOP | THESAURUS | TIMES | UPPERCASE | USING | WEIGHT | WILDCARDS | WITHOUT | WORD | WORDS
        // new XQuery Scripting proposal keywords
        | BREAK | CONTINUE | EXIT | LOOP | RETURNING
        // Zorba keywords
        | EVAL
        // Zorba DDL keywords
        | CHECK | COLLECTION | CONSTRAINT | EXPLICITLY | FOREACH | FOREIGN | INDEX | INTEGRITY | KEY | ON
        // Mark Logic keywords
        | BINARY
        // entity references
        | AMP_ER | APOS_ER | QUOT_ER
        ;

//[209] /* xgc: xml-version */
//S

//[210] /* xgc: xml-version */
//Char

//[211]
//Digits ::= [0-9]+

//[212]
//CommentContents ::= (Char+ - (Char* ('(:' | ':)') Char*))


// **************************************
// XQuery Update 1.0 Productions
// http://www.w3.org/TR/xquery-update-10/
// **************************************

pg_UpdateExpr
        : p_InsertExpr
        | p_DeleteExpr
        | p_RenameExpr
        | p_ReplaceExpr
        | p_TransformExpr
        ;

//[141]
pm_RevalidationDecl
        : k+=DECLARE k+=REVALIDATION (k+=STRICT | k+=LAX | k+=SKIP) {ak($k);}
        ;

//[142]
p_InsertExprTargetChoice
        : ((k+=AS (k+=FIRST | k+=LAST))? k+=INTO) {ak($k);}
        | ka=AFTER {ak($ka);}
        | kb=BEFORE {ak($kb);}
        ;

//[143]
p_InsertExpr
        : k+=INSERT (k+=NODE | k+=NODES) p_SourceExpr p_InsertExprTargetChoice p_TargetExpr {ak($k);}
        ;

//[144]
p_DeleteExpr
        : k+=DELETE (k+=NODE | k+=NODES) p_TargetExpr {ak($k);}
        ;

//[145]
p_ReplaceExpr
        : k+=REPLACE (k+=VALUE k+=OF)? k+=NODE p_ExprSingle k+=WITH p_ExprSingle {ak($k);}
        ;

//[146]
p_RenameExpr
        : k+=RENAME k+=NODE p_TargetExpr AS p_NewNameExpr {ak($k);}
        ;

//[147]
p_SourceExpr
        : p_ExprSingle
        ;

//[148]
p_TargetExpr
        : p_ExprSingle
        ;

//[149]
p_NewNameExpr
        : p_ExprSingle
        ;

//[150]
p_TransformExpr
        : k+=COPY DOLLAR p_VarName BIND p_ExprSingle (COMMA DOLLAR p_VarName BIND p_ExprSingle)* k+=MODIFY p_ExprSingle k+=RETURN p_ExprSingle {ak($k);} 
        ;


// **************************************
// XQuery Full Text 1.0 Productions
// http://www.w3.org/TR/xpath-full-text-10/
// **************************************

//[24] Full Text 1.0
pm_FTOptionDecl
        : k+=DECLARE k+=FT_OPTION p_FTMatchOptions SEMICOLON {ak($k);}
        ;

//[37] Full Text 1.0
p_FTScoreVar
        : ks=SCORE {ak($ks);} DOLLAR p_VarName
        ;

//[51] Full Text 1.0
p_FTContainsExpr
        : p_RangeExpr ( k+=CONTAINS k+=TEXT {ak($k);} p_FTSelection p_FTIgnoreOption? )?
        ;

//[144] Full Text 1.0
p_FTSelection
        : p_FTOr p_FTPosFilter*
        ;

//[145] Full Text 1.0
p_FTWeight
        : kw=WEIGHT {ak($kw);} LBRACKET p_Expr RBRACKET
        ;

//[146] Full Text 1.0
p_FTOr
        : p_FTAnd ( ko=FTOR {ak($ko);} p_FTAnd )*
        ;

//[147] Full Text 1.0
p_FTAnd
        : p_FTMildNot ( ka=FTAND {ak($ka);} p_FTMildNot )*
        ;

//[148] Full Text 1.0
p_FTMildNot
        : p_FTUnaryNot ( k+=NOT k+=IN {ak($k);} p_FTUnaryNot )*
        ;

//[149] Full Text 1.0
p_FTUnaryNot
        : ( kn=FTNOT {ak($kn);} )? p_FTPrimaryWithOptions
        ;

//[150] Full Text 1.0
p_FTPrimaryWithOptions
        : p_FTPrimary p_FTMatchOptions? p_FTWeight?
        ;

//[168] Full Text 1.0
//Prefix       ::=      NCName

//[151] Full Text 1.0
p_FTPrimary
        : (p_FTWords p_FTTimes?)
        | (LPAREN p_FTSelection RPAREN)
// disabled: see below 
//        | p_FTExtensionSelection
        ;

//[152] Full Text 1.0
p_FTWords
        : p_FTWordsValue p_FTAnyallOption?
        ;

//[153] Full Text 1.0
p_FTWordsValue
        : p_StringLiteral
        | (LBRACKET p_Expr RBRACKET)
        ;

// disabled because of an error:
//   [java] error(211): XQueryParser.g:1248:30: [fatal] rule p_FTExtensionSelection has non-LL(*) decision due to recursive rule invocations reachable from alts 1,2.  Resolve by left-factoring or using syntactic predicates or using backtrack=true option.
//   [java] warning(200): XQueryParser.g:1248:30: Decision can match input such as "LBRACKET FOR {AND, CAST..CASTABLE, DIV, EQ, EXCEPT, GE, GT..IDIV, INSTANCE..IS, LE, LT..MOD, NE, OR, TO..TREAT, UNION, CONTAINS, LPAREN, RBRACKET..LSQUARE, EQUAL, NOTEQUAL, COMMA, STAR..SLASH_SLASH, COLON, SEMICOLON..VBAR}" using multiple alternatives: 1, 2
//   [java] As a result, alternative(s) 2 were disabled for that input
// line 1248 is: L_Pragma+ LBRACKET p_FTSelection? LBRACKET
//[154] Full Text 1.0
//p_FTExtensionSelection
//        : L_Pragma+ LBRACKET p_FTSelection? LBRACKET
//        ;

//[155] Full Text 1.0
p_FTAnyallOption
        : ( (k+=ANY k+=WORD?) | (k+=ALL WORDS?) | k+=PHRASE ) {ak($k);}
        ;

//[156] Full Text 1.0
p_FTTimes
        : k+=OCCURS p_FTRange k+=TIMES {ak($k);}
        ;

//[157] Full Text 1.0
p_FTRange
        : ( (k+=EXACTLY p_AdditiveExpr)
        |   (k+=AT k+=LEAST p_AdditiveExpr)
        |   (k+=AT k+=MOST p_AdditiveExpr)
        |   (k+=FROM p_AdditiveExpr k+=TO p_AdditiveExpr) ) {ak($k);}
        ;

//[158] Full Text 1.0
p_FTPosFilter
        : p_FTOrder 
        | p_FTWindow 
        | p_FTDistance 
        | p_FTScope 
        | p_FTContent
        ;

//[159] Full Text 1.0
p_FTOrder
        : ko=ORDERED {ak($ko);}
        ;

//[160] Full Text 1.0
p_FTWindow
        : kw=WINDOW {ak($kw);} p_AdditiveExpr p_FTUnit
        ;

//[161] Full Text 1.0
p_FTDistance
        : kd=DISTANCE {ak($kd);} p_FTRange p_FTUnit
        ;

//[162] Full Text 1.0
p_FTUnit
        : ( k+=WORDS | k+=SENTENCES | k+=PARAGRAPHS ) {ak($k);}
        ;

//[163] Full Text 1.0
p_FTScope
        : (k+=SAME | k+=DIFFERENT) {ak($k);} p_FTBigUnit
        ;

//[164] Full Text 1.0
p_FTBigUnit
        : ( k+=SENTENCE | k+=PARAGRAPH ) {ak($k);}
        ;

//[165] Full Text 1.0
p_FTContent
        : ( (k+=AT k+=START) | (k+=AT k+=END) | (k+=ENTIRE k+=CONTENT) ) {ak($k);}
        ;

//[166] Full Text 1.0
p_FTMatchOptions
        : (ku=USING {ak($ku);} p_FTMatchOption)+
        ;

//[167] Full Text 1.0
p_FTMatchOption
        : p_FTLanguageOption
        | p_FTWildCardOption
        | p_FTThesaurusOption
        | p_FTStemOption
        | p_FTCaseOption
        | p_FTDiacriticsOption
        | p_FTStopWordOption
        | p_FTExtensionOption
        ;

//[168] Full Text 1.0
p_FTCaseOption
        : ( (k+=CASE k+=INSENSITIVE)
        |   (k+=CASE k+=SENSITIVE)
        |   k+=LOWERCASE
        |   k+=UPPERCASE ) {ak($k);}
        ;

//[169] Full Text 1.0
p_FTDiacriticsOption
        : ( (k+=DIACRITICS k+=INSENSITIVE)
        |   (k+=DIACRITICS k+=SENSITIVE) ) {ak($k);}
        ;

//[170] Full Text 1.0
p_FTStemOption
        : ( k+=STEMMING | (k+=NO k+=STEMMING) ) {ak($k);}
        ;

//[171] Full Text 1.0
p_FTThesaurusOption
        : ( (k+=THESAURUS (p_FTThesaurusID | k+=DEFAULT))
        |   (k+=THESAURUS LPAREN (p_FTThesaurusID | k+=DEFAULT) (COMMA p_FTThesaurusID)* RPAREN)
        |   (k+=NO k+=THESAURUS) ) {ak($k);}
        ;

//[172] Full Text 1.0
p_FTThesaurusID
        : k+=AT p_StringLiteral (k+=RELATIONSHIP p_StringLiteral)? (p_FTLiteralRange k+=LEVELS)? {ak($k);}
        ;

//[173] Full Text 1.0
p_FTLiteralRange
        : ( (k+=EXACTLY L_IntegerLiteral)
        |   (k+=AT k+=LEAST L_IntegerLiteral)
        |   (k+=AT k+=MOST L_IntegerLiteral)
        |   (k+=FROM L_IntegerLiteral TO L_IntegerLiteral) ) {ak($k);}
        ;

//[174] Full Text 1.0
p_FTStopWordOption
        : ( (k+=STOP k+=WORDS p_FTStopWords p_FTStopWordsInclExcl*)
        |   (k+=STOP k+=WORDS k+=DEFAULT p_FTStopWordsInclExcl*)
        |   (k+=NO k+=STOP k+=WORDS) ) {ak($k);}
        ;

//[175] Full Text 1.0
p_FTStopWords
        : (ka=AT {ak(ka);} p_StringLiteral)
        | (LPAREN p_StringLiteral (COMMA p_StringLiteral)* RPAREN)
        ;

//[176] Full Text 1.0
p_FTStopWordsInclExcl
        : ( (k+=UNION | k+=EXCEPT) p_FTStopWords ) {ak($k);}
        ;

//[177] Full Text 1.0
p_FTLanguageOption
        : kl=LANGUAGE {ak(kl);} p_StringLiteral
        ;

//[178] Full Text 1.0
p_FTWildCardOption
        : ( k+=WILDCARDS | (k+=NO k+=WILDCARDS) ) {ak($k);}
        ;

//[179] Full Text 1.0
p_FTExtensionOption
        : ko=OPTION {ak(ko);} p_QName p_StringLiteral
        ;

//[180] Full Text 1.0
p_FTIgnoreOption
        : k+=WITHOUT k+=CONTENT {ak($k);} p_UnionExpr
        ;


// **************************************
// XQuery Scripting proposal Productions
// http://xquery-scripting.ethz.ch/spec.html
// **************************************

//[1]
p_Program
        : p_StatementsAndOptionalExpr
        ;

//[2]
p_Statements
        : p_Statement*
        ;

//LL grammar
//[3]
//p_StatementsAndExpr
//        : p_Statements p_Expr
//        ;

//[4]
//LL grammar
p_StatementsAndOptionalExpr
        : p_Statement1+ p_Expr?
        | p_Expr?
        ;

//[5]
//LL grammar
p_Statement
        : p_Statement1 
        | p_Statement2
        ;
p_Statement1
        : p_AssignStatement
        | p_BlockStatement
        | p_BreakStatement
        | p_ContinueStatement
        | p_ExitStatement
        | p_VarDeclStatement
        | p_WhileStatement
        ;
p_Statement2
        : p_ApplyStatement
        | p_FLWORStatement
        | p_IfStatement
        | p_SwitchStatement
        | p_TryCatchStatement
        | p_TypeswitchStatement
        ;

//[6]
p_ApplyStatement
        : p_ExprSimple SEMICOLON
        ;

//[7]
p_AssignStatement
        : DOLLAR p_VarName BIND p_ExprSingle SEMICOLON
        ;

//[8]
p_BlockStatement
        : LBRACKET p_Statements RBRACKET
        ;

//[9]
p_BreakStatement
        : k=BREAK {ak($k);} k=LOOP {ak($k);} SEMICOLON
        ;

//[10]
p_ContinueStatement
        : k=CONTINUE {ak($k);} k=LOOP {ak($k);} SEMICOLON
        ;

//[11]
p_ExitStatement
        : k=EXIT {ak($k);} k=RETURNING {ak($k);} p_ExprSingle SEMICOLON
        ;

//[12]
p_FLWORStatement
        : p_InitialClause p_IntermediateClause* p_ReturnStatement
        ;    

//[13]
p_ReturnStatement
        : k=RETURN {ak($k);} p_Statement
        ;

//[14]
p_IfStatement
        : k=IF {ak($k);} LPAREN p_Expr RPAREN k=THEN {ak($k);} p_Statement k=ELSE {ak($k);} p_Statement
        ;

//[15]
p_SwitchStatement
        : k=SWITCH {ak($k);} LPAREN p_Expr RPAREN p_SwitchCaseStatement+ k=DEFAULT {ak($k);} k=RETURN {ak($k);} p_Statement
        ;

//[16]
p_SwitchCaseStatement
        : (k=CASE {ak($k);} p_SwitchCaseOperand)+ k=RETURN {ak($k);} p_Statement
        ;

//[17]
p_TryCatchStatement
        : k=TRY {ak($k);} p_BlockStatement (k=CATCH {ak($k);} p_CatchErrorList p_BlockStatement)+ {ak($k);}
        ;

//[18]
p_TypeswitchStatement
        : k=TYPESWITCH {ak($k);} LPAREN p_Expr RPAREN p_CaseStatement+ k=DEFAULT {ak($k);} (DOLLAR p_VarName)? k=RETURN {ak($k);} p_Statement
        ;

//[19]
p_CaseStatement
        : k=CASE {ak($k);} (DOLLAR p_VarName AS)? p_SequenceType k=RETURN {ak($k);} p_Statement
        ;

//[20]
p_VarDeclStatement
        : (k=LOCAL {ak($k);} p_Annotation*)? k=VARIABLE {ak($k);} DOLLAR p_VarName p_TypeDeclaration? (BIND p_ExprSingle)?
          (COMMA DOLLAR p_VarName p_TypeDeclaration? (BIND p_ExprSingle)?)*
          SEMICOLON
        ;

//[21]
p_WhileStatement
        : k=WHILE {ak($k);} LPAREN p_Expr RPAREN p_Statement
        ;

//[23]
p_ExprSimple
        : p_QuantifiedExpr
        | p_OrExpr
        | {lc(XQU)}?=> pg_UpdateExpr
        ;

//[31]
//p_BlockExpr
//        : LBRACKET p_StatementsAndExpr LBRACKET
//        ;

// **************************************
// Zorba XQuery Extensions
// http://www.zorba-xquery.com/doc/zorba-latest/zorba/html/eval.html
// **************************************
p_EvalExpr
        : p_UsingClause? k=EVAL {ak($k);} LBRACKET p_ExprSingle RBRACKET
        ;

p_UsingClause
        : k=USING {ak($k);} DOLLAR p_VarName (COMMA DOLLAR p_VarName)*
        ;
// *************************************************

// *************************************************
// XQDDL
// http://www.zorba-xquery.com/site2/doc/latest/zorba/html/xqddf.html
// *************************************************
p_CollectionDecl
        : k=COLLECTION {ak($k);} p_QName p_CollectionTypeDecl?
        ;

p_CollectionTypeDecl
        : (k=AS {ak($k);} p_KindTest ((p_OccurrenceIndicator) => p_OccurrenceIndicator)?)
        ;

p_IndexDecl
        : k=INDEX {ak($k);} p_IndexName k=ON {ak($k);} k=NODES {ak($k);} p_IndexDomainExpr k=BY {ak($k);} p_IndexKeySpec (COMMA p_IndexKeySpec)*
        ;

p_IndexName
        : p_QName
        ;

p_IndexDomainExpr
        : p_PathExpr
        ;

p_IndexKeySpec
        : p_IndexKeyExpr p_TypeDeclaration p_OrderModifier
        ;

p_IndexKeyExpr
        : p_PathExpr
        ;

p_ICDecl
        : k=INTEGRITY {ak($k);} k=CONSTRAINT {ak($k);} p_QName (p_ICCollection | p_ICForeignKey)
        ;

p_ICCollection
        : k=ON {ak($k);} k=COLLECTION {ak($k);} p_QName (p_ICCollSequence | p_ICCollSequenceUnique | p_ICCollNode)
        ;

p_ICCollSequence
        : DOLLAR p_QName k=CHECK {ak($k);} p_ExprSingle
        ;

p_ICCollSequenceUnique
        : k=NODE {ak($k);} DOLLAR p_QName k=CHECK {ak($k);} k=UNIQUE {ak($k);} k=KEY {ak($k);} p_PathExpr
        ;

p_ICCollNode
        : k=FOREACH {ak($k);} k=NODE {ak($k);} DOLLAR p_QName k=CHECK {ak($k);} p_ExprSingle
        ;

p_ICForeignKey
        : k=FOREIGN {ak($k);} k=KEY {ak($k);} p_ICForeignKeySource p_ICForeignKeyTarget
        ;

p_ICForeignKeySource
        : k=FROM {ak($k);} p_ICForeignKeyValues
        ;

p_ICForeignKeyTarget
        : k=TO {ak($k);} p_ICForeignKeyValues
        ;

p_ICForeignKeyValues
        : k=COLLECTION {ak($k);} p_QName k=NODE {ak($k);} DOLLAR p_QName k=KEY {ak($k);} p_PathExpr
        ;
// *************************************************

//********************************* BUGS ***************************************

// ******************************** FROM "LDS.org"

//== admin-ldsorg/feedback/resources/ajax/tags-save.xqy ========================
//This is on the first line of the file, is that the problem?
//import module namespace feedback = "http://lds.org/code/ldsorg/feedBackFuntions" at "/admin-ldsorg/feedback/modules/feedbackFunctions.xqy";
//------------------------------------------------------------------------------
// - line 1:7 missing IMPORT at 'module'

//== admin-ldsorg/content/ldsWebMLTransforms/addMediaCollection.xqy ============
// It is thinking it is a comment ending? 
//fn:concat(fn:base-uri($ldsWebML), " -- ", "Transformed")
//------------------------------------------------------------------------------
// - line 26:61 mismatched character ' ' expecting '>'

//== resources/video/xml.xqy ===================================================
//<![CDATA[ rtmpt://fls1g1.services.att-idns.net/av1]]>
//------------------------------------------------------------------------------
// - line 131:140 no viable alternative at character '['

//== media-library/images/category.xqy =========================================
// Keywords as namespaces?
//if:orderImages($imagesFromItems, $order);
//------------------------------------------------------------------------------
// - line 53:47 missing SEMICOLON at '('
// - line 53:73 missing EOF at ';'

// ******************************** FROM "NRML-Multisite"

//== modules/widgets/recent-news-widget.xqy ====================================
// This is the first line of the file
//declare namespace html = "http://www.w3.org/1999/xhtml";
//------------------------------------------------------------------------------
// - line 1:8 missing DECLARE at 'namespace'

// ******************************** FROM "Moderation"
//== filter/AddToWhiteList.xqy =================================================
// Can't even paste in the code - probably "fun" stuff coming from Visual Studio

// ******************************** FROM "common"
//== test/unittest.xqy =========================================================
// Comments inside of string literals give problems
//------------------------------------------------------------------------------
// - line 257:0 mismatched character '<EOF>' expecting ':'

//******************************** END BUGS ************************************

// TODO 
// Add virtual tokens for computed elements/attributes/etc.
// Make virtual token existance consistent - either have placeholders or don't have virtual tokens if there are no values
// Module variable declarations - add "external" in AST?

//Original TODO
// VarDecl changes structure of the tree: less children: this will break something in the variable type reading in XQDT
// Enabling p_FunctionItemExpr in p_PrimaryExpr will break the grammar and generate an error stating the p_OrderedExpr, p_UnorderedExpr, and p_FunctionItemExpr can never be matched
// Also when p_PostfixExpr accepts p_ArgumentList a recursion appears that ANTLR does not like: rule p_PostfixExpr has non-LL(*) decision due to recursive rule invocations reachable from alts 1,2.  Resolve by left-factoring or using syntactic predicates or using backtrack=true option.
