/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.surefire.api.AbstractSurefireParser;
import org.sonar.plugins.xquery.api.XQueryConstants;

public class SurefireXQueryParser extends AbstractSurefireParser implements BatchExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireXQueryParser.class);
    private final Settings settings;

    public SurefireXQueryParser(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected Resource getUnitTestResource(String classKey) {
        return org.sonar.api.resources.File.create(settings.getString(XQueryConstants.SOURCE_DIRECTORY_KEY) + classKey);
    }
}