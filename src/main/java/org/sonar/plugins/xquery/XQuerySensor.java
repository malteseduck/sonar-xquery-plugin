/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.plugins.xquery.language.SourceCode;
import org.sonar.plugins.xquery.language.XQueryFile;
import org.sonar.plugins.xquery.language.XQueryLineCountParser;
import org.sonar.plugins.xquery.language.XQuerySourceCode;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;
import org.sonar.plugins.xquery.rules.XQueryRulesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XQuerySensor implements Sensor {

    private static final Logger logger = Logger.getLogger(XQuerySensor.class.getName());

    private final RulesProfile profile;

    public XQuerySensor(RulesProfile profile) {
        this.profile = profile;
    }

    public void analyse(Project project, SensorContext context) {
        List<XQueryAstVisitor> visitors = new ArrayList<XQueryAstVisitor>();
        visitors.addAll(XQueryRulesRepository.createChecks(profile));
        ProjectFileSystem fileSystem = project.getFileSystem();
        
        // Create a mapper and add it to the visitors so that it can keep track
        // of global declarations and the local declaration stack
        DependencyMapper mapper = new DependencyMapper();
        visitors.add(mapper);

        // Do the first pass to map all the global dependencies
        logger.info("Scanning all files to map dependencies");
        for (InputFile inputfile : fileSystem.mainFiles(XQueryConstants.XQUERY_LANGUAGE_KEY)) {

            try {
                Resource<?> resource = XQueryFile.fromIOFile(inputfile.getFile(), fileSystem.getSourceDirs());
                SourceCode sourceCode = new XQuerySourceCode(resource, inputfile);
                logger.fine("Mapping " + resource.getLongName());

                XQueryAstParser parser = new XQueryAstParser(sourceCode, Arrays.asList(new XQueryAstVisitor[] { mapper }));
                XQueryTree tree = parser.parse();
                parser.mapDependencies(tree, mapper);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not map the dependencies in the file " + inputfile.getFile().getAbsolutePath(), e);
            }
        }

        // Now that the global mappings are done we can change the mode to "local"
        mapper.setMode("local");
                
        // Do the second pass to process the checks and other metrics
        logger.info("Scanning all files and gathering metrics");
        for (InputFile inputfile : fileSystem.mainFiles(XQueryConstants.XQUERY_LANGUAGE_KEY)) {

            try {
                Resource<?> resource = XQueryFile.fromIOFile(inputfile.getFile(), fileSystem.getSourceDirs());
                SourceCode sourceCode = new XQuerySourceCode(resource, inputfile);
                logger.fine("Analyzing " + resource.getLongName());

                XQueryAstParser parser = new XQueryAstParser(sourceCode, visitors);
                ProblemReporter reporter = new ProblemReporter();
                XQueryTree tree = parser.parse(reporter);
                parser.process(tree, mapper, reporter);

                // Count the lines of code
                new XQueryLineCountParser(sourceCode).count();

                // Save all the collected metrics
                saveMetrics(context, sourceCode);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not analyze the file " + inputfile.getFile().getAbsolutePath(), e);
            }
        }
    }

    private void saveMetrics(SensorContext context, SourceCode sourceCode) {
        for (Violation violation : sourceCode.getViolations()) {
            logger.finer("Saving violation: " + violation);
            context.saveViolation(violation);
        }
        for (Measure measure : sourceCode.getMeasures()) {
            logger.finer("Saving measure: " + measure);
            context.saveMeasure(sourceCode.getResource(), measure);
        }
        for (Dependency dependency : sourceCode.getDependencies()) {
            logger.finer("Saving dependency: " + dependency);
            context.saveDependency(dependency);
        }
    }

    public boolean shouldExecuteOnProject(Project project) {
        return XQueryConstants.XQUERY_LANGUAGE_KEY.equals(project.getLanguageKey());
    }
}
