package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.type.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Expression for use within crontab to specify a list of values.
 */
public class ExprNumberSetList extends ExprNode implements ExprEvaluator
{
    private static final Log log = LogFactory.getLog(ExprNumberSetList.class);
    private transient ExprEvaluator[] evaluators;
    private static final long serialVersionUID = 4941618470342360450L;

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        String delimiter = "";

        buffer.append('[');
        Iterator<ExprNode> it = this.getChildNodes().iterator();
        do
        {
            ExprNode expr = it.next();
            buffer.append(delimiter);
            buffer.append(expr.toExpressionString());
            delimiter = ",";
        }
        while (it.hasNext());
        buffer.append(']');

        return buffer.toString();
    }

    public boolean isConstantResult()
    {
        for (ExprNode child : this.getChildNodes())
        {
            if (!child.isConstantResult())
            {
                return false;
            }
        }
        return true;
    }

    public boolean equalsNode(ExprNode node)
    {
        return (node instanceof ExprNumberSetList);
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        // all nodes must either be int, frequency or range
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());
        for (ExprEvaluator child : evaluators)
        {
            Class type = child.getType();
            if ((type == FrequencyParameter.class) || (type == RangeParameter.class))
            {
                continue;
            }
            if (!(JavaClassHelper.isNumericNonFP(type)))
            {
                throw new ExprValidationException("Frequency operator requires an integer-type parameter");
            }
        }
        
    }

    public Class getType()
    {
        return ListParameter.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        List<NumberSetParameter> parameters = new ArrayList<NumberSetParameter>();
        for (ExprEvaluator child : evaluators)
        {
            Object value = child.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if (value == null)
            {
                log.info("Null value returned for lower bounds value in list parameter, skipping parameter");
                continue;
            }
            if ((value instanceof FrequencyParameter) || (value instanceof RangeParameter))
            {
                parameters.add((NumberSetParameter) value);
                continue;
            }
            
            int intValue = ((Number) value).intValue();
            parameters.add(new IntParameter(intValue));
        }
        if (parameters.isEmpty())
        {
            log.warn("Empty list of values in list parameter, using upper bounds");
            parameters.add(new IntParameter(Integer.MAX_VALUE));
        }
        return new ListParameter(parameters);
    }
}
