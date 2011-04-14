/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.client.EventBean;

/**
 * Interface for a callback method to be called when an event matches a filter specification. Provided
 * as a convenience for use as a filter handle for registering with the {@link FilterService}.
 */
public interface FilterHandleCallback extends FilterHandle
{
    /**
     * Indicate that an event was evaluated by the {@link com.espertech.esper.filter.FilterService}
     * which matches the filter specification {@link com.espertech.esper.filter.FilterSpecCompiled} associated with this callback.
     * @param event - the event received that matches the filter specification
     */
    public void matchFound(EventBean event);

    /**
     * Returns true if the filter applies to subselects.
     * @return subselect filter
     */
    public boolean isSubSelect();
}
