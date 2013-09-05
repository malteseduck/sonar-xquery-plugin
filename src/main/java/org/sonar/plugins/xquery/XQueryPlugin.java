/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.xquery.api.XQueryConstants;
import org.sonar.plugins.xquery.language.XQuery;
import org.sonar.plugins.xquery.language.XQueryCodeColorizerFormat;
import org.sonar.plugins.xquery.language.XQuerySourceImporter;
import org.sonar.plugins.xquery.rules.XQueryProfile;
import org.sonar.plugins.xquery.rules.XQueryRulesRepository;
import org.sonar.plugins.xquery.xqtest.XQTestSensor;

import java.util.ArrayList;
import java.util.List;

@Properties({
	@Property(key = XQueryConstants.FILE_EXTENSIONS_KEY,
	    name = "File extensions",
	    description = "List of file extensions that will be scanned.",
	    defaultValue = XQueryConstants.DEFAULT_FILE_EXTENSIONS_STRING,
	    global = true, 
	    project = true),
	@Property(key = XQueryConstants.SOURCE_DIRECTORY_KEY,
	    name = "Source directory",
	    description = "Source directory that will be scanned.",
	    defaultValue = XQueryConstants.DEFAULT_SOURCE_DIRECTORY,
	    global = false, 
	    project = true),
    @Property(key = XQueryConstants.XQTEST_REPORTS_DIRECTORY_KEY,
        name = "Reports path",
        description = "Path (absolute or relative) to XML report files.",
        defaultValue = XQueryConstants.DEFAULT_XQTEST_DIRECTORY,
        project = true,
        global = false)
})
public class XQueryPlugin extends SonarPlugin {

	public List<Class<? extends Extension>> getExtensions() {
		List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
		
		// XQuery language
		list.add(XQuery.class);
		list.add(XQuerySourceImporter.class);
		list.add(XQueryCodeColorizerFormat.class);
		
		// XQuery rules/profiles
		list.add(XQueryProfile.class);
        list.add(XQueryRulesRepository.class);
				
		// XQuery sensors
		list.add(XQuerySensor.class);
        list.add(XQTestSensor.class);
		
		return list;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
