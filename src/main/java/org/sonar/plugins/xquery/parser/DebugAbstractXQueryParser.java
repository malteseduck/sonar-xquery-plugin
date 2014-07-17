/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser;

import org.antlr.runtime.*;
import org.antlr.runtime.debug.DebugEventListener;
import org.antlr.runtime.debug.DebugParser;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;

import java.util.ArrayList;
import java.util.List;

public abstract class DebugAbstractXQueryParser extends DebugParser implements XQueryLanguageConstants {

    private LazyTokenStream stream;
    private ANTLRStringStream source;
    private ArrayList<AbstractXQueryLexer> lexerStack;
    private int language;
    private ProblemReporter reporter;
    
    public DebugAbstractXQueryParser(TokenStream input, DebugEventListener dbg) {
        this(input, dbg, null);
        setDebugListener(dbg);
    }

    public DebugAbstractXQueryParser(TokenStream input, DebugEventListener dbg, RecognizerSharedState state) {
        this(input, state);
        setDebugListener(dbg);
    }

    public DebugAbstractXQueryParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
        stream = (LazyTokenStream)input;
        lexerStack = new ArrayList<AbstractXQueryLexer>();
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
    
    @Override
    public String getErrorMessage(RecognitionException e, String[] tokenNames) {
        String msg = e.getMessage();
        if ( e instanceof UnwantedTokenException ) {
            UnwantedTokenException ute = (UnwantedTokenException)e;
            String tokenName="<unknown>";
            if ( ute.expecting== Token.EOF ) {
                tokenName = "EOF";
            }
            else {
                tokenName = tokenNames[ute.expecting];
            }
            msg = "extraneous input "+getTokenErrorDisplay(ute.getUnexpectedToken())+
                " expecting "+tokenName;
        }
        else if ( e instanceof MissingTokenException ) {
            MissingTokenException mte = (MissingTokenException)e;
            String tokenName="<unknown>";
            if ( mte.expecting== Token.EOF ) {
                tokenName = "EOF";
            }
            else {
                tokenName = tokenNames[mte.expecting];
            }
            msg = "missing "+tokenName+" at "+getTokenErrorDisplay(e.token);
        }
        else if ( e instanceof MismatchedTokenException ) {
            MismatchedTokenException mte = (MismatchedTokenException)e;
            String tokenName="<unknown>";
            if ( mte.expecting== Token.EOF ) {
                tokenName = "EOF";
            }
            else {
                tokenName = tokenNames[mte.expecting];
            }
            msg = "mismatched input "+getTokenErrorDisplay(e.token)+
                " expecting "+tokenName;
        }
        else if ( e instanceof MismatchedTreeNodeException ) {
            MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
            String tokenName="<unknown>";
            if ( mtne.expecting==Token.EOF ) {
                tokenName = "EOF";
            }
            else {
                tokenName = tokenNames[mtne.expecting];
            }
            msg = "mismatched tree node: "+mtne.node+
                " expecting "+tokenName;
        }
        else if ( e instanceof NoViableAltException ) {
            NoViableAltException nvae = (NoViableAltException)e;
            msg = "no viable alternative at input "+getTokenErrorDisplay(e.token);
            msg += "\ndecision=<<" + nvae.grammarDecisionDescription+">>";
            msg += "\n(decision=" + nvae.decisionNumber+")";
            msg += "\n(state" + nvae.stateNumber+")";
        }
        else if ( e instanceof EarlyExitException ) {
            EarlyExitException eee = (EarlyExitException)e;
            msg = "required (...)+ loop did not match anything at input "+getTokenErrorDisplay(e.token);
            msg += "\n(decision=" + eee.decisionNumber+")";               
        }
        else if ( e instanceof MismatchedSetException ) {
            MismatchedSetException mse = (MismatchedSetException)e;
            msg = "mismatched input "+getTokenErrorDisplay(e.token)+
                " expecting set "+mse.expecting;
        }
        else if ( e instanceof MismatchedNotSetException ) {
            MismatchedNotSetException mse = (MismatchedNotSetException)e;
            msg = "mismatched input "+getTokenErrorDisplay(e.token)+
                " expecting set "+mse.expecting;
        }
        else if ( e instanceof FailedPredicateException ) {
            FailedPredicateException fpe = (FailedPredicateException)e;
            msg = "rule "+fpe.ruleName+" failed predicate: {"+
                fpe.predicateText+"}?";
        }
        return msg;
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

    // The following methods are used form the generated parser
    // The short names help keeping the grammar source file smaller and readable

    public void pushXMLLexer() {
        XMLLexer xmlLexer = new XMLLexer(source);
        xmlLexer.setIsWsExplicit(true);
        pushLexer(xmlLexer);
    }
    
    public void pushXQueryLexer() {
        XQueryLexer xqueryLexer = new XQueryLexer(source);
        pushLexer(xqueryLexer);
    }

    @Override
    public void reportError(RecognitionException e) {
        super.reportError(e);
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
