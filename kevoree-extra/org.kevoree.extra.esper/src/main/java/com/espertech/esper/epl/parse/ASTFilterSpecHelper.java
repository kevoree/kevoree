/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.parse;

import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import org.antlr.runtime.tree.Tree;

/**
 * Builds a filter specification from filter AST nodes.
 */
public class ASTFilterSpecHelper
{
    /**
     * Return the generated property name that is defined by the AST child node and it's siblings.
     * @param parentNode the AST node to consider as the parent for the child nodes to look at
     * @param startIndex the index of the child node to start looking at
     * @return property name, ie. indexed[1] or mapped('key') or nested.nested or a combination or just 'simple'.
     */
    protected static String getPropertyName(Tree parentNode, int startIndex)
    {
        StringBuilder buffer = new StringBuilder();
        String delimiter = "";

        int childIndex = startIndex;
        while (childIndex < parentNode.getChildCount())
        {
        	Tree child = parentNode.getChild(childIndex);
            buffer.append(delimiter);

            switch (child.getType()) {
                case EsperEPL2GrammarParser.EVENT_PROP_SIMPLE:
                    buffer.append(escapeDot(child.getChild(0).getText()));
                    break;
                case EsperEPL2GrammarParser.EVENT_PROP_MAPPED:
                    buffer.append(escapeDot(child.getChild(0).getText()));
                    buffer.append('(');
                    buffer.append(child.getChild(1).getText());
                    buffer.append(')');
                    break;
                case EsperEPL2GrammarParser.EVENT_PROP_INDEXED:
                    buffer.append(escapeDot(child.getChild(0).getText()));
                    buffer.append('[');
                    buffer.append(child.getChild(1).getText());
                    buffer.append(']');
                    break;
                case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_SIMPLE:
                    buffer.append(escapeDot(child.getChild(0).getText()));
                    buffer.append('?');
                    break;
                case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_MAPPED:
                    buffer.append(escapeDot(child.getChild(0).getText()));
                    buffer.append('(');
                    buffer.append(child.getChild(1).getText());
                    buffer.append(')');
                    buffer.append('?');
                    break;
                case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_INDEXED:
                    buffer.append(escapeDot(child.getChild(0).getText()));
                    buffer.append('[');
                    buffer.append(child.getChild(1).getText());
                    buffer.append(']');
                    buffer.append('?');
                    break;
                default:
                    throw new IllegalStateException("Event property AST node not recognized, type=" + child.getType());
            }

            delimiter = ".";
            childIndex++;
        }

        return buffer.toString();
    }

    /**
     * Escape all unescape dot characters in the text (identifier only) passed in.
     * @param identifierToEscape text to escape
     * @return text where dots are escaped
     */
    protected static String escapeDot(String identifierToEscape)
    {
        int indexof = identifierToEscape.indexOf(".");
        if (indexof == -1)
        {
            return identifierToEscape;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < identifierToEscape.length(); i++)
        {
            char c = identifierToEscape.charAt(i);
            if (c != '.')
            {
                builder.append(c);
                continue;
            }

            if (i > 0)
            {
                if (identifierToEscape.charAt(i - 1) == '\\')
                {
                    builder.append('.');
                    continue;
                }
            }

            builder.append('\\');
            builder.append('.');
        }

        return builder.toString();
    }

    /**
     * Find the index of an unescaped dot (.) character, or return -1 if none found.
     * @param identifier text to find an un-escaped dot character
     * @return index of first unescaped dot
     */
    public static int unescapedIndexOfDot(String identifier)
    {
        int indexof = identifier.indexOf(".");
        if (indexof == -1)
        {
            return -1;
        }

        for (int i = 0; i < identifier.length(); i++)
        {
            char c = identifier.charAt(i);
            if (c != '.')
            {
                continue;
            }

            if (i > 0)
            {
                if (identifier.charAt(i - 1) == '\\')
                {
                    continue;
                }
            }

            return i;
        }

        return -1;
    }

    /**
     * Un-Escape all escaped dot characters in the text (identifier only) passed in.
     * @param identifierToUnescape text to un-escape
     * @return string
     */
    public static String unescapeDot(String identifierToUnescape)
    {
        int indexof = identifierToUnescape.indexOf(".");
        if (indexof == -1)
        {
            return identifierToUnescape;
        }
        indexof = identifierToUnescape.indexOf("\\");
        if (indexof == -1)
        {
            return identifierToUnescape;
        }

        StringBuilder builder = new StringBuilder();
        int index = -1;
        int max = identifierToUnescape.length() - 1;
        do
        {
            index++;
            char c = identifierToUnescape.charAt(index);
            if (c != '\\') {
                builder.append(c);
                continue;
            }
            if (index < identifierToUnescape.length() - 1)
            {
                if (identifierToUnescape.charAt(index + 1) == '.')
                {
                    builder.append('.');
                    index++;
                }
            }
        }
        while (index < max);

        return builder.toString();
    }
}
