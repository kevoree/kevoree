/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.espertech.esper.epl.expression.ExprNode;

/**
 * Utility class for AST node handling.
 */
public class ASTUtil
{
    private static Log log = LogFactory.getLog(ASTUtil.class);

    private final static String PROPERTY_ENABLED_AST_DUMP = "ENABLE_AST_DUMP";

    /**
     * Returns the first child node (shallow search) of the given parent that matches type, or null if no child node
     * matches type.
     * @param parent whose child nodes to ask for type
     * @param type the type looked for
     * @return child node if found, or null if not found
     */
    public static Tree findFirstNode(Tree parent, int type)
    {
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            Tree child = parent.getChild(i);
            if (child.getType() == type)
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Dump the AST node to system.out.
     * @param ast to dump
     */
    public static void dumpAST(Tree ast)
    {
        if (System.getProperty(PROPERTY_ENABLED_AST_DUMP) != null)
        {
            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);

            renderNode(new char[0], ast, printer);
            dumpAST(printer, ast, 2);

            log.info(".dumpAST ANTLR Tree dump follows...\n" + writer.toString());
        }
    }

    private static void dumpAST(PrintWriter printer, Tree ast, int ident)
    {
        char[] identChars = new char[ident];
        Arrays.fill(identChars, ' ');

        if (ast == null)
        {
            renderNode(identChars, null, printer);
            return;
        }
        for (int i = 0; i < ast.getChildCount(); i++)
        {
            Tree node = ast.getChild(i);
            if (node == null)
            {
                throw new NullPointerException("Null AST node");
            }
            renderNode(identChars, node, printer);
            dumpAST(printer, node, ident + 2);
        }
    }

    /**
     * Print the token stream to the logger.
     * @param tokens to print
     */
    public static void printTokens(CommonTokenStream tokens)
    {
        if (log.isDebugEnabled())
        {
            List tokenList = tokens.getTokens();

            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);
            for (int i = 0; i < tokens.size(); i++)
            {
                Token t = (Token) tokenList.get(i);
                String text = t.getText();
                if (text.trim().length() == 0)
                {
                    printer.print("'" + text + "'");                    
                }
                else
                {
                    printer.print(text);
                }
                printer.print('[');
                printer.print(t.getType());
                printer.print(']');
                printer.print(" ");
            }
            printer.println();
            log.debug("Tokens: " + writer.toString());
        }
    }

    private static void renderNode(char[] ident, Tree node, PrintWriter printer)
    {
        printer.print(ident);
        if (node == null)
        {
            printer.print("NULL NODE");
        }
        else
        {
            printer.print(node.getText());
            printer.print(" [");
            printer.print(node.getType());
            printer.print("]");

            if (node.getText() == null)
            {
                printer.print(" (null value in text)");
            }
            else if (node.getText().contains("\\"))
            {
                int count = 0;
                for (int i = 0; i < node.getText().length(); i++)
                {
                    if (node.getText().charAt(i) == '\\')
                    {
                        count++;
                    }
                }
                printer.print(" (" + count + " backlashes)");
            }
        }
        printer.println();
    }

    /**
     * For the given child, return the expression for that child and remove from node-to-expression map
     * @param child to ask for expression
     * @param astExprNodeMap map to remove node from
     * @return expression
     */
    public static ExprNode getRemoveExpr(Tree child, Map<Tree, ExprNode> astExprNodeMap)
    {
        ExprNode thisEvalNode = astExprNodeMap.get(child);
        astExprNodeMap.remove(child);
        return thisEvalNode;
    }
}
