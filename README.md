Sonar XQuery Plugin
===================

Language plugin for the Sonar code analysis tool (http://www.sonarqube.org/).  This has been 
tested on Sonar 4.3.  If you want a version that works with Sonar 3.5 then use version 1.4 
of this plugin.

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

###Sonar runner
To build the plugin and run the unit tests just run the following:
``` sh
mvn install
```

In order to run analysis on a project you will need to setup SonarQube 4.3 locally and install the built plugin
into SONARQUBE_HOME/extensions/plugins.  Follow the setup instructions here (step 1.5 is install the plugin):

``` http://docs.codehaus.org/display/SONAR/Setup+and+Upgrade ```

The simplest approach to run the analysis on a project is to use the runner described in the setup guide.  Here is an 
example sonar-project.properties file I used for analyzing the xray library (86.2% compliance - good
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

With the runner installed just run the sonar runner from your project directory.

To see more details of how to run analysis on projects using maven, gradle, etc. see the documentation on the SonarQube
site:

http://docs.codehaus.org/display/SONAR/Analyzing+Source+Code

###Maven
To prepare an example maven project that hosts both java and source code
and is able to run both java and xquery code sonar analysis do as below:
* Run below command to generate project from archetype:
``` sh
mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=xquery-java-example-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

* Modify generated pom.xml file so that it looks like the one below:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.mycompany.app</groupId>
  <artifactId>xquery-java-example-app</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>xquery-java-example-app</name>
  <url>http://maven.apache.org</url>
  <properties>
    <overridenSourceDirectory>src/main/java</overridenSourceDirectory>
  </properties>
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <sourceDirectory>${overridenSourceDirectory}</sourceDirectory>
  </build>
  <profiles>
    <profile>
      <id>sonar-xquery</id>
      <properties>
        <overridenSourceDirectory>src/main/xquery</overridenSourceDirectory>
        <sonar.branch>XQuery</sonar.branch>
        <sonar.language>xquery</sonar.language>
      </properties>
    </profile>
  </profiles>
</project>
```

* Add your java code under src/main/java
* Add your xquery code under src/main/xquery
* Run below command to run check for java:
``` sh
mvn sonar:sonar
```

* Run below command to run check for xquery:
``` sh
    mvn sonar:sonar -Psonar-xquery
```


Additional information about multi-language setup for sonar can be found in
[stackoverflow answer](http://stackoverflow.com/questions/13625022/does-sonar-support-multiple-language-in-same-project)

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
    <dt>StrongTypingInFunctionDeclaration</dt>
    <dd>
        (Use Strong Typing in Function Declarations)
        Declare types for function parameters and return types
        to increase readability and catch potential bugs.
        Also try to scope the types as narrowly as possible
        (i.e. use 'element()' instead of 'item()' when returning an element)
        and include quantifiers on each type.
    </dd>
    <dt>StrongTypingInModuleVariables</dt>
    <dd>
        (Use Strong Typing when Declaring Module Variables)
        Declare types for declared variables to increase readability and catch potential bugs.
        Also try to scope the types as narrowly as possible
        (i.e. use 'element()' instead of 'item()' when the value is an element)
        and include quantifiers on each type.
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
        (Dynamic Function Usage (Marklogic))
        Avoid using xdmp:eval() and xdmp:value() where possible.
        Instead use either xdmp:invoke(), xdmp:unpath()
        or if possible assign functions to variables to dynamically evaluate code logic.
        Please note that this check is Marklogic specific.
    </dd>
    <dt>OperationsInPredicate</dt>
    <dd>
        (Avoid Operations in Predicates)
        Instead of calling functions or performing operations in predicates
        try assigning the results to a variable before the predicate.
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
        (Effective Boolean in Conditional Predicate)
        Unless the value in the conditional is of type xs:boolean it is recommended you use
        fn:exists(), fn:empty(), or other boolean functions inside of conditional predicates to check values.
    </dd>
    <dt>StrongTypingInFLWOR</dt>
    <dd>
        (Use Strong Typing in FLWOR Expressions)
        Declare types for FLWOR 'let' and 'for' clauses to increase readability and catch potential bugs.
        Also try to scope the types as narrowly as possible
        (i.e. use 'element()' instead of 'item()' when the value is an element)
        and include quantifiers on each type.
    </dd>
    <dt>FunctionMapping</dt>
    <dd>
        (Function Mapping Usage (Marklogic))
        Make sure you are intentionally using and/or understand function mapping
        - otherwise disable it with 'declare option xdmp:mapping "false";'.
        If you wish to use it you should explicitly declare 'declare option xdmp:mapping "true";'
        for readability/maintainability.
        Please note that this check is Marklogic specific.
    </dd>
    <dt>XPathDescendantSteps</dt>
    <dd>
        (Avoid Using '//' in XPath)
        Favor fully-qualified paths in XPath
        for readability and to avoid potential performance problems.
    </dd>
    <dt>XPathTextSteps</dt>
    <dd>
        (Avoid Using text() in XPath)
        Generally avoid using /text() in your XPath in favor of using fn:string() or allowing atomization
        (through strong typing or default atomization).
    </dd>
    <dt>XQueryVersion</dt>
    <dd>
        (XQuery Version)
        Ensure that you declare the latest XQuery version (1.0-ml/3.0)
        at the top of each of your scripts
        (as opposed to declaring an older version - 0.9-ml - or not declaring a version at all).
        This ensures better compatibility of code after server upgrades
        and consistent behavior in XQuery processing.
    </dd>
</dl>

INFO Checks
-----------

These are purely informational either because they require manual checking, they
are just "good to know," or their full validity is in question.

<dl>
    <dt>OrderByRange</dt>
    <dd>
        (Range Evaulation in Order By Clause)
        Order bys or gt/lt checks on large numbers of documents
        might achieve better performance with a range index.
    </dd>
    <dt>ParseError</dt>
    <dd>
        (Code Parsing Error)
        This is to catch parsing errors on projects.
        There may be a potential syntax error, or the parser just may not be able to process certain syntax.
    </dd>
    <dt>XPathSubExpressionsInPredicate</dt>
    <dd>
        (Avoid XPath Sub-expressions in XPath Predicates)
        Watch out for expressions like '[foo/bar]' or '[foo[bar]]'
        because they can sometimes be bad for performance.
        If the result is static it can be bound to a variable.
    </dd>
</dl>