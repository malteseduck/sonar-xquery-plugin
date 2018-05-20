/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language

import org.sonar.api.design.Dependency
import org.sonar.api.measures.Measure
import org.sonar.api.measures.Metric
import org.sonar.api.resources.File
import java.io.Serializable

interface SourceCode {

    val codeString: String

    val code: List<String>

    val issues: List<Issue>

    val dependencies: List<Dependency>

    val measures: List<Measure<*>>

    val resource: File

    fun addIssue(issue: Issue)

    fun <T : Serializable> addMeasure(metric: Metric<T>, value: Double)

    fun addDependency(dependencyResource: File)

    fun <T : Serializable> getMeasure(metric: Metric<T>): Measure<*>?

}
