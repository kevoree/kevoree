package com.espertech.esper.epl.expression;

import com.espertech.esper.collection.Pair;

import java.util.List;
import java.util.ArrayList;

/**
 * Visitor for getting a list of "prev" functions.
 */
public class ExprNodePreviousVisitorWParent implements ExprNodeVisitorWithParent
{
    private List<Pair<ExprNode, ExprPreviousNode>> previous;

    public boolean isVisit(ExprNode exprNode)
    {
        return true;
    }

    public void visit(ExprNode exprNode, ExprNode parentExprNode)
    {
        if (exprNode instanceof ExprPreviousNode)
        {
            if (previous == null)
            {
                previous = new ArrayList<Pair<ExprNode, ExprPreviousNode>>();
            }
            previous.add(new Pair<ExprNode, ExprPreviousNode>(parentExprNode, (ExprPreviousNode) exprNode));
        }
    }

    /**
     * Returns the pair of previous nodes and their parent expression.
     * @return nodes
     */
    public List<Pair<ExprNode, ExprPreviousNode>> getPrevious() {
        return previous;
    }
}
