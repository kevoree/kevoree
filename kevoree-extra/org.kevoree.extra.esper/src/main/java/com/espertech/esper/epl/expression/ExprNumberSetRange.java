package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.type.RangeParameter;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Expression for use within crontab to specify a range.
 * <p>
 * Differs from the between-expression since the value returned by evaluating is a cron-value object.
 */
public class ExprNumberSetRange extends ExprNode implements ExprEvaluator
{
    private static final Log log = LogFactory.getLog(ExprNumberSetRange.class);
    private transient ExprEvaluator evaluators[];
    private static final long serialVersionUID = -3777415170380735662L;

    public String toExpressionString()
    {
        return this.getChildNodes().get(0).toExpressionString() +
                ":" +
                this.getChildNodes().get(1).toExpressionString();
    }

    @Override
    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public boolean isConstantResult()
    {
        return this.getChildNodes().get(0).isConstantResult() && this.getChildNodes().get(1).isConstantResult();
    }

    public boolean equalsNode(ExprNode node)
    {
        return node instanceof ExprNumberSetRange;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());
        Class typeOne = evaluators[0].getType();
        Class typeTwo = evaluators[1].getType();
        if ((!(JavaClassHelper.isNumericNonFP(typeOne))) || (!(JavaClassHelper.isNumericNonFP(typeTwo))))
        {
            throw new ExprValidationException("Range operator requires integer-type parameters");
        }
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public Class getType()
    {
        return RangeParameter.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object valueLower = evaluators[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        Object valueUpper = evaluators[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (valueLower == null)
        {
            log.warn("Null value returned for lower bounds value in range parameter, using zero as lower bounds");
            valueLower = 0;
        }
        if (valueUpper == null)
        {
            log.warn("Null value returned for upper bounds value in range parameter, using max as upper bounds");
            valueUpper = Integer.MAX_VALUE;
        }
        int intValueLower = ((Number) valueLower).intValue();
        int intValueUpper = ((Number) valueUpper).intValue();
        return new RangeParameter(intValueLower, intValueUpper);
    }
}
