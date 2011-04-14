package com.espertech.esper.epl.parse;

import org.antlr.runtime.tree.Tree;

/**
 * Result of a parse action.
 */
public class ParseResult
{
    private Tree tree;
    private String expressionWithoutAnnotations;

    /**
     * Ctor.
     * @param tree parse tree
     * @param expressionWithoutAnnotations expression text no annotations, or null if same
     */
    public ParseResult(Tree tree, String expressionWithoutAnnotations)
    {
        this.tree = tree;
        this.expressionWithoutAnnotations = expressionWithoutAnnotations;
    }

    /**
     * AST.
     * @return ast
     */
    public Tree getTree()
    {
        return tree;
    }

    /**
     * Returns the expression text no annotations.
     * @return expression text no annotations.
     */
    public String getExpressionWithoutAnnotations()
    {
        return expressionWithoutAnnotations;
    }
}
