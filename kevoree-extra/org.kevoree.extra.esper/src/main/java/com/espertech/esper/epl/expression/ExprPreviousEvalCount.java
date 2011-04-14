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

public class ExprPreviousEvalCount implements ExprPreviousEval
{
    private final int streamNumber;
    private final RandomAccessByIndexGetter randomAccessGetter;
    private final RelativeAccessByEventNIndexMap relativeAccessGetter;

    public ExprPreviousEvalCount(int streamNumber, RandomAccessByIndexGetter randomAccessGetter, RelativeAccessByEventNIndexMap relativeAccessGetter)
    {
        this.streamNumber = streamNumber;
        this.randomAccessGetter = randomAccessGetter;
        this.relativeAccessGetter = relativeAccessGetter;
    }

    public Object evaluate(EventBean[] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext)
    {
        long size;
        if (randomAccessGetter != null)
        {
            RandomAccessByIndex randomAccess = randomAccessGetter.getAccessor();
            size = randomAccess.getWindowCount();
        }
        else
        {
            EventBean evalEvent = eventsPerStream[streamNumber];
            RelativeAccessByEventNIndex relativeAccess = relativeAccessGetter.getAccessor(evalEvent);
            size = relativeAccess.getWindowToEventCount(evalEvent);
        }

        return size;
    }
}