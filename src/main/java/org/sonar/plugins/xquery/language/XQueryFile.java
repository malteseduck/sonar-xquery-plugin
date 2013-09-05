/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.api.resources.*;
import org.sonar.api.utils.WildcardPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an implementation of a resource of type FILE
 *
 * @since 1.10
 */
public class XQueryFile extends Resource<Directory> {

    private String directoryKey;
    private String filename;
    private Language language;
    private Directory parent;
    private boolean unitTest = false;

    private static String parseKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        key = key.replace('\\', '/');
        key = StringUtils.trim(key);
        return key;
    }

    public XQueryFile(String key) {
        if (key == null) {
            throw new IllegalArgumentException("XQuery File key is null");
        }
        this.language = XQuery.INSTANCE;
        String realKey = parseKey(key);
        if (realKey.indexOf(Directory.SEPARATOR) >= 0) {
            this.directoryKey = Directory.parseKey(StringUtils.substringBeforeLast(key, Directory.SEPARATOR));
            this.filename = StringUtils.substringAfterLast(realKey, Directory.SEPARATOR);
            //realKey = new StringBuilder().append(this.directoryKey).append(Directory.SEPARATOR).append(filename).toString();

        } else {
            this.filename = key;
        }
        setKey(realKey);
    }

    /**
     * Creates a file from its containing directory and name
     */
    public XQueryFile(String directory, String filename) {
        this.filename = StringUtils.trim(filename);
        if (StringUtils.isBlank(directory)) {
            setKey(filename);

        } else {
            this.directoryKey = Directory.parseKey(directory);
            setKey(new StringBuilder().append(directoryKey).append(Directory.SEPARATOR).append(this.filename).toString());
        }
    }

    /**
     * Creates a File from its language and its key
     */
    public XQueryFile(String key, boolean unitTest) {
        this(key);
        this.setUnitTest(unitTest);
    }

    /**
     * Creates a File from language, directory and filename
     */
    public XQueryFile(String directory, String filename, boolean unitTest) {
        this(directory, filename);
        this.setUnitTest(unitTest);
    }

    /**
     * {@inheritDoc}
     *
     * @see Resource#getParent()
     */
    public Directory getParent() {
        if (parent == null) {
            parent = new Directory(directoryKey, getLanguage());
        }
        return parent;
    }

    /**
     * {@inheritDoc}
     *
     * @see Resource#matchFilePattern(String)
     */
    public boolean matchFilePattern(String antPattern) {
        WildcardPattern matcher = WildcardPattern.create(antPattern, "/");
        return matcher.match(getKey());
    }

    /**
     * Creates a File from an io.file and a list of sources directories
     */
    public static XQueryFile fromIOFile(java.io.File file, List<java.io.File> sourceDirs) {
        return fromIOFile(file, sourceDirs, false);
    }

    public static XQueryFile fromIOFile(java.io.File file, List<java.io.File> sourceDirs, boolean unitTest) {
        String relativePath = getRelativePath(file, sourceDirs);
        if (relativePath != null) {
            return new XQueryFile(relativePath, unitTest);
        }
        return null;
    }

    public static String getRelativePath(java.io.File file, List<java.io.File> dirs) {
        List<String> stack = new ArrayList<String>();
        String path = FilenameUtils.normalize(file.getAbsolutePath());
        java.io.File cursor = new java.io.File(path);
        while (cursor != null) {
            if (containsFile(dirs, cursor)) {
                return StringUtils.join(stack, "/");
            }
            stack.add(0, cursor.getName());
            cursor = cursor.getParentFile();
        }
        return null;
    }

    private static boolean containsFile(List<java.io.File> dirs, java.io.File cursor) {
        for (java.io.File dir : dirs) {
            if (FilenameUtils.equalsNormalizedOnSystem(dir.getAbsolutePath(), cursor.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see Resource#getName()
     */
    public String getName() {
        return filename;
    }

    /**
     * {@inheritDoc}
     *
     * @see Resource#getLongName()
     */
    public String getLongName() {
        return getKey();
    }

    /**
     * {@inheritDoc}
     *
     * @see Resource#getDescription()
     */
    public String getDescription() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see Resource#getLanguage()
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @return SCOPE_ENTITY
     */
    public final String getScope() {
        return Scopes.FILE;
    }

    /**
     * Returns the qualifier associated to this File. Should be QUALIFIER_FILE
     * or QUALIFIER_UNIT_TEST_FILE
     */
    public String getQualifier() {
        return isUnitTest() ? Qualifiers.UNIT_TEST_FILE : Qualifiers.CLASS;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("key", getKey())
                .append("dir", directoryKey)
                .append("filename", filename)
                .append("unitTest", isUnitTest())
                .toString();
    }

    public boolean isUnitTest() {
        return unitTest;
    }

    public void setUnitTest(boolean unitTest) {
        this.unitTest = unitTest;
    }
}