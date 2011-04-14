package com.espertech.esper.rowregex;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * NFA state for a single match that applies a filter.
 */
public class RegexNFAStateFilter extends RegexNFAStateBase implements RegexNFAState
{
    private ExprEvaluator exprNode;

    /**
     * Ctor.
     * @param nodeNum node num
     * @param variableName variable name
     * @param streamNum stream number
     * @param multiple true for multiple matches
     * @param exprNode filter expression
     */
    public RegexNFAStateFilter(String nodeNum, String variableName, int streamNum, boolean multiple, ExprNode exprNode)
    {
        super(nodeNum, variableName, streamNum, multiple, null);
        this.exprNode = exprNode.getExprEvaluator();
    }

    public boolean matches(EventBean[] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        Boolean result = (Boolean) exprNode.evaluate(eventsPerStream, true, exprEvaluatorContext);
        if (result != null)
        {
            return result;
        }
        return false;
    }

    public String toString()
    {
        return "FilterEvent";
    }

}
