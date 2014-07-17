/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import com.google.common.collect.ImmutableList;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.plugins.xquery.language.XQuery;
import org.sonar.plugins.xquery.language.XQueryCodeColorizerFormat;
import org.sonar.plugins.xquery.language.XQuerySourceImporter;
import org.sonar.plugins.xquery.rules.XQueryProfile;
import org.sonar.plugins.xquery.rules.XQueryRulesRepository;
import org.sonar.plugins.xquery.test.SurefireXQueryParser;
import org.sonar.plugins.xquery.test.XQueryTestSensor;

import java.util.List;

@Properties({
    @Property(key = XQueryConstants.FILE_EXTENSIONS_KEY,
        name = "File extensions",
        description = "List of file extensions that will be scanned.",
        defaultValue = XQueryConstants.DEFAULT_FILE_EXTENSIONS_STRING,
        global = true,
        project = true),
    @Property(key = XQueryConstants.SOURCE_DIRECTORY_KEY,
        name = "Source directory",
        description = "Source directory that will be scanned.",
        defaultValue = XQueryConstants.DEFAULT_SOURCE_DIRECTORY,
        global = false,
        project = true),
    @Property(key = XQueryConstants.XQTEST_REPORTS_DIRECTORY_KEY,
        name = "Reports path",
        description = "Path (absolute or relative) to XML report files.",
        defaultValue = XQueryConstants.DEFAULT_XQTEST_DIRECTORY,
        project = true,
        global = false)
})
public class XQueryPlugin extends SonarPlugin {

    public List getExtensions() {
        return ImmutableList.of(
            // Core classes
            XQuery.class,
            XQuerySourceImporter.class,
            XQueryCodeColorizerFormat.class,

            // XQuery rules/profiles
            XQueryProfile.class,
            XQueryRulesRepository.class,

            // XQuery sensors
            XQuerySensor.class,
            XQueryTestSensor.class,

            // XQuery Test parser
            SurefireXQueryParser.class
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
