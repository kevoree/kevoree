package com.espertech.esper.epl.expression;

/**
 * Visitor for compiling usage informaton of special expressions within an expression tree.
 */
public class ExprNodeSummaryVisitor implements ExprNodeVisitor
{
    private boolean hasProperties;
    private boolean hasAggregation;
    private boolean hasSubselect;
    private boolean hasStreamSelect;
    private boolean hasPreviousPrior;

    public boolean isVisit(ExprNode exprNode)
    {
        return true;
    }

    public void visit(ExprNode exprNode)
    {
        if (exprNode instanceof ExprIdentNode)
        {
            hasProperties = true;
        }
        else if (exprNode instanceof ExprSubselectNode)
        {
            hasSubselect = true;
        }
        else if (exprNode instanceof ExprAggregateNode)
        {
            hasAggregation = true;
        }
        else if ((exprNode instanceof ExprStreamUnderlyingNode) || (exprNode instanceof ExprStreamInstanceMethodNode))
        {
            hasStreamSelect = true;
        }
        else if ((exprNode instanceof ExprPriorNode) || (exprNode instanceof ExprPreviousNode))
        {
            hasPreviousPrior = true;
        }
    }

    /**
     * Returns true if the expression is a plain-value expression, without any of the following:
     * properties, aggregation, subselect, stream select, previous or prior
     * @return true for plain
     */
    public boolean isPlain()
    {
        return !(hasProperties | hasAggregation | hasSubselect | hasStreamSelect | hasPreviousPrior);
    }

    /**
     * Returns a message if the expression contains special-instruction expressions.
     * @return message
     */
    public String getMessage()
    {
        if (hasProperties)
        {
            return "event properties";
        }
        else if (hasAggregation)
        {
            return "aggregation functions";
        }
        else if (hasSubselect)
        {
            return "sub-selects";
        }
        else if (hasStreamSelect)
        {
            return "stream selects or event instance methods";
        }
        else if (hasPreviousPrior)
        {
            return "previous or prior functions";
        }
        return null;
    }
}
