package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.type.WildcardParameter;

import java.util.Map;

/**
 * Expression for use within crontab to specify a wildcard.
 */
public class ExprNumberSetWildcard extends ExprNode implements ExprEvaluator
{
    private static final WildcardParameter wildcardParameter = new WildcardParameter();
    private static final long serialVersionUID = -6098833102154556698L;

    public String toExpressionString()
    {
        return "*";
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public boolean isConstantResult()
    {
        return true;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public boolean equalsNode(ExprNode node)
    {
        return node instanceof ExprNumberSetWildcard;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
    }

    public Class getType()
    {
        return WildcardParameter.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        return wildcardParameter;
    }
}
