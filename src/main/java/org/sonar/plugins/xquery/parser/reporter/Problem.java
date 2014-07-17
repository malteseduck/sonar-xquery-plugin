/*
 * © 2014 by Intellectual Reserve, Inc. All rights reserved.
 */

package org.sonar.plugins.xquery.parser.reporter;

import org.antlr.runtime.Token;

public class Problem {
    private String id;
    private Token token;

    private String message;

    public Problem(String id, String message, Token token) {
        this.id = id;
        this.message = message;
        this.token = token;
    }
    public int getCharPositionInLine() {
        if (token != null) {
            return token.getCharPositionInLine();
        }
        return 0;
    }
    
    public String getId() {
        return id;
    }
    
    public int getLine() {
        if (token != null) {
            return token.getLine();
        }
        return 0;
    }
    
    public String getMessage() {
        return message;
    }

    public String getMessageString() {
        StringBuffer buffer = new StringBuffer(" - line ");
        buffer.append(getLine()).append(":").append(getCharPositionInLine()).append(" - ").append(getMessage());
        return buffer.toString();
    }

    public Token getToken() {
        return token;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setToken(Token token) {
        this.token = token;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("Problem [");
        buffer.append(getLine()).append(":").append(getCharPositionInLine()).append(" - ").append(getMessage()).append("]");
        return buffer.toString();
    }
}
