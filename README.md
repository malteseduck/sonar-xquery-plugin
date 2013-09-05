Sonar XQuery Plugin
===================

Language plugin for the Sonar code analysis tool
(http://www.sonarqube.org/).  This has been tested on Sonar 3.5.

Currently the grammar is generated using ANTLR v3 and supports XQuery following
the 1.0 specification.  There are some MarkLogic-specific additions to
support some of what the MarkLogic XQuery parser allows.  One of the TODO
items is to upgrade support for some of the XQuery 3.0 syntax introduced
in MarkLogic 6.0 as well as simplify the grammar (maybe use the IntelliJ
GrammarKit plugin).  Code parsing checks are marked as "INFO" so parser failures
on 3.0 syntax should not be to disruptive.

Usage
=====

http://docs.codehaus.org/display/SONAR/Analyzing+Source+Code (more details to follow)

Language Checks
===============

"Rule" checks are grouped by one of the 5 severity levels: INFO, MINOR, MAJOR,
CRITICAL, and BLOCKING.  Each check gives a basic description and links to a
full page that has more detailed descriptions and examples of how to "properly
follow" the convention.

BLOCKING Checks
===============

These are things that should prevent release of an application into production
if any violations of these conventions exist.

Currently no checks of this severity have been created

CRITICAL Checks
===============

These are rules that are important to address and should be looked into before
releasing an application.  Following these can help prevent major problems in an
application or significantly increase readability and/or maintainability.

DynamicFunctionUserScripts - Dynamic Function Usage with User-supplied Script
Avoid passing user-supplied text into xdmp:eval() and xdmp:value().  Instead use
xdmp:invoke() or xdmp:unpath() or make sure all inputs passed into this function
are properly validated to avoid input injection.

StrongTypingInFunctionDeclaration - Use Strong Typing in Function Declarations
Declare types for function parameters and return type to increase readability
and catch potential bugs.  Also try to scope the types as narrowly as possible
(i.e. use 'element()' instead of 'item()' when returning an element) and include
quantifiers on each type.

StrongTypingInModuleVariables - Use Strong Typing when Declaring Module
Variables Declare types for declared variables to increase readability and catch
potential bugs.  Also try to scope the types as narrowly as possible (i.e. use
'element()' instead of 'item()' when the value is an element) and include
quantifiers on each type.

MAJOR Checks
============

These are rules about things that could cause problems in an application - but
that may not - so it is not critical to address violations immedately.  It would
be a good idea to make plans to address them eventually, though, to avoid any
future problems.

DynamicFunction - Dynamic Function General Usage Avoid using xdmp:eval() and
xdmp:value() where possible.  Instead use xdmp:invoke() or xdmp:unpath() or, if
possible, function values to dynamically evaluate code logic.

OperationsInPredicate - Avoid Operations in Predicates Instead of calling
functions or performing operations in predicates try to assign the results in a
variable before the predicate.

MINOR Checks
============

These are rules about things that should be done but generally won't cause too
many problems with an application.  Optimizing to follow these may help prevent
problems, they may not, but in many cases they can increase readability and/or
maintainability.

EffectiveBoolean - Effective Boolean in Conditional Predicate Unless the value
in the conditional is of type xs:boolean it is recommended you use fn:exists(),
fn:empty(), or other boolean functions inside of conditional predicates to check
values.

StrongTypingInFLWOR - Use Strong Typing in FLWOR Expressions Declare types for
FLWOR 'let' and 'for' clauses to increase readability and catch potential bugs.
Also try to scope the types as narrowly as possible (i.e. use 'element()'
instead of 'item()' when the value is an element) and include quantifiers on
each type.

FunctionMapping - Function Mapping Usage Make sure you are intentionally using
and/or understand function mapping - otherwise disable it with 'declare option
xdmp:mapping "false";'  If you wish to use it you should explicitly declare
'declare option xdmp:mapping "true";' for readability/maintainability.

XPathDecendantSteps - Avoid Using '//' in XPath Favor fully-qualified paths in
XPath for readability and to avoid potential performance problems.

XPathTextSteps - Avoid Using text() in XPath Generally avoid using text() in
your XPath in favor of using fn:string() or allowing atomization (through strong
typing or default atomization).

XQueryVersion - MarkLogic XQuery Version Ensure that you declare the latest
XQuery version (1.0-ml) at the top of each of your scripts (as opposed to
declaring an older version - 0.9-ml - or declaring no version at all).  This
ensures better compatability of code after server upgrades and helps maintain
consistent behavior in XQuery processing.

INFO Checks
===========

These are purely informational either because they require manual checking, they
are just "good to know," or their full validity is in question.

OrderByRange - Range Evaulation in Order By Clause Order bys or gt/lt checks on
large numbers of documents might get better performance with a range index.

ParseError - Code Parsing Error This is to catch parsing errors on projects.
There may be a potential syntax error, or the parser just may not be able to
process certain syntax.  Technically this rule does not take part in the AST
parsing.

XPathSubExpresssionsInPredicate - Avoid XPath Sub-expressions in XPath
Predicates Watch expressions like '[foo/bar]' or '[foo[bar]]' because they can
sometimes be bad for performance.  If the result is static it can be bound to a
variable.