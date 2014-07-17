/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.File;

import java.util.List;

public interface SourceCode {

    public String getCodeString();

    public List<String> getCode();

    public List<Issue> getIssues();

    public List<Dependency> getDependencies();

    public List<Measure> getMeasures();

    public void addIssue(Issue issue);

    public void addMeasure(Metric metric, double value);

    public void addDependency(File dependencyResource);

    public File getResource();

    public Measure getMeasure(Metric metric);

}
