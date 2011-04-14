package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.type.CronOperatorEnum;
import com.espertech.esper.type.CronParameter;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Expression for a parameter within a crontab.
 * <p>
 * May have one subnode depending on the cron parameter type.
 */
public class ExprNumberSetCronParam extends ExprNode implements ExprEvaluator
{
    private static final Log log = LogFactory.getLog(ExprNumberSetCronParam.class);

    private final CronOperatorEnum cronOperator;
    private transient TimeProvider timeProvider;
    private transient ExprEvaluator evaluator;
    private static final long serialVersionUID = -1315999998249935318L;

    /**
     * Ctor.
     * @param cronOperator type of cron parameter
     */
    public ExprNumberSetCronParam(CronOperatorEnum cronOperator)
    {
        this.cronOperator = cronOperator;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    /**
     * Returns the cron parameter type.
     * @return type of cron parameter
     */
    public CronOperatorEnum getCronOperator()
    {
        return cronOperator;
    }

    public String toExpressionString()
    {
        if (this.getChildNodes().isEmpty())
        {
            return cronOperator.getSyntax();
        }
        return this.getChildNodes().get(0).toExpressionString() + " " + cronOperator.getSyntax(); 
    }

    public boolean isConstantResult()
    {
        if (this.getChildNodes().size() == 0)
        {
            return true;
        }
        return this.getChildNodes().get(0).isConstantResult();
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprNumberSetCronParam))
        {
            return false;
        }
        ExprNumberSetCronParam other = (ExprNumberSetCronParam) node;
        return other.cronOperator.equals(this.cronOperator); 
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        this.timeProvider = timeProvider;
        if (this.getChildNodes().isEmpty())
        {
            return;
        }
        evaluator = this.getChildNodes().get(0).getExprEvaluator();
        Class type = evaluator.getType();
        if (!(JavaClassHelper.isNumericNonFP(type)))
        {
            throw new ExprValidationException("Frequency operator requires an integer-type parameter");
        }
    }

    public Class getType()
    {
        return CronParameter.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (timeProvider == null)
        {
            throw new EPException("Expression node has not been validated");
        }
        if (this.getChildNodes().isEmpty())
        {
            return new CronParameter(cronOperator, null, timeProvider.getTime());
        }
        Object value = evaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (value == null)
        {
            log.warn("Null value returned for cron parameter");
            return new CronParameter(cronOperator, null, timeProvider.getTime());
        }
        else
        {
            int intValue = ((Number) value).intValue();
            return new CronParameter(cronOperator, intValue, timeProvider.getTime());
        }
    }
}
