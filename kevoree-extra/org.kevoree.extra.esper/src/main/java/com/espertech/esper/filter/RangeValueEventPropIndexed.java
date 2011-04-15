package com.espertech.esper.filter;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.pattern.MatchedEventMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An event property as a filter parameter representing a range.
 */
public class RangeValueEventPropIndexed implements FilterSpecParamRangeValue
{
    private static final Log log = LogFactory.getLog(RangeValueEventPropIndexed.class);
    private final String resultEventAsName;
    private final int resultEventIndex;
    private final String resultEventProperty;
    private final String statementName;
    private static final long serialVersionUID = -2443484252813342579L;

    /**
     * Ctor.
     * @param resultEventAsName is the event tag
     * @param resultEventProperty is the event property name
     * @param resultEventIndex index for event
     */
    public RangeValueEventPropIndexed(String resultEventAsName, int resultEventIndex, String resultEventProperty, String statementName)
    {
        this.resultEventAsName = resultEventAsName;
        this.resultEventIndex = resultEventIndex;
        this.resultEventProperty = resultEventProperty;
        this.statementName = statementName;
    }

    public int getFilterHash()
    {
        return resultEventProperty.hashCode();
    }

    /**
     * Returns the index.
     * @return index
     */
    public int getResultEventIndex()
    {
        return resultEventIndex;
    }

    public final Double getFilterValue(MatchedEventMap matchedEvents)
    {
        EventBean[] events = (EventBean[]) matchedEvents.getMatchingEventAsObject(resultEventAsName);

        Number value;
        if (events == null)
        {
            log.warn("Matching events for tag '" + resultEventAsName + "' returned a null result, using null value in filter criteria, for statement '" + statementName + "'");
            return null;
        }
        else if (resultEventIndex > (events.length - 1))
        {
            log.warn("Matching events for tag '" + resultEventAsName + "' returned no result for index " + resultEventIndex + " at array length " + events.length + ", using null value in filter criteria, for statement '" + statementName + "'");
            return null;
        }
        else
        {
            value = (Number) events[resultEventIndex].get(resultEventProperty);
        }

        if (value == null)
        {
            return null;
        }
        return value.doubleValue();
    }

    /**
     * Returns the tag name or stream name to use for the event property.
     * @return tag name
     */
    public String getResultEventAsName()
    {
        return resultEventAsName;
    }

    /**
     * Returns the name of the event property.
     * @return event property name
     */
    public String getResultEventProperty()
    {
        return resultEventProperty;
    }

    public final String toString()
    {
        return "resultEventProp=" + resultEventAsName + '.' + resultEventProperty;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof RangeValueEventPropIndexed))
        {
            return false;
        }

        RangeValueEventPropIndexed other = (RangeValueEventPropIndexed) obj;
        if ( (other.resultEventAsName.equals(this.resultEventAsName)) &&
             (other.resultEventProperty.equals(this.resultEventProperty) &&
             (other.resultEventIndex == resultEventIndex)))
        {
            return true;
        }

        return false;
    }

    public int hashCode()
    {
        return resultEventProperty.hashCode();
    }
}
