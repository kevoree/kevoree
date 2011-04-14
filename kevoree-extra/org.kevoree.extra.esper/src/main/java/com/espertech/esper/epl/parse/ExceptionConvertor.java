package com.espertech.esper.epl.parse;

import com.espertech.esper.client.EPStatementSyntaxException;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.generated.EsperEPL2Ast;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import org.antlr.runtime.*;

import java.util.Set;
import java.util.Stack;

/**
 * Converts recognition exceptions.
 */
public class ExceptionConvertor
{
    /**
     * Converts from a syntax error to a nice statement exception.
     * @param e is the syntax error
     * @param expression is the expression text
     * @param parser the parser that parsed the expression
     * @param addPleaseCheck indicates to add "please check" paraphrases
     * @return syntax exception
     */
    public static EPStatementSyntaxException convertStatement(RecognitionException e, String expression, boolean addPleaseCheck, EsperEPL2GrammarParser parser)
    {
        UniformPair<String> pair = convert(e, expression, addPleaseCheck, parser);
        return new EPStatementSyntaxException(pair.getFirst(), pair.getSecond());
    }

    /**
     * Converts from a syntax error to a nice property exception.
     * @param e is the syntax error
     * @param expression is the expression text
     * @param parser the parser that parsed the expression
     * @param addPleaseCheck indicates to add "please check" paraphrases
     * @return syntax exception
     */
    public static PropertyAccessException convertProperty(RecognitionException e, String expression, boolean addPleaseCheck, EsperEPL2GrammarParser parser)
    {
        UniformPair<String> pair = convert(e, expression, addPleaseCheck, parser);
        return new PropertyAccessException(pair.getFirst(), pair.getSecond());
    }

    /**
     * Converts from a syntax error to a nice exception.
     * @param e is the syntax error
     * @param expression is the expression text
     * @param parser the parser that parsed the expression
     * @param addPleaseCheck indicates to add "please check" paraphrases
     * @return syntax exception
     */
    public static UniformPair<String> convert(RecognitionException e, String expression, boolean addPleaseCheck, EsperEPL2GrammarParser parser)
    {
        if (expression.trim().length() == 0)
        {
            String message = "Unexpected end of input";
            return new UniformPair<String>(message, expression);
        }

        Token t;
        Token tBefore = null;
        Token tAfter = null;
        if (e.index < parser.getTokenStream().size())
        {
            t = parser.getTokenStream().get(e.index);
            if ((e.index + 1) < parser.getTokenStream().size())
            {
                tAfter = parser.getTokenStream().get(e.index + 1);
            }
            if (e.index - 1 >= 0)
            {
                tBefore = parser.getTokenStream().get(e.index - 1);
            }
        }
        else
        {
            if (parser.getTokenStream().size() >= 2) {
                tBefore = parser.getTokenStream().get(parser.getTokenStream().size() - 2);
            }
            t = parser.getTokenStream().get(parser.getTokenStream().size() - 1);
        }
        String positionInfo = getPositionInfo(t);
        String token = "'" + t.getText() + "'";

        Stack stack = parser.getParaphrases();
        String check = "";
        if ((stack.size() > 0) && addPleaseCheck)
        {
            String delimiter = "";
            StringBuilder checkList = new StringBuilder();
            checkList.append(", please check the ");
            while(stack.size() != 0)
            {
                checkList.append(delimiter);
                checkList.append(stack.pop());
                delimiter = " within the ";
            }
            check = checkList.toString();
        }

        // check if token is a reserved keyword
        Set<String> keywords = parser.getKeywords();
        if (keywords.contains(token.toLowerCase()))
        {
            token += " (a reserved keyword)";
        }
        else
        {
            if ((tBefore != null) &&
                (tAfter != null) &&
                (keywords.contains("'" + tBefore.getText().toLowerCase() + "'")) &&
                (keywords.contains("'" + tAfter.getText().toLowerCase() + "'")))
            {
                token += " ('" + tBefore.getText() + "' and '" + tAfter.getText() + "' are a reserved keyword)";
            }
            else if ((tBefore != null) &&
                     (keywords.contains("'" + tBefore.getText().toLowerCase() + "'")))
            {
                token += " ('" + tBefore.getText() + "' is a reserved keyword)";
            }
        }

        String message = "Incorrect syntax near " + token + positionInfo + check;
        if (e instanceof NoViableAltException)
        {
            NoViableAltException nvae = (NoViableAltException) e;
            if (nvae.token.getType() == -1)
            {
                message = "Unexpected end of input near " + token + positionInfo + check;
            }
            else
            {
                if (parser.getParserTokenParaphrases().get(nvae.token.getType()) != null)
                {
                    message = "Incorrect syntax near " + token + positionInfo + check;
                }
                else
                {
                    // find next keyword in the next 3 tokens
                    int currentIndex = e.index + 1;
                    while ((currentIndex > 0) &&
                           (currentIndex < parser.getTokenStream().size() - 1) &&
                           (currentIndex < e.index + 3))
                    {
                        Token next = parser.getTokenStream().get(currentIndex);
                        currentIndex++;

                        String quotedToken = "'" + next.getText() + "'";
                        if (parser.getKeywords().contains(quotedToken))
                        {
                            check += " near reserved keyword '" + next.getText() + "'";
                            break;
                        }
                    }
                    message = "Incorrect syntax near " + token + positionInfo + check;
                }
            }
        }

        if (e instanceof MismatchedTokenException)
        {
            MismatchedTokenException mismatched = (MismatchedTokenException) e;

            String expected = "end of input";
            if ((mismatched.expecting >= 0) && (mismatched.expecting < parser.getTokenNames().length))
            {
                expected = parser.getTokenNames()[mismatched.expecting];
            }
            if (parser.getLexerTokenParaphrases().get(mismatched.expecting) != null)
            {
                expected = parser.getLexerTokenParaphrases().get(mismatched.expecting);
            }
            if (parser.getParserTokenParaphrases().get(mismatched.expecting) != null)
            {
                expected = parser.getParserTokenParaphrases().get(mismatched.expecting);
            }

            String unexpected;
            if ((mismatched.getUnexpectedType() < 0) || (mismatched.getUnexpectedType() >= parser.getTokenNames().length))
            {
                unexpected = "end of input";
            }
            else
            {
                unexpected = parser.getTokenNames()[mismatched.getUnexpectedType()];
            }
            if (parser.getLexerTokenParaphrases().get(mismatched.getUnexpectedType()) != null)
            {
                unexpected = parser.getLexerTokenParaphrases().get(mismatched.getUnexpectedType());
            }
            if (parser.getParserTokenParaphrases().get(mismatched.getUnexpectedType()) != null)
            {
                unexpected = parser.getParserTokenParaphrases().get(mismatched.getUnexpectedType());
            }

            String expecting = " expecting " + expected.trim() + " but found " + unexpected.trim();
            message = "Incorrect syntax near " + token + expecting + positionInfo + check;
        }

        if (e instanceof EarlyExitException)
        {
            EarlyExitException ee = (EarlyExitException) e;
            char c = (char) ee.c;
            if (c == 65535) {
                message = "Unexpected end of input string, check for an invalid identifier or missing additional keywords near " + token + positionInfo + " ";
            }
            else {
                message = "Incorrect syntax near " + token + positionInfo + " unexpected character '" + c + "', check for an invalid identifier or missing additional keywords";
            }
        }

        return new UniformPair<String>(message, expression);
    }

    /**
     * Converts from a syntax error to a nice statement exception.
     * @param e is the syntax error
     * @param expression is the expression text
     * @param treeWalker the tree walker that walked the tree
     * @return syntax exception
     */
    public static EPStatementSyntaxException convert(RecognitionException e, String expression, EsperEPL2Ast treeWalker)
    {
        String positionInfo = getPositionInfo(e.token);
        String tokenName = "end of input";
        if ((e.token != null) && (e.token.getType() >= 0) && (e.token.getType() < treeWalker.getTokenNames().length))
        {
            tokenName = treeWalker.getTokenNames()[e.token.getType()];
        }

        String message = "Unexpected error processing statement near token " + tokenName + positionInfo;

        if (e instanceof MismatchedTokenException)
        {
            MismatchedTokenException mismatched = (MismatchedTokenException) e;

            String expected = "end of input";
            if ((mismatched.expecting >= 0) && (mismatched.expecting < treeWalker.getTokenNames().length))
            {
                expected = treeWalker.getTokenNames()[mismatched.expecting];
            }

            String unexpected;
            if ((mismatched.getUnexpectedType() < 0) || (mismatched.getUnexpectedType() >= treeWalker.getTokenNames().length))
            {
                unexpected = "end of input";
            }
            else
            {
                unexpected = treeWalker.getTokenNames()[mismatched.getUnexpectedType()];
            }

            String expecting = " expecting " + expected.trim() + " but found " + unexpected.trim();
            message = "Unexpected error processing statement near token " + tokenName + expecting + positionInfo;
        }

        return new EPStatementSyntaxException(message, expression);
    }

    /**
     * Returns the position information string for a parser exception.
     * @param t the token to return the information for
     * @return is a string with line and column information
     */
    private static String getPositionInfo(Token t)
    {
        return t.getLine() > 0 && t.getCharPositionInLine() > 0
                ? " at line " + t.getLine() + " column " + t.getCharPositionInLine()
                : "";
    }
}
