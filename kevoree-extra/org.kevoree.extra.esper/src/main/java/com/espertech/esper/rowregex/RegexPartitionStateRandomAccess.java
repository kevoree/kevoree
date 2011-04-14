package com.espertech.esper.rowregex;

import com.espertech.esper.client.EventBean;

/**
 * Interface for random access to a previous event.
 */
public interface RegexPartitionStateRandomAccess
{
    /**
     * Returns an new data event given an index.
     * @param index to return new data for
     * @return new data event
     */
    public EventBean getPreviousEvent(int index);
}
