package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.rowregex.RegexPartitionStateRandomAccess;
import com.espertech.esper.rowregex.RegexPartitionStateRandomAccessGetter;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;

import java.util.Map;

/**
 * Represents the 'prev' previous event function in match-recognize "define" item.
 */
public class ExprPreviousMatchRecognizeNode extends ExprNode implements ExprEvaluator
{
    private static final long serialVersionUID = 0L;

    private Class resultType;
    private int streamNumber;
    private Integer constantIndexNumber;

    private transient RegexPartitionStateRandomAccessGetter getter;
    private transient ExprEvaluator evaluator;
    private int assignedIndex;

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 2)
        {
            throw new ExprValidationException("Match-Recognize Previous expression must have 2 child nodes");
        }

        if (!(this.getChildNodes().get(0) instanceof ExprIdentNode))
        {
            throw new ExprValidationException("Match-Recognize Previous expression requires an property identifier as the first parameter");
        }

        if (!this.getChildNodes().get(1).isConstantResult() || (!JavaClassHelper.isNumericNonFP(this.getChildNodes().get(1).getExprEvaluator().getType())))
        {
            throw new ExprValidationException("Match-Recognize Previous expression requires an integer index parameter or expression as the second parameter");
        }

        ExprNode constantNode = this.getChildNodes().get(1);
        Object value = constantNode.getExprEvaluator().evaluate(null, false, exprEvaluatorContext);
        if (!(value instanceof Number))
        {
            throw new ExprValidationException("Match-Recognize Previous expression requires an integer index parameter or expression as the second parameter");
        }
        constantIndexNumber = ((Number) value).intValue();

        // Determine stream number
        ExprIdentNode identNode = (ExprIdentNode) this.getChildNodes().get(0);
        streamNumber = identNode.getStreamId();
        evaluator = this.getChildNodes().get(0).getExprEvaluator();
        resultType = evaluator.getType();
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    /**
     * Returns the index number.
     * @return index number
     */
    public Integer getConstantIndexNumber()
    {
        if (constantIndexNumber == null)
        {
            ExprNode constantNode = this.getChildNodes().get(1);
            Object value = constantNode.getExprEvaluator().evaluate(null, false, null);
            constantIndexNumber = ((Number) value).intValue();            
        }
        return constantIndexNumber;
    }

    public Class getType()
    {
        return resultType;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        RegexPartitionStateRandomAccess access = getter.getAccessor();
        EventBean substituteEvent = access.getPreviousEvent(assignedIndex);

        if (substituteEvent == null)
        {
            return null;
        }

        // Substitute original event with prior event, evaluate inner expression
        EventBean originalEvent = eventsPerStream[streamNumber];
        eventsPerStream[streamNumber] = substituteEvent;
        Object evalResult = evaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        eventsPerStream[streamNumber] = originalEvent;

        return evalResult;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("prev(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(',');
        buffer.append(this.getChildNodes().get(1).toExpressionString());
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprPreviousMatchRecognizeNode))
        {
            return false;
        }

        return true;
    }

    /**
     * Sets the getter to obtain the value.
     * @param getter to set
     */
    public void setGetter(RegexPartitionStateRandomAccessGetter getter)
    {
        this.getter = getter;
    }

    /**
     * Sets the index to use when accessing via getter
     * @param assignedIndex index
     */
    public void setAssignedIndex(int assignedIndex)
    {
        this.assignedIndex = assignedIndex;
    }
}
