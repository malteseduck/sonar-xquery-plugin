///*
// * © 2014 by Intellectual Reserve, Inc. All rights reserved.
// */
//
//package org.sonar.plugins.xquery.parser;
//
//import org.antlr.v4.runtime.CommonToken;
//import org.antlr.v4.runtime.Token;
//import org.antlr.runtime.tree.CommonTree;
//import org.antlr.v4.runtime.ParserRuleContext;
//import org.antlr.v4.runtime.RuleContext;
//import org.antlr.v4.runtime.tree.ErrorNode;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.antlr.v4.runtime.tree.Trees;
//import org.apache.commons.lang.StringUtils;
//import org.sonar.plugins.xquery.language.XQuery;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.IntStream;
//
//import static java.util.Arrays.asList;
//
//public class XQueryTree extends ParserRuleContext {
//
////    protected int start;
////
////    protected int stop;
//
//    private boolean stacked;
//
//    public XQueryTree() {
//    }
//
////    public XQueryTree(Token t) {
////        this.start = t;
////    }
//
//    public XQueryTree(ParserRuleContext ctx) {
//        this.parent = ctx.parent;
//        this.invokingState = ctx.invokingState;
//
//        this.start = ctx.start;
//        this.stop = ctx.stop;
//
//        // copy any error nodes to alt label node
//        if ( ctx.children!=null ) {
//            for (ParseTree child : ctx.children) {
//                addAnyChild(child);
//            }
//        }
//    }
//
//    public ParserRuleContext getContext() {
//        return context;
//    }
//
//    @Override
//    public int getRuleIndex() {
//        return context != null ? context.getRuleIndex() : super.getRuleIndex();
//    }
//
//    public ParserRuleContext findContext(String name) {
//        XQueryTree tree = find(name);
//        return tree != null ? tree.getContext() : null;
//    }
//
//    /**
//     * Finds the tree node that has the specified name (path). The name is
//     * comprised names of tokens separated by a ".". For example, to get the
//     * first FLWOR statement you could do something like "MainModule.FLWOR" and
//     * it will return the tree at the first FLWOR node in the first MainModule
//     * node. By default will skip node levels until it finds one that matches.
//     *
//     * @param name
//     *            The name of the node to find
//     * @return The node tree
//     */
//    public XQueryTree find(String name) {
//        return find(this, name, true);
//    }
//
//    /**
//     * See documentation for find(name). This method allows you to specify
//     * whether or not to skip node levels.
//     *
//     * @param name
//     *            The name of the node to find
//     * @param skipNodes
//     *            Whether or not to skip nodes when searching for the name
//     *            parts.
//     * @return The node tree
//     */
//    public XQueryTree find(String name, boolean skipNodes) {
//        return find(this, name, skipNodes);
//    }
//
//    /*
//     * Internal method that allows us to pass in the root of where to search for
//     * the node.
//     */
//    private XQueryTree find(XQueryTree root, String name, boolean skipNodes) {
// If we don't have a root or a name then don't go further
//        if (root == null || StringUtils.isBlank(name)) {
//            return null;
//        }
//
//        String checkName = StringUtils.substringBefore(name, ".");
//        String nodeName = root.getName();
//
//        // If we are at the "end" of the name we can just return where we are
//        if (StringUtils.equals(name, nodeName)) {
//            return root;
//
//            // Check the current name in the node, if we match then go to the
//            // next
//            // part of the name
//        } else if (StringUtils.equals(checkName, nodeName)) {
//            for (XQueryTree child : root.getChildren()) {
//                XQueryTree tree = find(child, StringUtils.substringAfter(name, "."), skipNodes);
//                if (tree != null) {
//                    return tree;
//                }
//            }
//            return null;
//
//            // If we didn't find anything at the current "level" then only
//            // continue if we are able to skip nodes of the tree
//        } else if (skipNodes) {
//            for (XQueryTree child : root.getChildren()) {
//                XQueryTree tree = find(child, name, skipNodes);
//                if (tree != null) {
//                    return tree;
//                }
//            }
//            return null;
//        }
//        return null;
//    }
//
//    /**
//     * Change the position function so that it will always have a somewhat valid
//     * number by inheriting from the first child that has a position instead of
//     * just checking only the first child.
//     */
//    public int getCharPositionInLine() {
//        if (start == null || start.getCharPositionInLine() == -1) {
//            for (XQueryTree child : getChildren()) {
//                int pos = child.getCharPositionInLine();
//                if (pos != -1) {
//                    return pos;
//                }
//            }
//            return 0;
//        }
//        return start.getCharPositionInLine();
//    }
//
//    /**
//     * Overridden so that we can give back an XQueryTree object instead of just
//     * a basic Tree.
//     *
//     * @param i
//     *            The index of the child to get
//     */
//    @Override
//    public XQueryTree getChild(int i) {
//        return new XQueryTree(super.getChild(i));
//    }
//
//    public XQueryTree getParentTree() {
//        return context != null ? new XQueryTree(context.getParent()) : new XQueryTree(super.getParent());
//    }
//
//    @Override
//    public ParserRuleContext getParent() {
//        return context != null ? context.getParent() : super.getParent();
//    }
//
//    /**
//     * Change the function so we don't return null, the thought being that in
//     * this case it is better/easier if we have an empty list for the children.
//     */
//    public List<XQueryTree> getChildren() {
//        final List<XQueryTree> children = new ArrayList<>();
//        if (super.children != null) {
//            IntStream.range(0, super.children.size())
//                .forEach(idx ->
//                    children.add(getChild(idx))
//                );
//        }
//        return children;
//    }
//
//    /**
//     * Gets the "text value" of the specified node - the text for each of the
//     * children appended together. Different from the getTextValue() method in
//     * that when finding the node it will not skip levels if it doesn't find a
//     * portion of the name identifier.
//     *
//     * @param name
//     *            The name of the node to find
//     * @return String "text" value for the node
//     */
//    public String getChildTextValue(String name) {
//        XQueryTree node = find(this, name, false);
//        if (node != null) {
//            return node.getTextValue();
//        }
//        return null;
//    }
//
//    /**
//     * Gets the "value" of the specified node - the text for each of the
//     * children appended together. Different from the getValue() method in that
//     * when finding the node it will not skip levels if it doesn't find a
//     * portion of the name identifier.
//     *
//     * @param name
//     *            The name of the node to find
//     * @return String value for the node
//     */
//    public String getChildValue(String name) {
//        XQueryTree node = find(this, name, false);
//        if (node != null) {
//            return node.getValue();
//        }
//        return null;
//    }
//
//    /**
//     * Change the line function so that it will always have a somewhat valid
//     * number by inheriting from the first child that has a line instead of just
//     * checking only the first child.
//     */
//    public int getLine() {
//        if (start == null || start.getLine() == 0) {
//            for (XQueryTree child : getChildren()) {
//                int line = child.getLine();
//                if (line > 0) {
//                    return line;
//                }
//            }
//            return 0;
//        }
//        return start.getLine();
//    }
//
////    public int getStart() {
////        return start;
////    }
////
////    public int getStop() {
////        return stop;
////    }
//
//    public String getName() {
//        return context != null && context.getRuleIndex() >= 0
//            ? XQueryParser.ruleNames[context.getRuleIndex()]
//            : getText();
//    }
//
//    public String getText() {
//        return context != null ? context.getText() : start.getText();
//    }
//
//    /**
//     * Gets the "text value" of the node - the text for each of the children
//     * appended together. All blank spaces are removed, so this is mainly useful
//     * for getting names of variables and functions, not string literals.
//     *
//     * @return String "text" value of the node
//     */
//    public String getTextValue() {
//        String value = getValue();
////        String value = context != null ? context.getText() : "";
//        if (StringUtils.isNotBlank(value)) {
//            value = value.replaceAll(" ", "");
//        }
//        return value;
//    }
//
//    /**
//     * Gets the "text value" of the specified node - the text for each of the
//     * children appended together. All blank spaces are removed, so this is
//     * mainly useful for getting names of variables and functions, not string
//     * literals.
//     *
//     * @param name
//     *            The name of the node to find
//     * @return String "text" value of the node
//     */
//    public String getTextValue(String name) {
//        XQueryTree node = find(this, name, true);
//        if (node != null) {
//            return node.getTextValue();
//        }
//        return null;
//    }
//
//    public CommonToken getToken() {
//        return (CommonToken) start;
//    }
//
//    public int getType() {
//        return context != null ? context.getRuleIndex() : getRuleIndex();
//    }
//
//    /**
//     * Get the value of the type. We need this because types can be either a
//     * KindTest, ItemTest, BinaryTest, or AtomicOrUnionType. Currently doesn't
//     * support multiple types (part of XQuery 3.0).
//     *
//     * @param name
//     *            The name of the type node
//     * @return The value of the type, if any
//     */
//    public String getTypeValue(String name) {
//        XQueryTree node = find(this, name, true);
//        String value = null;
//        if (node != null) {
//            value = node.getTextValue("KindTest");
//            if (StringUtils.isNotBlank(value) && value.contains("QName")) {
//                value = value.replace("QName", node.getTextValue("KindTest.QName"));
//            }
//            if (StringUtils.isBlank(value)) {
//                value = node.getTextValue("QName");
//            }
//            if (StringUtils.isBlank(value)) {
//                value = node.getTextValue("ItemTest");
//            }
//            if (StringUtils.isBlank(value)) {
//                value = node.getTextValue("BinaryTest");
//            }
//        }
//        return value;
//    }
//
//    /**
//     * Gets the "value" of the node - the text for each of the children appended
//     * together.
//     *
//     * @return String value for the node
//     */
//    public String getValue() {
//        StringBuffer value = new StringBuffer();
//        for (XQueryTree child : getChildren()) {
//            String text = (child).getName();
//            if (StringUtils.isNotBlank(text)) {
//                if (value.length() > 0) {
//                    value.append(' ');
//                }
//                value.append(text.trim());
//            }
//        }
//        if (value.toString().length() > 0) {
//            return value.toString();
//        } else if (XQueryParser.RULE_stringLiteral == getType()) {
//            return "";
//        }
//        return null;
//    }
//
//    /**
//     * Gets the "value" of the specified node - the text for each of the
//     * children appended together.
//     *
//     * @param name
//     *            The name of the node to find
//     *
//     * @return String value for the node
//     */
//    public String getValue(String name) {
//        XQueryTree node = find(this, name, true);
//        if (node != null) {
//            return node.getValue();
//        }
//        return null;
//    }
//
//    public boolean isError() {
//        return false;
//    }
//
//    public boolean isStacked() {
//        return stacked;
//    }
//
//    public void setStacked(boolean inStack) {
//        stacked = inStack;
//    }
////
////    public void setStart(int start) {
////        this.start = start;
////    }
////
////    public void setStop(int stop) {
////        this.stop = stop;
////    }
//
//    @Override
//    public String toString() {
//        return super.toString() + " (" + getLine() + ":" + getCharPositionInLine() + ")";
//    }
//
//    @Override
//    public String toStringTree() {
//        List<String> rules = asList(XQueryParser.ruleNames);
//        return context != null ? Trees.toStringTree(context, rules) : super.toStringTree(rules);
//    }
//
//    public void printStringTree() {
//        printStringTree(context, 0);
//    }
//
//    public void printStringTree(RuleContext tree, int indentation) {
//
//        if (tree instanceof ParserRuleContext) {
//            for (int i = 0; i < indentation; i++) System.out.print("  ");
//            System.out.println(XQueryParser.ruleNames[tree.getRuleIndex()]);
//            if (((ParserRuleContext) tree).children != null)
//                ((ParserRuleContext) tree).children.forEach(child -> {
//                        if (child instanceof RuleContext)
//                            printStringTree((RuleContext) child, indentation + 1);
//                        else {
//                            for (int i = 0; i <= indentation; i++) System.out.print("  ");
//                            System.out.println(child.getText());
//                        }
//
//                    }
//                );
//        }
//
//        System.out.println();
//    }
//
//    /**
//     * Prints out the tree nodes in a readable format (for debugging). This will
//     * intent each level of the tree so it is more easily readable as to which
//     * nodes are children of other nodes.
//     */
////    @Override
////    public String toStringTree() {
////        return toStringTree(0);
////    }
////
////    /*
////     * Internal method to indicate the indentation level.
////     */
////    private String toStringTree(int level) {
////        if (children == null || children.size() == 0) {
////            return this.toString();
////        }
////        StringBuffer buf = new StringBuffer();
////        if (!isNil()) {
////            buf.append("(");
////            buf.append(this.toString());
////        }
////        buf.append('\n');
////        for (int i = 0; i <= level; i++) {
////            buf.append("  ");
////        }
////        for (int i = 0; children != null && i < children.size(); i++) {
////            XQueryTree t = (XQueryTree) children.get(i);
////            if (i > 0) {
////                buf.append('\n');
////                for (int j = 0; j <= level; j++) {
////                    buf.append("  ");
////                }
////            }
////            buf.append(t.toStringTree(level + 1));
////        }
////        if (!isNil()) {
////            buf.append('\n');
////            for (int i = 0; i < level; i++) {
////                buf.append("  ");
////            }
////            buf.append(')');
////        }
////        return buf.toString();
////    }
////
////    public String path() {
////        if (parent == null) {
////            return this.toString();
////        } else if (!isNil()) {
////            return ((XQueryTree) parent).path() + "/" + this.toString();
////        } else {
////            return ((XQueryTree) parent).path();
////        }
////    }
//
//}
