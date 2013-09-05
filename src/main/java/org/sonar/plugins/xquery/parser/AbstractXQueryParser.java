/*******************************************************************************
 * Copyright (c) 2008, 2009 28msec Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gabriel Petrovay (28msec) - initial API and implementation
 *
 * Modified
 *     Chris Cieslinski
 *******************************************************************************/

package org.sonar.plugins.xquery.parser;

import org.antlr.runtime.*;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractXQueryParser extends Parser implements XQueryLanguageConstants {

    private final LazyTokenStream stream;
    private ANTLRStringStream source;
    private ArrayList<AbstractXQueryLexer> lexerStack;
    private int language;
    private ProblemReporter reporter;

    public AbstractXQueryParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }

    public AbstractXQueryParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
        lexerStack = new ArrayList<AbstractXQueryLexer>();
        stream = (LazyTokenStream)input;
    }

    /**
     * Add a new keywords to the keyword list
     */
    protected void ak(CommonToken token) {
    }

    protected void ak(@SuppressWarnings("rawtypes") List list) {
//        if (list == null) {
//            return;
//        }
//        for (Object object : list) {
//            CommonToken token = (CommonToken)object;
//            fKeywords.add(new Position(token.getStartIndex(), token.getStopIndex() - token.getStartIndex() + 1));
//        }
    }

    public int getLanguage() {
        return language;
    }

    public ProblemReporter getReporter() {
        return reporter;
    }

    /**
     * Tests if the current language level is compliant with the language level specified in the
     * languageMask
     */
    protected boolean lc(int languageMask) {
        return (getLanguage() & languageMask) == languageMask;
    }

    public void popLexer() {
        if (lexerStack.size() == 0) {
            return;
        }

        AbstractXQueryLexer oldLexer = (AbstractXQueryLexer)stream.getTokenSource();
        AbstractXQueryLexer newLexer = lexerStack.remove(lexerStack.size() - 1);
        stream.setTokenSource(newLexer);
        oldLexer.postErrors();
    }

    public void postErrors() {
        AbstractXQueryLexer lexer = (AbstractXQueryLexer)stream.getTokenSource();
        lexer.postErrors();
    }

    public void pushLexer(AbstractXQueryLexer lexer) {
        lexer.setReporter(reporter);
        lexer.setLanguage(getLanguage());
        AbstractXQueryLexer oldLexer = (AbstractXQueryLexer)stream.getTokenSource();
        oldLexer.addToStack(lexerStack);
        stream.setTokenSource(lexer);
        oldLexer.postErrors();
    }

    public void pushStringLexer(boolean isAposStr) {
        StringLexer stringLexer = new StringLexer(source, isAposStr);        
        pushLexer(stringLexer);
    }

    public void pushXMLLexer() {
        XMLLexer xmlLexer = new XMLLexer(source);
        xmlLexer.setIsWsExplicit(true);
        pushLexer(xmlLexer);
    }

    public void pushXQueryLexer() {
        XQueryLexer xqueryLexer = new XQueryLexer(source);
        pushLexer(xqueryLexer);
    }
    
    // The following methods are used form the generated parser
    // The short names help keeping the grammar source file smaller and readable

    @Override
    public void reportError(RecognitionException e) {
        if (reporter != null) {
            reporter.reportError(getSourceName(), getErrorMessage(e, this.getTokenNames()), e.token);
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        if (lexerStack != null) {
            lexerStack.clear();
            stream.setWsExplicit(false);
        }
    }

    public void setCharSource(ANTLRStringStream source) {
        this.source = source;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    /**
     * Sets the current language level given an XQuery version (called from version prolog rule).
     */
    public void setLanguageVersion(String languageVersion) {
        if ("1.0-ml".equals(languageVersion) || "0.9-ml".equals(languageVersion)) {
            setLanguage(XQueryLanguageConstants.LANGUAGE_XQUERY_MARK_LOGIC);
        }
    }

    public void setReporter(ProblemReporter reporter) {
        this.reporter = reporter;
    }

    public void setWsExplicit(boolean isExplicit) {
        stream.setWsExplicit(isExplicit);
    }
}
