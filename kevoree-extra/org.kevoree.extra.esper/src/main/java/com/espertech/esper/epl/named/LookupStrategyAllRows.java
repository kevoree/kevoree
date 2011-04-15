/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Deletes from a named window all events simply using the named window's data window iterator.
 */
public class LookupStrategyAllRows implements LookupStrategy
{
    private Iterable<EventBean> source;

    /**
     * Ctor.
     * @param source iterator of the data window under the named window
     */
    public LookupStrategyAllRows(Iterable<EventBean> source)
    {
        this.source = source;
    }

    public EventBean[] lookup(EventBean[] newData, ExprEvaluatorContext exprEvaluatorContext)
    {
        ArrayList<EventBean> events = new ArrayList<EventBean>();
        for (Iterator<EventBean> it = source.iterator(); it.hasNext();)
        {
            events.add(it.next());
        }
        return events.toArray(new EventBean[events.size()]);
    }
}
