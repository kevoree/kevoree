package com.espertech.esper.pattern.guard;

import com.espertech.esper.client.EPException;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.pattern.*;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;
import java.util.List;

public class TimerWithinOrMaxCountGuardFactory implements GuardFactory, MetaDefItem, Serializable
{
    private static final long serialVersionUID = 6650243610865501435L;
    
    /**
     * Number of milliseconds.
     */
    protected ExprNode millisecondsExpr;

    /**
     * Number of count-to max.
     */
    protected ExprNode numCountToExpr;

    /**
     * For converting matched-events maps to events-per-stream.
     */
    protected transient MatchedEventConvertor convertor;

    public void setGuardParameters(List<ExprNode> params, MatchedEventConvertor convertor) throws GuardParameterException
    {
        String message = "Timer-within-or-max-count guard requires two parameters: "
                    + "numeric or time period parameter and an integer-value expression parameter";

        if (params.size() != 2) {
            throw new GuardParameterException(message);
        }

        if (!JavaClassHelper.isNumeric(params.get(0).getExprEvaluator().getType())) {
            throw new GuardParameterException(message);
        }

        if (params.get(1).getExprEvaluator().getType() != Integer.class) {
            throw new GuardParameterException(message);
        }

        this.millisecondsExpr = params.get(0);
        this.numCountToExpr = params.get(1);
        this.convertor = convertor;
    }

    public Guard makeGuard(PatternContext context, MatchedEventMap matchedEventMap, Quitable quitable, EvalStateNodeNumber stateNodeId, Object guardState)
    {
        Object millisecondVal = PatternExpressionUtil.evaluate("Timer-Within-Or-Max-Count guard", matchedEventMap, millisecondsExpr, convertor, context);
        if (null == millisecondVal) {
            throw new EPException("Timer-within-or-max first parameter evaluated to a null-value");
        }
        Number param = (Number) millisecondVal;
        long milliseconds = Math.round(1000d * param.doubleValue());

        Object numCountToVal = PatternExpressionUtil.evaluate("Timer-Within-Or-Max-Count guard", matchedEventMap, numCountToExpr, convertor,context);
        if (null == numCountToVal) {
            throw new EPException("Timer-within-or-max second parameter evaluated to a null-value");
        }
        Integer numCountTo = (Integer) numCountToVal;
        return new TimerWithinOrMaxCountGuard(milliseconds, numCountTo, quitable);
    }
}
