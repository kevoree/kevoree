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
import com.espertech.esper.view.window.RandomAccessByIndex;
import com.espertech.esper.view.window.RandomAccessByIndexGetter;
import com.espertech.esper.view.window.RelativeAccessByEventNIndex;
import com.espertech.esper.view.window.RelativeAccessByEventNIndexMap;

import java.lang.reflect.Array;
import java.util.Iterator;

public class ExprPreviousEvalWindow implements ExprPreviousEval
{
    private final int streamNumber;
    private final ExprEvaluator evalNode;
    private final Class componentType;
    private final RandomAccessByIndexGetter randomAccessGetter;
    private final RelativeAccessByEventNIndexMap relativeAccessGetter;

    public ExprPreviousEvalWindow(int streamNumber, ExprEvaluator evalNode, Class componentType, RandomAccessByIndexGetter randomAccessGetter, RelativeAccessByEventNIndexMap relativeAccessGetter)
    {
        this.streamNumber = streamNumber;
        this.evalNode = evalNode;
        this.componentType = componentType;
        this.randomAccessGetter = randomAccessGetter;
        this.relativeAccessGetter = relativeAccessGetter;
    }

    public Object evaluate(EventBean[] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        Iterator<EventBean> events;
        int size;
        if (randomAccessGetter != null)
        {
            RandomAccessByIndex randomAccess = randomAccessGetter.getAccessor();
            events = randomAccess.getWindowIterator();
            size = (int) randomAccess.getWindowCount();
        }
        else
        {
            EventBean evalEvent = eventsPerStream[streamNumber];
            RelativeAccessByEventNIndex relativeAccess = relativeAccessGetter.getAccessor(evalEvent);
            size = relativeAccess.getWindowToEventCount(evalEvent);
            events = relativeAccess.getWindowToEvent(evalEvent);
        }

        if (size <= 0) {
            return null;
        }

        EventBean originalEvent = eventsPerStream[streamNumber];
        Object[] result = (Object[]) Array.newInstance(componentType, size);

        for (int i = 0; i < size; i++) {
            eventsPerStream[streamNumber] = events.next();
            Object evalResult = evalNode.evaluate(eventsPerStream, true, exprEvaluatorContext);
            result[i] = evalResult;
        }

        eventsPerStream[streamNumber] = originalEvent;
        return result;
    }
}