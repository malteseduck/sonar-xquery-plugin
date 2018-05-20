/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

/*
 * (c) 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.sonar.api.design.Dependency
import org.sonar.api.measures.Measure
import org.sonar.api.measures.Metric
import org.sonar.api.utils.SonarException
import org.sonar.squid.api.SourceCodeEdgeUsage
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.*

class XQuerySourceCode : SourceCode {

    private var inputFile: File?
    override var code: List<String> = ArrayList()
    override var resource: org.sonar.api.resources.File
    override var measures = ArrayList<Measure<*>>()
    override var dependencies = ArrayList<Dependency>()
    override val issues = ArrayList<Issue>()

    override val codeString: String
        get() = StringUtils.join(code, "\n")

    /**
     * Creates a source code object using the string code.
     *
     * @param code a string of source code
     */
    constructor(code: String) : this(Arrays.asList<String>(*StringUtils.split(code, '\n')))

    constructor(resource: org.sonar.api.resources.File, code: List<String>, inputFile: File?) {
        this.inputFile = inputFile
        this.code = if (inputFile != null) {
            FileUtils.readLines(inputFile, "UTF-8")
        } else {
            code
        }
        this.resource = resource
    }

    /**
     * Creates a source code object using the list of strings for code. Since
     * this is not a file it uses the the code string as
     * the source name.
     *
     * @param code a list of strings code lines
     */
    constructor(code: List<String>) : this(org.sonar.api.resources.File.create("'" + StringUtils.join(code, "\n") + "'\n"), code, null)

    constructor(resource: org.sonar.api.resources.File, inputFile: File): this(resource, listOf(), inputFile)

    override fun addIssue(issue: Issue) {
        this.issues.add(issue)
    }

    override fun <T: Serializable>addMeasure(metric: Metric<T>, value: Double) {
        val measure = Measure<T>(metric, value)
        this.measures.add(measure)
    }

    override fun addDependency(dependencyResource: org.sonar.api.resources.File) {
        val dependency = Dependency(resource, dependencyResource)
        dependency.usage = SourceCodeEdgeUsage.USES.name
        dependency.weight = 1

        dependencies.add(dependency)
    }

    override fun <T: Serializable> getMeasure(metric: Metric<T>): Measure<*>? {
        for (measure in measures) {
            if (measure.metric == metric) {
                return measure
            }
        }
        return null
    }

    override fun toString(): String {
        return resource.longName
    }
}
