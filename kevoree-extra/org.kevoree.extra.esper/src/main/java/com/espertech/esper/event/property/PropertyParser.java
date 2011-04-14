/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.property;

import com.espertech.esper.antlr.ASTUtil;
import com.espertech.esper.antlr.NoCaseSensitiveStream;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.epl.generated.EsperEPL2GrammarLexer;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import com.espertech.esper.epl.parse.ExceptionConvertor;
import com.espertech.esper.type.IntValue;
import com.espertech.esper.type.StringValue;
import com.espertech.esper.util.ExecutionPathDebugLog;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Parser for property names that can be simple, nested, mapped or a combination of these.
 * Uses ANTLR parser to parse.
 */
public class PropertyParser
{
    private static final Log log = LogFactory.getLog(PropertyParser.class);

    private static Set<String> keywordCache;

    /**
     * Parse the given property name returning a Property instance for the property.
     * @param propertyName is the property name to parse
     * @param isRootedDynamic is true to indicate that the property is already rooted in a dynamic
     * property and therefore all child properties should be dynamic properties as well
     * @return Property instance for property
     */
    public static Property parse(String propertyName, boolean isRootedDynamic)
    {
        Tree tree = parse(propertyName);

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            ASTUtil.dumpAST(tree);
        }

        if (tree.getChildCount() == 1)
        {
            return makeProperty(tree.getChild(0), isRootedDynamic);
        }

        List<Property> properties = new LinkedList<Property>();
        boolean isRootedInDynamic = isRootedDynamic;
        for (int i = 0; i < tree.getChildCount(); i++)
        {
        	Tree child = tree.getChild(i);

            Property property = makeProperty(child, isRootedInDynamic);
            if (property instanceof DynamicSimpleProperty)
            {
                isRootedInDynamic = true;
            }
            properties.add(property);
        }

        return new NestedProperty(properties);
    }

    /**
     * Parses a given property name returning an AST.
     * @param propertyName to parse
     * @return AST syntax tree
     */
    public static Tree parse(String propertyName)
    {
        CharStream input;
        try
        {
            input = new NoCaseSensitiveStream(new StringReader(propertyName));
        }
        catch (IOException ex)
        {
            throw new PropertyAccessException("IOException parsing property name '" + propertyName + '\'', ex);
        }

        EsperEPL2GrammarLexer lex = new EsperEPL2GrammarLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        EsperEPL2GrammarParser g = new EsperEPL2GrammarParser(tokens);
        EsperEPL2GrammarParser.startEventPropertyRule_return r;

        try
        {
             r = g.startEventPropertyRule();
        }
        catch (RuntimeException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error parsing property expression [" + propertyName + "]", e);
            }
            if (e.getCause() instanceof RecognitionException)
            {
                throw ExceptionConvertor.convertProperty((RecognitionException)e.getCause(), propertyName, true, g);
            }
            else
            {
                throw e;
            }
        }
        catch (RecognitionException e)
        {
            // Check for keywords and escape each, parse again
            String escapedPropertyName = escapeKeywords(tokens);

            CharStream inputEscaped;
            try
            {
                inputEscaped = new NoCaseSensitiveStream(new StringReader(escapedPropertyName));
            }
            catch (IOException ex)
            {
                throw new PropertyAccessException("IOException parsing property name '" + propertyName + '\'', ex);
            }

            EsperEPL2GrammarLexer lexEscaped = new EsperEPL2GrammarLexer(inputEscaped);
            CommonTokenStream tokensEscaped = new CommonTokenStream(lexEscaped);
            EsperEPL2GrammarParser gEscaped = new EsperEPL2GrammarParser(tokensEscaped);
            EsperEPL2GrammarParser.startEventPropertyRule_return rEscaped;

            try
            {
                rEscaped = gEscaped.startEventPropertyRule();
                return (Tree) rEscaped.getTree(); 
            }
            catch (Exception eEscaped)
            {
            }

            throw ExceptionConvertor.convertProperty(e, propertyName, true, g);
        }

        return (Tree) r.getTree();
    }

    private synchronized static String escapeKeywords(CommonTokenStream tokens) {

        if (keywordCache == null) {
            keywordCache = new HashSet<String>();
            Set<String> keywords = new EsperEPL2GrammarParser(tokens).getKeywords();
            for (String keyword : keywords) {
                if (keyword.charAt(0) == '\'' && keyword.charAt(keyword.length() - 1) == '\'') {
                    keywordCache.add(keyword.substring(1, keyword.length() - 1));
                }
            }
        }

        StringWriter writer = new StringWriter();
        for (Object token : tokens.getTokens()) // Call getTokens first before invoking tokens.size! ANTLR problem
        {
            Token t = (Token) token;
            boolean isKeyword = keywordCache.contains(t.getText().toLowerCase());
            if (isKeyword) {
                writer.append('`');
                writer.append(t.getText());
                writer.append('`');
            }
            else {
                writer.append(t.getText());
            }
        }
        return writer.toString();
    }

    /**
     * Returns true if the property is a dynamic property.
     * @param ast property ast
     * @return dynamic or not
     */
    public static boolean isPropertyDynamic(Tree ast)
    {
        for (int i = 0; i < ast.getChildCount(); i++)
        {
            int type = ast.getChild(i).getType();
            if ((type == EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_SIMPLE) ||
                (type == EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_INDEXED) ||
                (type == EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_MAPPED))
            {
                return true;
            }
        }
        return false;
    }

    private static Property makeProperty(Tree child, boolean isRootedInDynamic)
    {
        switch (child.getType()) {
            case EsperEPL2GrammarParser.EVENT_PROP_SIMPLE:
                if (!isRootedInDynamic)
                {
                    return new SimpleProperty(child.getChild(0).getText());
                }
                else
                {
                    return new DynamicSimpleProperty(child.getChild(0).getText());
                }
            case EsperEPL2GrammarParser.EVENT_PROP_MAPPED:
                String key = StringValue.parseString(child.getChild(1).getText());
                if (!isRootedInDynamic)
                {
                    return new MappedProperty(child.getChild(0).getText(), key);
                }
                else
                {
                    return new DynamicMappedProperty(child.getChild(0).getText(), key);
                }
            case EsperEPL2GrammarParser.EVENT_PROP_INDEXED:
                int index = IntValue.parseString(child.getChild(1).getText());
                if (!isRootedInDynamic)
                {
                    return new IndexedProperty(child.getChild(0).getText(), index);
                }
                else
                {
                    return new DynamicIndexedProperty(child.getChild(0).getText(), index);
                }
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_SIMPLE:
                return new DynamicSimpleProperty(child.getChild(0).getText());
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_INDEXED:
                index = IntValue.parseString(child.getChild(1).getText());
                return new DynamicIndexedProperty(child.getChild(0).getText(), index);
            case EsperEPL2GrammarParser.EVENT_PROP_DYNAMIC_MAPPED:
                key = StringValue.parseString(child.getChild(1).getText());
                return new DynamicMappedProperty(child.getChild(0).getText(), key);
            default:
                throw new IllegalStateException("Event property AST node not recognized, type=" + child.getType());
        }
    }
}
