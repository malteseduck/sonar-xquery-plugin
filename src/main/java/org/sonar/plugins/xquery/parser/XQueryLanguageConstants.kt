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

interface XQueryLanguageConstants {
    companion object {

        val LANGUAGE_XQUERY = 0x00000000 // 0000 0000
        val LANGUAGE_XQUERY_UPDATE = 0x00000001 // 0000 0001
        val LANGUAGE_XQUERY_SCRIPTING = 0x00000003 // 0000 0011
        val LANGUAGE_XQUERY_FULLTEXT = 0x00000004 // 0000 0100
        val LANGUAGE_XQUERY_ZORBA = 0x00000008 // 0000 1000
        val LANGUAGE_XQUERY_MARK_LOGIC = 0x00000010 // 0001
        // 0000

        // constant shortcuts to be used inside the grammar source
        // file in order to keep the grammar source small and readable

        val XQ = LANGUAGE_XQUERY
        val XQU = LANGUAGE_XQUERY_UPDATE
        val XQS = LANGUAGE_XQUERY_SCRIPTING
        val XQF = LANGUAGE_XQUERY_FULLTEXT
        val ZORBA = LANGUAGE_XQUERY_ZORBA
        val MLS = LANGUAGE_XQUERY_MARK_LOGIC
    }
}