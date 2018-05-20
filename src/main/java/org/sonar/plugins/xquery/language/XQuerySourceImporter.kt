/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.sonar.api.batch.AbstractSourceImporter

class XQuerySourceImporter(xQuery: XQuery) : AbstractSourceImporter(xQuery) {

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return "XQuery Source Importer"
    }
}