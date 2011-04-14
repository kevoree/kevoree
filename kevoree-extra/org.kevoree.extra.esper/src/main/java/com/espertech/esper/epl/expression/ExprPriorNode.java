/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.view.ViewCapPriorEventAccess;
import com.espertech.esper.view.window.RandomAccessByIndex;
import com.espertech.esper.view.window.RelativeAccessByEventNIndex;

import java.util.Map;

/**
 * Represents the 'prior' prior event function in an expression node tree.
 */
public class ExprPriorNode extends ExprNode implements ViewResourceCallback, ExprEvaluator
{
    private Class resultType;
    private int streamNumber;
    private int constantIndexNumber;
    private transient RelativeAccessByEventNIndex relativeAccess;
    private transient RandomAccessByIndex randomAccess;
    private transient ExprEvaluator evaluator;
    private static final long serialVersionUID = -2115346817501589366L;

    /**
     * Returns the index of the prior.
     * @return index of prior function
     */
    public int getConstantIndexNumber()
    {
        return constantIndexNumber;
    }

    @Override
    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 2)
        {
            throw new ExprValidationException("Prior node must have 2 child nodes");
        }
        if (!(this.getChildNodes().get(0).isConstantResult()))
        {
            throw new ExprValidationException("Prior function requires an integer index parameter");
        }
        ExprNode constantNode = this.getChildNodes().get(0);
        if (constantNode.getExprEvaluator().getType() != Integer.class)
        {
            throw new ExprValidationException("Prior function requires an integer index parameter");
        }

        Object value = constantNode.getExprEvaluator().evaluate(null, false, exprEvaluatorContext);
        constantIndexNumber = ((Number) value).intValue();
        evaluator = this.getChildNodes().get(1).getExprEvaluator();

        // Determine stream number
        // Determine stream number
        if (this.getChildNodes().get(1) instanceof ExprIdentNode) {
            ExprIdentNode identNode = (ExprIdentNode) this.getChildNodes().get(1);
            streamNumber = identNode.getStreamId();
            resultType = evaluator.getType();
        }
        else if (this.getChildNodes().get(1) instanceof ExprStreamUnderlyingNode) {
            ExprStreamUnderlyingNode streamNode = (ExprStreamUnderlyingNode) this.getChildNodes().get(1);
            streamNumber = streamNode.getStreamId();
            resultType = evaluator.getType();
        }
        else
        {
            throw new ExprValidationException("Previous function requires an event property as parameter");
        }

        if (viewResourceDelegate == null)
        {
            throw new ExprValidationException("Prior function cannot be used in this context");
        }
        // Request a callback that provides the required access
        if (!viewResourceDelegate.requestCapability(streamNumber, new ViewCapPriorEventAccess(constantIndexNumber), this))
        {
            throw new ExprValidationException("Prior function requires the prior event view resource");
        }
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
        EventBean originalEvent = eventsPerStream[streamNumber];
        EventBean substituteEvent;

        if (randomAccess != null)
        {
            if (isNewData)
            {
                substituteEvent = randomAccess.getNewData(constantIndexNumber);
            }
            else
            {
                substituteEvent = randomAccess.getOldData(constantIndexNumber);
            }
        }
        else
        {
            substituteEvent = relativeAccess.getRelativeToEvent(originalEvent, constantIndexNumber);
        }

        // Substitute original event with prior event, evaluate inner expression
        eventsPerStream[streamNumber] = substituteEvent;
        Object evalResult = evaluator.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        eventsPerStream[streamNumber] = originalEvent;

        return evalResult;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("prior(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(',');
        buffer.append(this.getChildNodes().get(1).toExpressionString());
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprPriorNode))
        {
            return false;
        }

        return true;
    }

    public void setViewResource(Object resource)
    {
        if (resource instanceof RelativeAccessByEventNIndex)
        {
            relativeAccess = (RelativeAccessByEventNIndex) resource;
        }
        else if (resource instanceof RandomAccessByIndex)
        {
            randomAccess = (RandomAccessByIndex) resource;
        }
        else
        {
            throw new IllegalArgumentException("View resource " + resource.getClass() + " not recognized by expression node");
        }
    }
}
