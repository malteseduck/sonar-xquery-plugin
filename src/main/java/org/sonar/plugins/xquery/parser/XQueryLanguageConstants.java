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

public interface XQueryLanguageConstants {

    public static final int LANGUAGE_XQUERY = 0x00000000; // 0000 0000
    public static final int LANGUAGE_XQUERY_UPDATE = 0x00000001; // 0000 0001
    public static final int LANGUAGE_XQUERY_SCRIPTING = 0x00000003; // 0000 0011
    public static final int LANGUAGE_XQUERY_FULLTEXT = 0x00000004; // 0000 0100
    public static final int LANGUAGE_XQUERY_ZORBA = 0x00000008; // 0000 1000
    public static final int LANGUAGE_XQUERY_MARK_LOGIC = 0x00000010; // 0001
    // 0000

    // constant shortcuts to be used inside the grammar source
    // file in order to keep the grammar source small and readable

    public static final int XQ = LANGUAGE_XQUERY;
    public static final int XQU = LANGUAGE_XQUERY_UPDATE;
    public static final int XQS = LANGUAGE_XQUERY_SCRIPTING;
    public static final int XQF = LANGUAGE_XQUERY_FULLTEXT;
    public static final int ZORBA = LANGUAGE_XQUERY_ZORBA;
    public static final int MLS = LANGUAGE_XQUERY_MARK_LOGIC;
}