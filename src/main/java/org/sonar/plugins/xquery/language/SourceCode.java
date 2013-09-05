/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;

import java.util.List;

public interface SourceCode {

    public String getCodeString();

    public List<String> getCode();

    public List<Violation> getViolations();

    public List<Dependency> getDependencies();

    public List<Measure> getMeasures();

    public void addViolation(Violation violation);

    public void addMeasure(Metric metric, double value);

    public void addDependency(Resource<?> dependencyResource);

    public Resource<?> getResource();

    public Measure getMeasure(Metric metric);

}
