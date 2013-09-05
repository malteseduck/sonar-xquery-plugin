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

import java.util.List;

public abstract class AbstractXQueryLexer extends Lexer implements XQueryLanguageConstants {

    private boolean fIsWsExplicit = false;
    private int language;
    private ProblemReporter reporter;

    public AbstractXQueryLexer() {
    }

    public AbstractXQueryLexer(CharStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public void addToStack(List<AbstractXQueryLexer> stack) {
        stack.add(this);
    }

    public int getLanguage() {
        return language;
    }

    public ProblemReporter getReporter() {
        return reporter;
    }

    public boolean isWsExplicit() {
        return fIsWsExplicit;
    }
    
    /**
     * Tests if the current language level is compliant with the language level specified in the
     * languageMask
     */
    protected boolean lc(int languageMask) {
        return (getLanguage() & languageMask) == languageMask;
    }

    public void postErrors() {
    }

    @Override
    public void reportError(RecognitionException e) {
        if (reporter != null) {
            reporter.reportError(getSourceName(), getErrorMessage(e, this.getTokenNames()), e.token);
        }
    }

    public void rewindLine() {
        ANTLRStringStream stream = (ANTLRStringStream)input;
        int line = stream.getLine() - 1;
        if (line >= 1) {
            stream.setLine(stream.getLine() - 1);
        }
    }

    public void rewindToIndex(int index) {
        ANTLRStringStream stream = (ANTLRStringStream)input;
        stream.seek(index);
    }

    public void setIsWsExplicit(boolean wsExplicit) {
        fIsWsExplicit = wsExplicit;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public void setReporter(ProblemReporter reporter) {
        this.reporter = reporter;
    }   
}
