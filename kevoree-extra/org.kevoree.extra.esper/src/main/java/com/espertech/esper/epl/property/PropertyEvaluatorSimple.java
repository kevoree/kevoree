package com.espertech.esper.epl.property;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Property evaluator that considers only level one and considers a where-clause,
 * but does not consider a select clause or N-level.
 */
public class PropertyEvaluatorSimple implements PropertyEvaluator
{
    private static final Log log = LogFactory.getLog(PropertyEvaluatorSimple.class);
    private final EventPropertyGetter getter;
    private final FragmentEventType fragmentEventType;
    private final ExprEvaluator filter;
    private final String propertyName;

    /**
     * Ctor.
     * @param getter property getter
     * @param fragmentEventType property event type
     * @param filter optional where-clause expression
     * @param propertyName the property name
     */
    public PropertyEvaluatorSimple(EventPropertyGetter getter, FragmentEventType fragmentEventType, ExprEvaluator filter, String propertyName)
    {
        this.fragmentEventType = fragmentEventType;
        this.getter = getter;
        this.filter = filter;
        this.propertyName = propertyName;
    }

    public EventBean[] getProperty(EventBean event, ExprEvaluatorContext exprEvaluatorContext)
    {
        try
        {
            Object result = getter.getFragment(event);

            EventBean[] rows;
            if (fragmentEventType.isIndexed())
            {
                rows = (EventBean[]) result;
            }
            else
            {
                rows = new EventBean[] {(EventBean) result};
            }

            if (filter == null)
            {
                return rows;
            }
            return ExprNodeUtility.applyFilterExpression(filter, event, (EventBean[]) result, exprEvaluatorContext);
        }
        catch (RuntimeException ex)
        {
            log.error("Unexpected error evaluating property expression for event of type '" +
                    event.getEventType().getName() +
                    "' and property '" +
                    propertyName + "': " + ex.getMessage(), ex);
        }
        return null;
    }

    public EventType getFragmentEventType()
    {
        return fragmentEventType.getFragmentType();
    }

    /**
     * Returns the property name.
     * @return property name
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    /**
     * Returns the filter.
     * @return filter
     */
    public ExprEvaluator getFilter()
    {
        return filter;
    }

    public boolean compareTo(PropertyEvaluator otherEval)
    {
        if (!(otherEval instanceof PropertyEvaluatorSimple))
        {
            return false;
        }
        PropertyEvaluatorSimple other = (PropertyEvaluatorSimple) otherEval;
        if (!other.getPropertyName().equals(this.getPropertyName()))
        {
            return false;
        }
        if ((other.getFilter() == null) && (this.getFilter() == null))
        {
            return true;
        }
        return false;
    }
}