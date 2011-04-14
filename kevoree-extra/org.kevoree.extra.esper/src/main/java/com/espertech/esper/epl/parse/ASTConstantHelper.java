/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.parse;

import com.espertech.esper.type.*;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import org.antlr.runtime.tree.Tree;

/**
 * Parses constant strings and returns the constant Object.
 */
public class ASTConstantHelper
{
    /**
     * Remove tick '`' character from a string start and end.
     * @param tickedString delimited string
     * @return delimited string with ticks removed, if starting and ending with tick
     */
    public static String removeTicks(String tickedString)
    {
        int indexFirst = tickedString.indexOf('`');
        int indexLast = tickedString.lastIndexOf('`');
        if ((indexFirst != indexLast) && (indexFirst != -1) && (indexLast != -1))
        {
            return tickedString.substring(indexFirst+1, indexLast);
        }
        return tickedString;
    }

    /**
     * Parse the AST constant node and return Object value.
     * @param node - parse node for which to parse the string value
     * @return value matching AST node type
     */
    public static Object parse(Tree node)
    {
        switch(node.getType())
        {
            case EsperEPL2GrammarParser.NUM_INT:       return parseIntLongByte(node.getText());
            case EsperEPL2GrammarParser.INT_TYPE:      return parseIntLongByte(node.getText());
            case EsperEPL2GrammarParser.LONG_TYPE:     return LongValue.parseString(node.getText());
            case EsperEPL2GrammarParser.BOOL_TYPE:     return BoolValue.parseString(node.getText());
            case EsperEPL2GrammarParser.FLOAT_TYPE:    return FloatValue.parseString(node.getText());
            case EsperEPL2GrammarParser.DOUBLE_TYPE:   return DoubleValue.parseString(node.getText());
            case EsperEPL2GrammarParser.STRING_TYPE:   return StringValue.parseString(node.getText());
            case EsperEPL2GrammarParser.NULL_TYPE:     return null;
            default:
                throw new IllegalArgumentException("Unexpected constant of non-primitive type " + node.getType() + " encountered");
        }
    }

    private static Object parseIntLongByte(String arg)
    {
        // try to parse as an int first, else try to parse as a long
        try
        {
            return IntValue.parseString(arg);
        }
        catch (NumberFormatException e1)
        {
            try
            {
                return LongValue.parseString(arg);
            }
            catch (Exception e2)
            {
                try
                {
                    return Byte.decode(arg);
                }
                catch (Exception e3)
                {
                    throw e1;
                }
            }
        }

    }
}
