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
 * Strategy for performing an output via dispatch view.
 */
public interface OutputStrategy
{
    /**
     * Outputs the result to the output view and following update policy.
     * @param forceUpdate indicates whether output can be skipped, such as when no results collected
     * @param result the output to indicate
     * @param outputView the view to output to
     */
    public void output(boolean forceUpdate, UniformPair<EventBean[]> result, UpdateDispatchView outputView);
}
