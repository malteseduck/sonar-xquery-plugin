/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.test;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.test.MutableTestPlan;
import org.sonar.api.test.TestCase;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.surefire.data.SurefireStaxHandler;
import org.sonar.plugins.surefire.data.UnitTestClassReport;
import org.sonar.plugins.surefire.data.UnitTestIndex;
import org.sonar.plugins.surefire.data.UnitTestResult;
import org.sonar.plugins.xquery.api.XQueryConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

public class SurefireXQueryParser implements BatchExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireXQueryParser.class);
    private final ResourcePerspectives perspectives;
    private final Settings settings;

    public SurefireXQueryParser(ResourcePerspectives perspectives, Settings settings) {
        this.perspectives = perspectives;
        this.settings = settings;
    }

    public void collect(Project project, SensorContext context, File reportsDir) {
        File[] xmlFiles = getReports(reportsDir);
        if (xmlFiles.length > 0) {
            parseFiles(context, xmlFiles);
        }
    }

    private File[] getReports(File dir) {
        if (dir == null) {
            return new File[0];
        } else if (!dir.isDirectory()) {
            LOGGER.warn("Reports path not found: " + dir.getAbsolutePath());
            return new File[0];
        }
        File[] unitTestResultFiles = findXMLFilesStartingWith(dir, "TEST-");
        if (unitTestResultFiles.length == 0) {
            // maybe there's only a test suite result file
            unitTestResultFiles = findXMLFilesStartingWith(dir, "TESTS-");
        }
        return unitTestResultFiles;
    }

    private File[] findXMLFilesStartingWith(File dir, final String fileNameStart) {
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(fileNameStart) && name.endsWith(".xml");
            }
        });
    }

    private void parseFiles(SensorContext context, File[] reports) {
        UnitTestIndex index = new UnitTestIndex();
        parseFiles(reports, index);
        save(index, context);
    }

    private void parseFiles(File[] reports, UnitTestIndex index) {
        SurefireStaxHandler staxParser = new SurefireStaxHandler(index);
        StaxParser parser = new StaxParser(staxParser, false);
        for (File report : reports) {
            try {
                parser.parse(report);
            } catch (XMLStreamException e) {
                throw new SonarException("Fail to parse the Surefire report: " + report, e);
            }
        }
    }

    private void save(UnitTestIndex index, SensorContext context) {
        for (Map.Entry<String, UnitTestClassReport> entry : index.getIndexByClassname().entrySet()) {
            UnitTestClassReport report = entry.getValue();
            if (report.getTests() > 0) {
                org.sonar.api.resources.File resource = org.sonar.api.resources.File.create(settings.getString(XQueryConstants.SOURCE_DIRECTORY_KEY) + entry.getKey());
                if (resource != null) {
                    save(entry.getValue(), resource, context);
                } else {
                    LOGGER.warn("Resource not found: {}", entry.getKey());
                }
            }
        }
    }

    private void save(UnitTestClassReport report, Resource resource, SensorContext context) {
        double testsCount = report.getTests() - report.getSkipped();
        saveMeasure(context, resource, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
        saveMeasure(context, resource, CoreMetrics.TESTS, testsCount);
        saveMeasure(context, resource, CoreMetrics.TEST_ERRORS, report.getErrors());
        saveMeasure(context, resource, CoreMetrics.TEST_FAILURES, report.getFailures());
        saveMeasure(context, resource, CoreMetrics.TEST_EXECUTION_TIME, report.getDurationMilliseconds());
        double passedTests = testsCount - report.getErrors() - report.getFailures();
        if (testsCount > 0) {
            double percentage = passedTests * 100d / testsCount;
            saveMeasure(context, resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
        }
        saveResults(context, resource, report);
    }

    protected void saveResults(SensorContext context, Resource testFile, UnitTestClassReport report) {
        for (UnitTestResult unitTestResult : report.getResults()) {
            MutableTestPlan testPlan = perspectives.as(MutableTestPlan.class, testFile);
            if (testPlan != null) {
                testPlan.addTestCase(unitTestResult.getName())
                        .setDurationInMs(unitTestResult.getDurationMilliseconds())
                        .setStatus(TestCase.Status.of(unitTestResult.getStatus()))
                        .setMessage(unitTestResult.getMessage())
                        .setType(TestCase.TYPE_UNIT)
                        .setStackTrace(unitTestResult.getStackTrace());
            }
        }
    }

    private void saveMeasure(SensorContext context, Resource resource, Metric metric, double value) {
        if (!Double.isNaN(value)) {
            context.saveMeasure(resource, metric, value);
        }
    }

}