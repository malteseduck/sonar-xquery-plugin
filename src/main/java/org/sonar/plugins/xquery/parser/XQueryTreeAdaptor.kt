/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

/*******************************************************************************
 * Copyright (c) 2008, 2009 28msec Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Gabriel Petrovay (28msec) - initial API and implementation
 *
 * Modified
 * Chris Cieslinski
 */

package org.sonar.plugins.xquery.parser

import org.antlr.runtime.CommonToken
import org.antlr.runtime.RecognitionException
import org.antlr.v4.runtime.Token
import org.antlr.runtime.TokenStream
import org.antlr.runtime.tree.CommonTreeAdaptor
import org.antlr.runtime.tree.Tree

class XQueryTreeAdaptor : CommonTreeAdaptor()//    private boolean failOnError;
//
//    public XQueryTreeAdaptor() {
//        this(false);
//    }
//
//    public XQueryTreeAdaptor(boolean failOnError) {
//        this.failOnError = failOnError;
//    }
//
//    public Object create(Token payload) {
//        return new XQueryTree(payload);
//    }
//
//    @Override
//    public void setTokenBoundaries(Object t, Token startToken, Token stopToken) {
//        if (t == null) {
//            return;
//        }
//        int startTI = 0;
//        int stopTI = 0;
//        int startTS = 0;
//        int stopTS = 0;
//        if (startToken != null) {
//            startTI = startToken.getTokenIndex();
//            startTS = ((CommonToken) startToken).getStartIndex();
//        }
//        if (stopToken != null) {
//            stopTI = stopToken.getTokenIndex();
//            stopTS = ((CommonToken) stopToken).getStopIndex();
//        }
//        ((Tree) t).setTokenStartIndex(startTI);
//        ((Tree) t).setTokenStopIndex(stopTI);
//        if (t instanceof XQueryTree) {
//            XQueryTree xct = (XQueryTree) t;
//            xct.setStart(startTS);
//            xct.setStop(stopTS);
//        }
//    }
//
//    @Override
//    public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
//        try {
//            if (failOnError) {
//                e.printStackTrace();
//            }
//            @SuppressWarnings("unused") CommonToken ctStart = null, ctStop = null;
//            boolean reverse = start.getTokenIndex() > stop.getTokenIndex();
//            if (reverse) {
//                ctStart = (CommonToken) stop;
//                ctStop = (CommonToken) start;
//            } else {
//                ctStart = (CommonToken) start;
//                ctStop = (CommonToken) stop;
//            }
//
//        } catch (Exception e2) {
//            if (failOnError) {
//                e2.printStackTrace();
//            }
//        }
//
//        XQueryErrorNode node = new XQueryErrorNode(input, start, stop, e);
//
//        if (failOnError) {
//            throw new RuntimeException(node.toString(), e);
//        }
//        return node;
//    }
