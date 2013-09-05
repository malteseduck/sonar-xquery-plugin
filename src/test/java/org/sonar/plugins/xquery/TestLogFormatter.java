/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TestLogFormatter extends Formatter {
    
    private static final String lineSep = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder output = new StringBuilder()
            .append(record.getMessage()).append(' ')
            .append(lineSep);
        return output.toString();       
    }
 
}