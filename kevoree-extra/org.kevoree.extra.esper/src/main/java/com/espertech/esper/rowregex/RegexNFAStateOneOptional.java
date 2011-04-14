package com.espertech.esper.rowregex;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * The '?' state in the regex NFA states.
 */
public class RegexNFAStateOneOptional extends RegexNFAStateBase implements RegexNFAState
{
    private ExprEvaluator exprNode;

    /**
     * Ctor.
     * @param nodeNum node num
     * @param variableName variable name
     * @param streamNum stream number
     * @param multiple true for multiple matches
     * @param isGreedy true for greedy
     * @param exprNode filter expression
     */
    public RegexNFAStateOneOptional(String nodeNum, String variableName, int streamNum, boolean multiple, boolean isGreedy, ExprNode exprNode)
    {
        super(nodeNum, variableName, streamNum, multiple, isGreedy);
        this.exprNode = exprNode.getExprEvaluator();
    }

    public boolean matches(EventBean[] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (exprNode == null)
        {
            return true;
        }

        Boolean result = (Boolean) exprNode.evaluate(eventsPerStream, true, exprEvaluatorContext);
        if (result != null)
        {
            return result;
        }
        return false;
    }

    public String toString()
    {
        if (exprNode == null)
        {
            return "OptionalFilterEvent";
        }
        return "OptionalFilterEvent-Filtered";
    }

}
