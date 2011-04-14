/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.UpdateDispatchView;
import com.espertech.esper.client.EventBean;

/**
 * An output strategy that outputs if there are results of if
 * the force-update flag is set.
 */
public class OutputStrategySimple implements OutputStrategy
{
    public void output(boolean forceUpdate, UniformPair<EventBean[]> result, UpdateDispatchView finalView)
    {
        EventBean[] newEvents = result != null ? result.getFirst() : null;
        EventBean[] oldEvents = result != null ? result.getSecond() : null;
        if(newEvents != null || oldEvents != null)
        {
            finalView.newResult(result);
        }
        else if(forceUpdate)
        {
            finalView.newResult(result);
        }
    }
}
