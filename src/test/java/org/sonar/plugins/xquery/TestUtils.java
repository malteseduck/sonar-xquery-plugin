/*
 * © 2013 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    
    public static List<String> list(String... strings) {
        List<String> list = new ArrayList<String>();
        for (String string : strings) {
            list.add(string);
        }
        return list;        
    }
}
