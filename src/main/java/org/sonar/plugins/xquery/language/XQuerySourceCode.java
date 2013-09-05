/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.squid.api.SourceCodeEdgeUsage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XQuerySourceCode implements SourceCode {

    private List<String> code = new ArrayList<String>();
    private final InputFile file;
    private final Resource<?> resource;
    private final List<Measure> measures = new ArrayList<Measure>();
    private final List<Dependency> dependencies = new ArrayList<Dependency>();
    private final List<Violation> violations = new ArrayList<Violation>();

    /**
     * Creates a source code object using the string code.
     * 
     * @param code
     *            a string of source code
     */
    public XQuerySourceCode(String code) {
        this(Arrays.asList(StringUtils.split(code, '\n')));
    }

    public XQuerySourceCode(Resource<?> resource, List<String> code) {
        this.code = code;
        this.resource = resource;
        this.file = null;  
    }
    
    /**
     * Creates a source code object using the list of strings for code. Since
     * this is not a file it uses the the code string as
     * the source name.
     * 
     * @param code
     *            a list of strings code lines
     */
    public XQuerySourceCode(List<String> code) {
        this(new XQueryFile("'" + StringUtils.join(code, "\n") + "'\n"), code);
    }

    public XQuerySourceCode(Resource<?> resource, InputFile file) {
        this.resource = resource;
        this.file = file;
    }

    public String getCodeString() {
        return StringUtils.join(getCode(), "\n");
    }

    public List<String> getCode() {
        if (file != null && code.size() == 0) {
            try {
                code = FileUtils.readLines(file.getFile(), "UTF-8");
                return code;
            } catch (IOException e) {
                throw new SonarException(e);
            }
        } else {
            return code;
        }
    }

    public Resource<?> getResource() {
        return resource;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void addViolation(Violation violation) {
        this.violations.add(violation);
    }

    public void addMeasure(Metric metric, double value) {
        Measure measure = new Measure(metric, value);
        this.measures.add(measure);
    }

    public void addDependency(Resource<?> dependencyResource) {
        Dependency dependency = new Dependency(resource, dependencyResource);
        dependency.setUsage(SourceCodeEdgeUsage.USES.name());
        dependency.setWeight(1);

        dependencies.add(dependency);
    }

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
