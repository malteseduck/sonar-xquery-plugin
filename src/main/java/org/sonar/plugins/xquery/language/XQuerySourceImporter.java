/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xquery.api.XQueryConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Import of source files to sonar database.
 * 
 * @since 1.0
 */
public final class XQuerySourceImporter extends AbstractSourceImporter {

    private static final Logger logger = Logger.getLogger(XQuerySourceImporter.class.getName());
    private final Project project;

    public XQuerySourceImporter(Project project) {
        super(XQuery.INSTANCE);
        this.project = project;
    }

    private void saveSource(SensorContext context, InputFile file, boolean unitTest) {
        Resource resource = createResource(file.getFile(), project.getFileSystem().getSourceDirs(), unitTest);
        if (resource != null) {
            try {
                context.index(resource);
                String source = FileUtils.readFileToString(file.getFile(), project.getFileSystem().getSourceCharset().name());
                context.saveSource(resource, source);

            } catch (IOException e) {
                throw new SonarException("Unable to read and import the source file : '" + file.getFile().getAbsolutePath() + "' with the charset : '" + project.getFileSystem().getSourceCharset().name() + "'. You should check the property " + CoreProperties.ENCODING_PROPERTY, e);
            }
        }
    }
    
    @Override
    public void analyse(Project project, SensorContext context) {
        for (InputFile file : project.getFileSystem().mainFiles(XQueryConstants.XQUERY_LANGUAGE_KEY)) {
            // Check to see if this is a test and set it appropriately in the resource
            boolean isTest = false;
            String path = file.getFileBaseDir().getPath() + "/" + file.getRelativePath();
            for (File dir : project.getFileSystem().getTestDirs()) {
                if (StringUtils.startsWith(path, dir.getPath())) {
                    isTest = true;
                }
            }
            if (isTest) {
                saveSource(context, file, true);
            } else {
                saveSource(context, file, false);                
            }
        }
    }

    @Override
    protected Resource createResource(File file, List<File> sourceDirs, boolean unitTest) {
        Resource resource = XQueryFile.fromIOFile(file, sourceDirs, unitTest);
        if (resource == null) {
            logger.fine("XquerySourceImporter failed for: " + file.getPath());
        } else {
            logger.fine("XquerySourceImporter:" + file.getPath());
        }
        return resource;
    }
}