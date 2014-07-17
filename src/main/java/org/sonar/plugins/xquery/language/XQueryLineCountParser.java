/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.language;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xquery.checks.AbstractCheck;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Count lines of code in Xquery files.
 * 
 * @since 1.0
 */
public class XQueryLineCountParser extends AbstractCheck {

    private static final Logger logger = Logger.getLogger(XQueryLineCountParser.class.getName());
    
    private int blankLines = 0;
    private int commentLines = 0;
    private int linesOfCode = 0;
    private int functions = 0;

    private SourceCode sourceCode;
    
    public XQueryLineCountParser(SourceCode sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void count() {
        logger.fine("Count comment in " + sourceCode.getResource().getLongName());
        
        ArrayList<String> code = (ArrayList<String>) sourceCode.getCode();

        if (code != null && code.size() > 0) {
            try {
                boolean commenting = false;
                for (String line : code) {
                    String trimmed = StringUtils.trim(line);
                    if (StringUtils.isBlank(trimmed)) {
                        blankLines++;
                    } else if (trimmed.startsWith("(:") && trimmed.endsWith(":)")) {
                        commentLines++;
                    } else if (trimmed.startsWith("(:") && !trimmed.contains(":)") && !trimmed.endsWith(":)")) {
                        commenting = true;
                        commentLines++;
                    } else if (commenting && trimmed.contains(":)")) {
                        commenting = false;
                        commentLines++;
                    } else if (commenting) {
                        commentLines++;
                    } else {
                        if (trimmed.contains("declare function")) {
                            functions++;
                        }
                    }
                    
                    linesOfCode++;
                }                
            } catch (Exception e) {
                throw new SonarException(e);
            }
        }
                
        sourceCode.addMeasure(CoreMetrics.FUNCTIONS, (double) functions);
        sourceCode.addMeasure(CoreMetrics.LINES, (double) linesOfCode);
        sourceCode.addMeasure(CoreMetrics.COMMENT_LINES, (double) commentLines);
        sourceCode.addMeasure(CoreMetrics.NCLOC, (double) linesOfCode - blankLines - commentLines);        
    }
}
