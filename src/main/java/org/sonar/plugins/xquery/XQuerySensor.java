/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.design.Dependency;
import org.sonar.api.issue.Issuable;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.plugins.xquery.language.*;
import org.sonar.plugins.xquery.parser.XQueryTree;
import org.sonar.plugins.xquery.parser.node.DependencyMapper;
import org.sonar.plugins.xquery.parser.reporter.ProblemReporter;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstParser;
import org.sonar.plugins.xquery.parser.visitor.XQueryAstVisitor;
import org.sonar.plugins.xquery.rules.CheckClasses;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XQuerySensor implements Sensor {

    private static final Logger logger = Logger.getLogger(XQuerySensor.class.getName());

    private final RulesProfile profile;
    private final ResourcePerspectives perspectives;
    private final AnnotationCheckFactory annotationCheckFactory;
    private SensorContext context;
    private Project project;

    public XQuerySensor(RulesProfile profile, ResourcePerspectives perspectives) {
        this.profile = profile;
        this.perspectives = perspectives;
        this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CheckClasses.REPOSITORY_KEY, CheckClasses.getChecks());
    }

    org.sonar.api.resources.File getSonarResource(File file) {
        return org.sonar.api.resources.File.fromIOFile(file, project);
    }

    @Override
    public void analyse(Project project, SensorContext context) {
        this.context = context;
        this.project = project;

        Collection<XQueryAstVisitor> checks = annotationCheckFactory.getChecks();
        List<XQueryAstVisitor> visitors = new ArrayList(checks);
        ProjectFileSystem fileSystem = project.getFileSystem();
        
        // Create a mapper and add it to the visitors so that it can keep track
        // of global declarations and the local declaration stack
        DependencyMapper mapper = new DependencyMapper();
        visitors.add(mapper);

        // Do the first pass to map all the global dependencies
        logger.info("Scanning all files to map dependencies");
        for (InputFile inputfile : fileSystem.mainFiles(XQueryConstants.XQUERY_LANGUAGE_KEY)) {

            try {
                Resource resource = XQueryFile.fromIOFile(inputfile.getFile(), fileSystem.getSourceDirs());
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
                Resource resource = XQueryFile.fromIOFile(inputfile.getFile(), fileSystem.getSourceDirs());
                org.sonar.api.resources.File sonarFile = getSonarResource(inputfile.getFile());
                SourceCode sourceCode = new XQuerySourceCode(resource, inputfile);
                logger.fine("Analyzing " + resource.getLongName());

                XQueryAstParser parser = new XQueryAstParser(sourceCode, visitors);
                ProblemReporter reporter = new ProblemReporter();
                XQueryTree tree = parser.parse(reporter);
                parser.process(tree, mapper, reporter);

                // Count the lines of code
                new XQueryLineCountParser(sourceCode).count();

                // Save all the collected metrics
                saveMetrics(sonarFile, context, sourceCode);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not analyze the file " + inputfile.getFile().getAbsolutePath(), e);
            }
        }
    }

    private void saveMetrics(org.sonar.api.resources.File sonarFile, SensorContext context, SourceCode sourceCode) {
        for (Issue issue : sourceCode.getIssues()) {
            logger.finer("Saving issue: " + issue);
            Issuable issuable = perspectives.as(Issuable.class, sonarFile);
            if (issuable != null) {
                org.sonar.api.issue.Issue sIssue = issuable.newIssueBuilder()
                        .ruleKey(issue.rule())
                        .line(issue.line())
                        .message(issue.message())
                        .build();
                issuable.addIssue(sIssue);
            }
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
