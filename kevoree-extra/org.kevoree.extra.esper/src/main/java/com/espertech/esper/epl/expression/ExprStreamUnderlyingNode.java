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
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Map;

/**
 * Represents an stream selector that returns the streams underlying event, or null if undefined.
 */
public class ExprStreamUnderlyingNode extends ExprNode implements ExprEvaluator
{
    private final String streamName;
    private final boolean isWildcard;
    private int streamNum = -1;
    private Class type;
    private static final long serialVersionUID = 6611578192872250478L;

    /**
     * Ctor.
     * @param streamName is the name of the stream for which to return the underlying event
     */
    public ExprStreamUnderlyingNode(String streamName, boolean isWildcard)
    {
        if ((streamName == null) && (!isWildcard))
        {
            throw new IllegalArgumentException("Stream name is null");
        }
        this.streamName = streamName;
        this.isWildcard = isWildcard;
    }

    public ExprStreamUnderlyingNode(String streamName, boolean wildcard, int streamNum, Class type) {
        this.streamName = streamName;
        isWildcard = wildcard;
        this.streamNum = streamNum;
        this.type = type;
    }

    @Override
    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    /**
     * Returns the stream name.
     * @return stream name
     */
    public String getStreamName()
    {
        return streamName;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (isWildcard) {
            if (streamTypeService.getStreamNames().length > 1) {
                throw new ExprValidationException("Wildcard must be stream wildcard if specifying multiple streams, use the 'streamname.*' syntax instead");
            }
            streamNum = 0;
        }
        else {
            streamNum = streamTypeService.getStreamNumForStreamName(streamName);
        }

        if (streamNum == -1)
        {
            throw new ExprValidationException("Stream by name '" + streamName + "' could not be found among all streams");
        }

        EventType eventType = streamTypeService.getEventTypes()[streamNum];
        type = eventType.getUnderlyingType();
    }

    public Class getType()
    {
        if (streamNum == -1)
        {
            throw new IllegalStateException("Stream underlying node has not been validated");
        }
        return type;
    }

    public boolean isConstantResult()
    {
        return false;
    }

    /**
     * Returns stream id supplying the property value.
     * @return stream number
     */
    public int getStreamId()
    {
        if (streamNum == -1)
        {
            throw new IllegalStateException("Stream underlying node has not been validated");
        }
        return streamNum;
    }

    public String toString()
    {
        return "streamName=" + streamName +
                " streamNum=" + streamNum;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        EventBean event = eventsPerStream[streamNum];
        if (event == null)
        {
            return null;
        }
        return event.getUnderlying();
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(streamName);
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprStreamUnderlyingNode))
        {
            return false;
        }

        ExprStreamUnderlyingNode other = (ExprStreamUnderlyingNode) node;
        if (this.isWildcard != other.isWildcard) {
            return false;
        }
        if (this.isWildcard) {
            return true;
        }
        return (this.streamName.equals(other.streamName));
    }
}
