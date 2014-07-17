/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.sonar.api.batch.AbstractSourceImporter;

public class XQuerySourceImporter extends AbstractSourceImporter {

    /**
     * Instantiates a new xquery source importer.
     */
    public XQuerySourceImporter(XQuery xQuery) {
        super(xQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "XQuery Source Importer";
    }
}