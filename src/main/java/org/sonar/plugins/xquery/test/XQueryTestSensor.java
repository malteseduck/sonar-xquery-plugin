/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.test;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xquery.api.XQueryConstants;

import java.io.File;
import java.util.logging.Logger;

public class XQueryTestSensor implements Sensor {

    private static final Logger logger = Logger.getLogger(XQueryTestSensor.class.getName());

    public boolean shouldExecuteOnProject(Project project) {
        return XQueryConstants.XQUERY_LANGUAGE_KEY.equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) {
        File dir = getReportsDirectory(project);
        collect(project, context, dir);
    }

    protected void collect(Project project, SensorContext context, File reportsDir) {
        logger.info("parsing " + reportsDir);
//        new AbstractSurefireParser() {
//            protected Resource getUnitTestResource(String key) {
//                return new XQueryFile(StringUtils.substringAfter(key, "/"), true);
//            }
//        }.collect(project, context, reportsDir);
    }

    public static File getReportsDirectory(Project project) {
        String path = (String) project.getProperty(XQueryConstants.XQTEST_REPORTS_DIRECTORY_KEY);
        if (path != null) {
            return project.getFileSystem().resolvePath(path);
        } else {
            return project.getFileSystem().resolvePath(XQueryConstants.DEFAULT_XQTEST_DIRECTORY);
        }
    }

}
