package com.espertech.esper.epl.parse;

import org.antlr.runtime.tree.Tree;
import com.espertech.esper.epl.spec.MatchRecognizeSkipEnum;

/**
 * Helper class for walking the match-recognize AST.
 */
public class ASTMatchRecognizeHelper {

    private final static String message = "Match-recognize AFTER clause must be either AFTER MATCH SKIP TO LAST ROW or AFTER MATCH SKIP TO NEXT ROW or AFTER MATCH SKIP TO CURRENT ROW";

    /**
     * Parse the skip clause.
     * @param node parent AST node
     * @return skip node enum
     */
    public static MatchRecognizeSkipEnum parseSkip(Tree node) {

        if (node.getChildCount() != 5)
        {
            throw new IllegalArgumentException(message);
        }

        if ((!node.getChild(0).getText().toUpperCase().equals("MATCH")) ||
            (!node.getChild(1).getText().toUpperCase().equals("SKIP")) ||
            (!node.getChild(4).getText().toUpperCase().equals("ROW"))
            )
        {
            throw new IllegalArgumentException(message);
        }

        if ((!node.getChild(2).getText().toUpperCase().equals("TO")) &&
            (!node.getChild(2).getText().toUpperCase().equals("PAST"))
            )
        {
            throw new IllegalArgumentException(message);
        }

        if (node.getChild(3).getText().toUpperCase().equals("LAST"))
        {
            return MatchRecognizeSkipEnum.PAST_LAST_ROW;
        }
        else if (node.getChild(3).getText().toUpperCase().equals("NEXT"))
        {
            return MatchRecognizeSkipEnum.TO_NEXT_ROW;
        }
        else if (node.getChild(3).getText().toUpperCase().equals("CURRENT"))
        {
            return MatchRecognizeSkipEnum.TO_CURRENT_ROW;
        }
        throw new IllegalArgumentException(message);
    }
}
