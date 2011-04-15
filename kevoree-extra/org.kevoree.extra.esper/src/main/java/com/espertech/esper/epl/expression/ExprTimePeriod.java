package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Map;

/**
 * Expression representing a time period.
 * <p>
 * Child nodes to this expression carry the actual parts and must return a numeric value.
 */
public class ExprTimePeriod extends ExprNode implements ExprEvaluator
{
    private static final Log log = LogFactory.getLog(ExprTimePeriod.class);

    private final boolean hasYear;
    private final boolean hasMonth;
    private final boolean hasWeek;
    private final boolean hasDay;
    private final boolean hasHour;
    private final boolean hasMinute;
    private final boolean hasSecond;
    private final boolean hasMillisecond;
    private boolean hasVariable;
    private transient ExprEvaluator[] evaluators;
    private transient TimePeriodAdder[] adders;
    private static final long serialVersionUID = -7229827032500659319L;

    /**
     * Ctor.
     * @param hasDay true if the expression has that part, false if not
     * @param hasHour true if the expression has that part, false if not
     * @param hasMinute true if the expression has that part, false if not
     * @param hasSecond true if the expression has that part, false if not
     * @param hasMillisecond true if the expression has that part, false if not
     */
    public ExprTimePeriod(boolean hasYear, boolean hasMonth, boolean hasWeek, boolean hasDay, boolean hasHour, boolean hasMinute, boolean hasSecond, boolean hasMillisecond)
    {
        this.hasYear = hasYear;
        this.hasMonth = hasMonth;
        this.hasWeek = hasWeek;
        this.hasDay = hasDay;
        this.hasHour = hasHour;
        this.hasMinute = hasMinute;
        this.hasSecond = hasSecond;
        this.hasMillisecond = hasMillisecond;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    /**
     * Indicator whether the time period has a day part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasDay()
    {
        return hasDay;
    }

    /**
     * Indicator whether the time period has a hour part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasHour()
    {
        return hasHour;
    }

    /**
     * Indicator whether the time period has a minute part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasMinute()
    {
        return hasMinute;
    }

    /**
     * Indicator whether the time period has a second part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasSecond()
    {
        return hasSecond;
    }

    /**
     * Indicator whether the time period has a millisecond part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasMillisecond()
    {
        return hasMillisecond;
    }

    /**
     * Indicator whether the time period has a year part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasYear() {
        return hasYear;
    }

    /**
     * Indicator whether the time period has a month part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasMonth() {
        return hasMonth;
    }

    /**
     * Indicator whether the time period has a week part child expression.
     * @return true for part present, false for not present
     */
    public boolean isHasWeek() {
        return hasWeek;
    }

    /**
     * Indicator whether the time period has a variable in any of the child expressions.
     * @return true for variable present, false for not present
     */
    public boolean hasVariable()
    {
        return hasVariable;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());
        for (ExprNode childNode : this.getChildNodes())
        {
            validate(childNode);
        }

        ArrayDeque<TimePeriodAdder> list = new ArrayDeque<TimePeriodAdder>();
        if (hasYear) {
            list.add(new TimePeriodAdderYear());
        }
        if (hasMonth) {
            list.add(new TimePeriodAdderMonth());
        }
        if (hasWeek) {
            list.add(new TimePeriodAdderWeek());
        }
        if (hasDay) {
            list.add(new TimePeriodAdderDay());
        }
        if (hasHour) {
            list.add(new TimePeriodAdderHour());
        }
        if (hasMinute) {
            list.add(new TimePeriodAdderMinute());
        }
        if (hasSecond) {
            list.add(new TimePeriodAdderSecond());
        }
        if (hasMillisecond) {
            list.add(new TimePeriodAdderMSec());
        }
        adders = list.toArray(new TimePeriodAdder[list.size()]);
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    private void validate(ExprNode expression) throws ExprValidationException
    {
        if (expression == null)
        {
            return;
        }
        Class returnType = expression.getExprEvaluator().getType();
        if (!JavaClassHelper.isNumeric(returnType))
        {
            throw new ExprValidationException("Time period expression requires a numeric parameter type");
        }
        if (expression instanceof ExprVariableNode)
        {
            hasVariable = true;
        }
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        double seconds = 0;
        for (int i = 0; i < adders.length; i++) {
            Double result = eval(evaluators[i], eventsPerStream, exprEvaluatorContext);
            if (result == null)
            {
                logWarn(i);
                return null;
            }
            seconds += adders[i].compute(result);
        }
        return seconds;
    }

    private void logWarn(int ctr)
    {
        log.warn("Time period expression returned a null value for expression '" + this.getChildNodes().get(ctr).toExpressionString() + "'");
    }

    public Class getType()
    {
        return Double.class;
    }

    public boolean isConstantResult()
    {
        for (ExprNode child : getChildNodes())
        {
            if (!child.isConstantResult())
            {
                return false;
            }
        }
        return true;
    }

    public String toExpressionString()
    {
        StringBuffer buf = new StringBuffer();
        int exprCtr = 0;
        if (hasYear)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" years ");
        }
        if (hasMonth)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" months ");
        }
        if (hasWeek)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" weeks ");
        }
        if (hasDay)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" days ");
        }
        if (hasHour)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" hours ");
        }
        if (hasMinute)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" minutes ");
        }
        if (hasSecond)
        {
            buf.append(getChildNodes().get(exprCtr++).toExpressionString());
            buf.append(" seconds ");
        }
        if (hasMillisecond)
        {
            buf.append(getChildNodes().get(exprCtr).toExpressionString());
            buf.append(" milliseconds ");
        }
        return buf.toString();
    }
    
    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprTimePeriod))
        {
            return false;
        }
        ExprTimePeriod other = (ExprTimePeriod) node;

        if (hasYear!= other.hasYear)
        {
            return false;
        }
        if (hasMonth != other.hasMonth)
        {
            return false;
        }
        if (hasWeek != other.hasWeek)
        {
            return false;
        }
        if (hasDay != other.hasDay)
        {
            return false;
        }
        if (hasHour != other.hasHour)
        {
            return false;
        }
        if (hasMinute != other.hasMinute)
        {
            return false;
        }
        if (hasSecond != other.hasSecond)
        {
            return false;
        }
        return (hasMillisecond == other.hasMillisecond);
    }

    private Double eval(ExprEvaluator expr, EventBean[] events, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object value = expr.evaluate(events, true, exprEvaluatorContext);
        if (value == null)
        {
            return null;
        }
        if (value instanceof BigDecimal)
        {
            return ((Number) value).doubleValue();
        }
        if (value instanceof BigInteger)
        {
            return ((Number) value).doubleValue();
        }
        return ((Number) value).doubleValue();
    }

    public static interface TimePeriodAdder {
        public double compute(Double value);
    }

    public static class TimePeriodAdderYear implements TimePeriodAdder {
        private static final double MULTIPLIER = 365*24*60*60;
        public double compute(Double value) {
            return value*MULTIPLIER;
        }
    }

    public static class TimePeriodAdderMonth implements TimePeriodAdder {
        private static final double MULTIPLIER = 30*24*60*60;
        public double compute(Double value) {
            return value*MULTIPLIER;
        }
    }

    public static class TimePeriodAdderWeek implements TimePeriodAdder {
        private static final double MULTIPLIER = 7*24*60*60;
        public double compute(Double value) {
            return value*MULTIPLIER;
        }
    }

    public static class TimePeriodAdderDay implements TimePeriodAdder {
        private static final double MULTIPLIER = 24*60*60;
        public double compute(Double value) {
            return value*MULTIPLIER;
        }
    }

    public static class TimePeriodAdderHour implements TimePeriodAdder {
        private static final double MULTIPLIER = 60*60;
        public double compute(Double value) {
            return value*MULTIPLIER;
        }
    }

    public static class TimePeriodAdderMinute implements TimePeriodAdder {
        private static final double MULTIPLIER = 60;
        public double compute(Double value) {
            return value*MULTIPLIER;
        }
    }

    public static class TimePeriodAdderSecond implements TimePeriodAdder {
        public double compute(Double value) {
            return value;
        }
    }

    public static class TimePeriodAdderMSec implements TimePeriodAdder {
        public double compute(Double value) {
            return value / 1000d;
        }
    }
}
