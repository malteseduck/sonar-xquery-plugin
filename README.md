Sonar XQuery Plugin
===================

Language plugin for the Sonar code analysis tool (http://www.sonarqube.org/).  This has been
tested on Sonar 3.5.

Currently the XQuery parser is generated using ANTLR v3 and supports XQuery following
the 1.0 specification.  There are some MarkLogic-specific additions to
support some of what the MarkLogic XQuery parser allows.

One of the "to do" items is to upgrade support for some of the XQuery 3.0 syntax
introduced in MarkLogic 6.0 as well as simplify the grammar management(maybe use
the IntelliJ GrammarKit plugin).

Code parsing checks are marked as "INFO" so parser failures
on 3.0 syntax should not be to disruptive.

Usage
-----

To build the plugin, run the unit tests, and start up a test instance of Sonar just run
the following:

    mvn install org.codehaus.sonar:sonar-dev-maven-plugin::start-war -Dsonar.runtimeVersion=3.5

The simplest approach to run the analysis on a project is to use the runner (see
http://docs.codehaus.org/display/SONAR/Analyzing+with+SonarQube+Runner).  Here is an example
sonar-project.properties file I used for analyzing the xray library (86.2% compliance - good
work Rob :)):

<pre>
# required metadata
sonar.projectKey=xray
sonar.projectName=XQuery Unit Testing
sonar.projectVersion=2.0

# optional description
sonar.projectDescription=xray is a framework for writing XQuery unit tests on MarkLogic Server

# path to source directories (required)
sonar.sources=src

# The value of the property must be the key of the language.
sonar.language=xquery

# Encoding of the source code
sonar.sourceEncoding=UTF-8
</pre>

With the runner installed just run "sonar-runner" from your project directory.

To run this in a "production" instance of Sonar just copy the built JAR file into
into the SONARQUBE_HOME/extensions/plugins directory.  To see more details of how to
run analysis on projects using maven, gradle, etc. see the documentation on the SonarQube
site:

http://docs.codehaus.org/display/SONAR/Analyzing+Source+Code

Language Checks
---------------

"Rule" checks are grouped by one of the 5 severity levels: INFO, MINOR, MAJOR,
CRITICAL, and BLOCKING.  Each check gives a basic description and links to a
full page that has more detailed descriptions and examples of how to "properly
follow" the convention.

BLOCKING Checks
---------------

These are things that should prevent release of an application into production
if any violations of these conventions exist.

Currently no checks of this severity have been created

CRITICAL Checks
---------------

These are rules that are important to address and should be looked into before
releasing an application.  Following these can help prevent major problems in an
application or significantly increase readability and/or maintainability.

<dl>
    <dt>DynamicFunctionUserScripts</dt>
    <dd>
        (Dynamic Function Usage with User-supplied Script)
        Avoid passing user-supplied text into xdmp:eval() and xdmp:value().  Instead use
        xdmp:invoke() or xdmp:unpath() or make sure all inputs passed into this function
        are properly validated to avoid input injection.
    </dd>
    <dt>StrongTypingInFunctionDeclaration</dt>
    <dd>
        (Use Strong Typing in Function Declarations)
        Declare types for function parameters and return type to increase readability
        and catch potential bugs.  Also try to scope the types as narrowly as possible
        (i.e. use 'element()' instead of 'item()' when returning an element) and include
        quantifiers on each type.
    </dd>
    <dt>StrongTypingInModuleVariables</dt>
    <dd>
        (Use Strong Typing when Declaring Module Variables)
        Declare types for declared variables to increase readability and catch
        potential bugs.  Also try to scope the types as narrowly as possible (i.e. use
        'element()' instead of 'item()' when the value is an element) and include
        quantifiers on each type.
    </dd>
</dl>

MAJOR Checks
-----------

These are rules about things that could cause problems in an application - but
that may not - so it is not critical to address violations immedately.  It would
be a good idea to make plans to address them eventually, though, to avoid any
future problems.

<dl>
    <dt>DynamicFunction</dt>
    <dd>
        (Dynamic Function General Usage) Avoid using xdmp:eval() and
        xdmp:value() where possible.  Instead use xdmp:invoke() or xdmp:unpath() or, if
        possible, function values to dynamically evaluate code logic.
    </dd>
    <dt>OperationsInPredicate</dt>
    <dd>
        (Avoid Operations in Predicates) Instead of calling
        functions or performing operations in predicates try to assign the results in a
        variable before the predicate.
    </dd>
</dl>

MINOR Checks
------------

These are rules about things that should be done but generally won't cause too
many problems with an application.  Optimizing to follow these may help prevent
problems, they may not, but in many cases they can increase readability and/or
maintainability.

<dl>
    <dt>EffectiveBoolean</dt>
    <dd>
        (Effective Boolean in Conditional Predicate) Unless the value
        in the conditional is of type xs:boolean it is recommended you use fn:exists(),
        fn:empty(), or other boolean functions inside of conditional predicates to check
        values.
    </dd>
    <dt>StrongTypingInFLWOR</dt>
    <dd>
        (Use Strong Typing in FLWOR Expressions) Declare types for
        FLWOR 'let' and 'for' clauses to increase readability and catch potential bugs.
        Also try to scope the types as narrowly as possible (i.e. use 'element()'
        instead of 'item()' when the value is an element) and include quantifiers on
        each type.
    </dd>
    <dt>FunctionMapping</dt>
    <dd>
        (Function Mapping Usage) Make sure you are intentionally using
        and/or understand function mapping - otherwise disable it with 'declare option
        xdmp:mapping "false";'  If you wish to use it you should explicitly declare
        'declare option xdmp:mapping "true";' for readability/maintainability.
    </dd>
    <dt>XPathDecendantSteps</dt>
    <dd>
        (Avoid Using '//' in XPath) Favor fully-qualified paths in
        XPath for readability and to avoid potential performance problems.
    </dd>
    <dt>XPathTextSteps</dt>
    <dd>
        (Avoid Using text() in XPath) Generally avoid using text() in
        your XPath in favor of using fn:string() or allowing atomization (through strong
        typing or default atomization).
    </dd>
    <dt>XQueryVersion</dt>
    <dd>
        (MarkLogic XQuery Version) Ensure that you declare the latest
        XQuery version (1.0-ml) at the top of each of your scripts (as opposed to
        declaring an older version - 0.9-ml - or declaring no version at all).  This
        ensures better compatibility of code after server upgrades and helps maintain
        consistent behavior in XQuery processing.
    </dd>
</dl>

INFO Checks
-----------

These are purely informational either because they require manual checking, they
are just "good to know," or their full validity is in question.

<dl>
    <dt>OrderByRange</dt>
    <dd>
        (Range Evaulation in Order By Clause) Order bys or gt/lt checks on
        large numbers of documents might get better performance with a range index.
    </dd>
    <dt>ParseError</dt>
    <dd>
        (Code Parsing Error) This is to catch parsing errors on projects.
        There may be a potential syntax error, or the parser just may not be able to
        process certain syntax.  Technically this rule does not take part in the AST
        parsing.
    </dd>
    <dt>XPathSubExpresssionsInPredicate</dt>
    <dd>
        (Avoid XPath Sub-expressions in XPath)
        Predicates Watch expressions like '[foo/bar]' or '[foo[bar]]' because they can
        sometimes be bad for performance.  If the result is static it can be bound to a
        variable.
    </dd>
</dl>