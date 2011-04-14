package com.espertech.esper.epl.property;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

/**
 * A property evaluator that returns a full row of events for each stream, i.e. flattened inner-join results for
 * property-upon-property.
 */
public class PropertyEvaluatorAccumulative
{
    private static final Log log = LogFactory.getLog(PropertyEvaluatorAccumulative.class);

    private final EventPropertyGetter[] getter;
    private final FragmentEventType[] fragmentEventType;
    private final ExprEvaluator[] whereClauses;
    private final EventBean[] eventsPerStream;
    private final int lastLevel;
    private final int levels;
    private final List<String> propertyNames;

    /**
     * Ctor.
     * @param getter property getters
     * @param fragmentEventType property fragment types
     * @param whereClauses filters, if any
     * @param propertyNames the property names that are staggered
     */
    public PropertyEvaluatorAccumulative(EventPropertyGetter[] getter, FragmentEventType[] fragmentEventType, ExprEvaluator[] whereClauses, List<String> propertyNames)
    {
        this.fragmentEventType = fragmentEventType;
        this.getter = getter;
        this.whereClauses = whereClauses;
        lastLevel = fragmentEventType.length - 1;
        levels = fragmentEventType.length + 1;
        eventsPerStream = new EventBean[levels];
        this.propertyNames = propertyNames;
    }

    /**
     * Returns the accumulative events for the input event.
     * @param event is the input event
     * @param exprEvaluatorContext expression evaluation context
     * @return events per stream for each row
     */
    public ArrayDeque<EventBean[]> getAccumulative(EventBean event, ExprEvaluatorContext exprEvaluatorContext)
    {
        ArrayDeque<EventBean[]> resultEvents = new ArrayDeque<EventBean[]>();
        eventsPerStream[0] = event;
        populateEvents(event, 0, resultEvents, exprEvaluatorContext);
        if (resultEvents.isEmpty())
        {
            return null;
        }
        return resultEvents;
    }

    private void populateEvents(EventBean branch, int level, Collection<EventBean[]> events, ExprEvaluatorContext exprEvaluatorContext)
    {
        try
        {
            Object result = getter[level].getFragment(branch);

            if (fragmentEventType[level].isIndexed())
            {
                EventBean[] fragments = (EventBean[]) result;
                if (level == lastLevel)
                {
                    if (whereClauses[level] != null)
                    {
                        for (EventBean event : fragments)
                        {
                            eventsPerStream[level+1] = event;
                            if (ExprNodeUtility.applyFilterExpression(whereClauses[level], eventsPerStream, exprEvaluatorContext))
                            {
                                EventBean[] eventsPerRow = new EventBean[levels];
                                System.arraycopy(eventsPerStream, 0, eventsPerRow, 0, levels);
                                events.add(eventsPerRow);
                            }
                        }
                    }
                    else
                    {
                        for (EventBean event : fragments)
                        {
                            eventsPerStream[level+1] = event;
                            EventBean[] eventsPerRow = new EventBean[levels];
                            System.arraycopy(eventsPerStream, 0, eventsPerRow, 0, levels);
                            events.add(eventsPerRow);
                        }
                    }
                }
                else
                {
                    if (whereClauses[level] != null)
                    {
                        for (EventBean next : fragments)
                        {
                            eventsPerStream[level+1] = next;
                            if (ExprNodeUtility.applyFilterExpression(whereClauses[level], eventsPerStream, exprEvaluatorContext))
                            {
                                populateEvents(next, level+1, events, exprEvaluatorContext);
                            }
                        }
                    }
                    else
                    {
                        for (EventBean next : fragments)
                        {
                            eventsPerStream[level+1] = next;
                            populateEvents(next, level+1, events, exprEvaluatorContext);
                        }
                    }
                }
            }
            else
            {
                EventBean fragment = (EventBean) result;
                if (level == lastLevel)
                {
                    if (whereClauses[level] != null)
                    {
                        eventsPerStream[level+1] = fragment;
                        if (ExprNodeUtility.applyFilterExpression(whereClauses[level], eventsPerStream, exprEvaluatorContext))
                        {
                            EventBean[] eventsPerRow = new EventBean[levels];
                            System.arraycopy(eventsPerStream, 0, eventsPerRow, 0, levels);
                            events.add(eventsPerRow);
                        }
                    }
                    else
                    {
                        eventsPerStream[level+1] = fragment;
                        EventBean[] eventsPerRow = new EventBean[levels];
                        System.arraycopy(eventsPerStream, 0, eventsPerRow, 0, levels);
                        events.add(eventsPerRow);
                    }
                }
                else
                {
                    if (whereClauses[level] != null)
                    {
                        eventsPerStream[level+1] = fragment;
                        if (ExprNodeUtility.applyFilterExpression(whereClauses[level], eventsPerStream, exprEvaluatorContext))
                        {
                            populateEvents(fragment, level+1, events, exprEvaluatorContext);
                        }
                    }
                    else
                    {
                        eventsPerStream[level+1] = fragment;
                        populateEvents(fragment, level+1, events, exprEvaluatorContext);
                    }
                }
            }
        }
        catch (RuntimeException ex)
        {
            log.error("Unexpected error evaluating property expression for event of type '" +
                    branch.getEventType().getName() +
                    "' and property '" +
                    propertyNames.get(level + 1) + "': " + ex.getMessage(), ex);
        }
    }
}