/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xquery.api.XQueryConstants;

import java.io.File;

public class XQueryTestSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(XQueryTestSensor.class);

    private final SurefireXQueryParser surefireXQueryParser;
    private final Settings settings;
    private final FileSystem fileSystem;

    public XQueryTestSensor(SurefireXQueryParser surefireXQueryParser, Settings settings, FileSystem fileSystem) {
        this.surefireXQueryParser = surefireXQueryParser;
        this.settings = settings;
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return fileSystem.files(fileSystem.predicates().hasLanguage(XQueryConstants.XQUERY_LANGUAGE_KEY)).iterator().hasNext();
    }

    @Override
    public void analyse(Project project, SensorContext context) {
        File dir = new File(settings.getString(XQueryConstants.XQTEST_REPORTS_DIRECTORY_KEY));
        collect(project, context, dir);
    }

    protected void collect(Project project, SensorContext context, File reportsDir) {
        LOGGER.info("parsing {}", reportsDir);
        surefireXQueryParser.collect(project, context, reportsDir);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
