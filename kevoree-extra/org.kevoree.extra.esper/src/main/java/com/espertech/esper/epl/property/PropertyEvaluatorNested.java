package com.espertech.esper.epl.property;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A property evaluator that considers nested properties and that considers where-clauses
 * but does not consider select-clauses.
 */
public class PropertyEvaluatorNested implements PropertyEvaluator
{
    private static final Log log = LogFactory.getLog(PropertyEvaluatorNested.class);
    
    private final EventPropertyGetter[] getter;
    private final FragmentEventType[] fragmentEventType;
    private final ExprEvaluator[] whereClauses;
    private final EventBean[] eventsPerStream;
    private final int lastLevel;
    private final List<String> propertyNames;

    /**
     * Ctor.
     * @param getter property getter
     * @param fragmentEventType the fragments
     * @param whereClauses the where clauses
     * @param propertyNames the property names that are staggered
     */
    public PropertyEvaluatorNested(EventPropertyGetter[] getter, FragmentEventType[] fragmentEventType, ExprEvaluator[] whereClauses, List<String> propertyNames)
    {
        this.fragmentEventType = fragmentEventType;
        this.getter = getter;
        this.whereClauses = whereClauses;
        lastLevel = fragmentEventType.length - 1;
        eventsPerStream = new EventBean[fragmentEventType.length + 1];
        this.propertyNames = propertyNames;
    }

    public EventBean[] getProperty(EventBean event, ExprEvaluatorContext exprEvaluatorContext)
    {
        ArrayDeque<EventBean> resultEvents = new ArrayDeque<EventBean>();
        eventsPerStream[0] = event;
        populateEvents(event, 0, resultEvents, exprEvaluatorContext);
        if (resultEvents.isEmpty())
        {
            return null;
        }
        return resultEvents.toArray(new EventBean[resultEvents.size()]);
    }

    private void populateEvents(EventBean branch, int level, Collection<EventBean> events, ExprEvaluatorContext exprEvaluatorContext)
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
                                events.add(event);
                            }
                        }
                    }
                    else
                    {
                        events.addAll(Arrays.asList(fragments));
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
                            events.add(fragment);
                        }
                    }
                    else
                    {
                        events.add(fragment);
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

    public EventType getFragmentEventType()
    {
        return fragmentEventType[lastLevel].getFragmentType();
    }

    public boolean compareTo(PropertyEvaluator otherEval)
    {
        return false;
    }
}
