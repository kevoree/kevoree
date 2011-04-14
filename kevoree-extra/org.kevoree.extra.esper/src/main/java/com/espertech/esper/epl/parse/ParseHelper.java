/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.parse;

import com.espertech.esper.antlr.ASTUtil;
import com.espertech.esper.antlr.NoCaseSensitiveStream;
import com.espertech.esper.client.EPException;
import com.espertech.esper.epl.generated.EsperEPL2GrammarLexer;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import com.espertech.esper.epl.generated.EsperEPL2Ast;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Helper class for parsing an expression and walking a parse tree.
 */
public class ParseHelper
{
    /**
     * Newline.
     */
    public final static String newline = System.getProperty("line.separator");

    /**
     * Walk parse tree starting at the rule the walkRuleSelector supplies.
     * @param ast - ast to walk
     * @param walker - walker instance
     * @param walkRuleSelector - walk rule
     * @param expression - the expression we are walking in string form
     * @param eplStatementForErrorMsg - statement text for error messages
     */
    public static void walk(Tree ast, EPLTreeWalker walker, WalkRuleSelector walkRuleSelector, String expression, String eplStatementForErrorMsg)
    {
        // Walk tree
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug(".walk Walking AST using walker " + walker.getClass().getName());
            }

            walkRuleSelector.invokeWalkRule(walker);

            if (log.isDebugEnabled())
            {
                log.debug(".walk AST tree after walking");
                ASTUtil.dumpAST(ast);
            }
        }
        catch (RuntimeException e)
        {
            log.info("Error walking statement [" + expression + "]", e);
            if (e.getCause() instanceof RecognitionException)
            {
                throw ExceptionConvertor.convert((RecognitionException)e.getCause(), eplStatementForErrorMsg, walker);
            }
            else
            {
                throw e;
            }
        }
        catch (RecognitionException e)
        {
            log.info("Error walking statement [" + expression + "]", e);
            throw ExceptionConvertor.convert(e, eplStatementForErrorMsg, walker);
        }
    }

    /**
     * Parse expression using the rule the ParseRuleSelector instance supplies.
     * @param expression - text to parse
     * @param parseRuleSelector - parse rule to select
     * @param addPleaseCheck - true to include depth paraphrase
     * @param eplStatementErrorMsg - text for error
     * @return AST - syntax tree
     * @throws EPException when the AST could not be parsed
     */
    public static ParseResult parse(String expression, String eplStatementErrorMsg, boolean addPleaseCheck, ParseRuleSelector parseRuleSelector) throws EPException
    {
        if (log.isDebugEnabled())
        {
            log.debug(".parse Parsing expr=" + expression);
        }

        CharStream input;
        try
        {
            input = new NoCaseSensitiveStream(new StringReader(expression));
        }
        catch (IOException ex)
        {
            throw new EPException("IOException parsing expression '" + expression + '\'', ex);
        }

        EsperEPL2GrammarLexer lex = new EsperEPL2GrammarLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        EsperEPL2GrammarParser parser = new EsperEPL2GrammarParser(tokens);

        Tree tree;
        try
        {
            tree = parseRuleSelector.invokeParseRule(parser);
        }
        catch (RuntimeException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error parsing statement [" + eplStatementErrorMsg + "]", e);
            }
            if (e.getCause() instanceof RecognitionException)
            {
                throw ExceptionConvertor.convertStatement((RecognitionException)e.getCause(), eplStatementErrorMsg, addPleaseCheck, parser);
            }
            else
            {
                throw e;
            }
        }
        catch (RecognitionException ex)
        {
            log.debug("Error parsing statement [" + expression + "]", ex);
            throw ExceptionConvertor.convertStatement(ex, eplStatementErrorMsg, addPleaseCheck, parser);
        }

        if (log.isDebugEnabled())
        {
            log.debug(".parse Dumping AST...");
            ASTUtil.dumpAST(tree);
        }

        return new ParseResult(tree, getNoAnnotation(expression, tree, tokens));
    }

    private static String getNoAnnotation(String expression, Tree tree, CommonTokenStream tokens)
    {
        Token lastAnnotationToken = null;
        for (int i = 0; i < tree.getChildCount(); i++)
        {
            if (tree.getChild(i).getType() == EsperEPL2Ast.ANNOTATION)
            {
                lastAnnotationToken = tokens.get(tree.getChild(i).getTokenStopIndex());
            }
            else
            {
                break;
            }
        }

        if (lastAnnotationToken == null)
        {
            return null;
        }

        try
        {
            int line = lastAnnotationToken.getLine();
            int charpos = lastAnnotationToken.getCharPositionInLine();
            int fromChar = charpos + lastAnnotationToken.getText().length();
            if (line == 1)
            {
                return expression.substring(fromChar).trim();
            }

            String[] lines = expression.split("\r\n|\r|\n");
            StringBuilder buf = new StringBuilder();
            buf.append(lines[line-1].substring(fromChar));
            for (int i = line; i < lines.length; i++)
            {
                buf.append(lines[i]);
                if (i < lines.length - 1)
                {
                    buf.append(newline);
                }
            }
            return buf.toString().trim();
        }
        catch (RuntimeException ex)
        {
            log.error("Error determining non-annotated expression sting: " + ex.getMessage(), ex);
        }
        return null;
    }

    private static Log log = LogFactory.getLog(ParseHelper.class);
}
