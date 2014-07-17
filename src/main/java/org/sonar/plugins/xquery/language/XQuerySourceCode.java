/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

/*
 * (c) 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.SonarException;
import org.sonar.squid.api.SourceCodeEdgeUsage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XQuerySourceCode implements SourceCode {

    private final File inputFile;
    private List<String> code = new ArrayList<String>();
    private final org.sonar.api.resources.File resource;
    private final List<Measure> measures = new ArrayList<Measure>();
    private final List<Dependency> dependencies = new ArrayList<Dependency>();

    private final List<Issue> issues = new ArrayList<Issue>();

    /**
     * Creates a source code object using the string code.
     *
     * @param code a string of source code
     */
    public XQuerySourceCode(String code) {
        this(Arrays.asList(StringUtils.split(code, '\n')));
    }

    public XQuerySourceCode(org.sonar.api.resources.File resource, List<String> code, File inputFile) {
        this.code = code;
        this.resource = resource;
        this.inputFile = inputFile;
    }

    /**
     * Creates a source code object using the list of strings for code. Since
     * this is not a file it uses the the code string as
     * the source name.
     *
     * @param code a list of strings code lines
     */
    public XQuerySourceCode(List<String> code) {
        this(org.sonar.api.resources.File.create("'" + StringUtils.join(code, "\n") + "'\n"), code, null);
    }

    public XQuerySourceCode(org.sonar.api.resources.File resource, File inputFile) {
        this.resource = resource;
        this.inputFile = inputFile;
    }

    @Override
    public String getCodeString() {
        return StringUtils.join(getCode(), "\n");
    }

    @Override
    public List<String> getCode() {
        if (inputFile != null && code.size() == 0) {
            try {
                code = FileUtils.readLines(inputFile, "UTF-8");
                return code;
            } catch (IOException e) {
                throw new SonarException(e);
            }
        } else {
            return code;
        }
    }

    public org.sonar.api.resources.File getResource() {
        return resource;
    }

    @Override
    public List<Issue> getIssues() {
        return issues;
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public List<Measure> getMeasures() {
        return measures;
    }

    @Override
    public void addIssue(Issue issue) {
        this.issues.add(issue);
    }

    @Override
    public void addMeasure(Metric metric, double value) {
        Measure measure = new Measure(metric, value);
        this.measures.add(measure);
    }

    @Override
    public void addDependency(org.sonar.api.resources.File dependencyResource) {
        Dependency dependency = new Dependency(resource, dependencyResource);
        dependency.setUsage(SourceCodeEdgeUsage.USES.name());
        dependency.setWeight(1);

        dependencies.add(dependency);
    }

    @Override
    public Measure getMeasure(Metric metric) {
        for (Measure measure : measures) {
            if (measure.getMetric().equals(metric)) {
                return measure;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return resource.getLongName();
    }
}
