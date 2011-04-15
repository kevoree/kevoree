/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stat;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFieldEnum;
import com.espertech.esper.view.ViewSupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * View for computing a weighted average. The view uses 2 fields within the parent view to compute the weighted average.
 * The X field and weight field. In a price-volume example it calculates the volume-weighted average price
 * as   (sum(price * volume) / sum(volume)).
 * Example: weighted_avg("price", "volume")
 */
public final class WeightedAverageView extends ViewSupport implements CloneableView
{
    private final EventType eventType;
    private final StatementContext statementContext;
    private final ExprNode fieldNameX;
    private final ExprEvaluator fieldNameXEvaluator;
    private final ExprNode fieldNameWeight;
    private final ExprEvaluator fieldNameWeightEvaluator;
    private final StatViewAdditionalProps additionalProps;

    private EventBean[] eventsPerStream = new EventBean[1];

    private double sumXtimesW = Double.NaN;
    private double sumW = Double.NaN;
    private double currentValue = Double.NaN;
    private Object[] lastValuesEventNew;

    private EventBean lastNewEvent;

    /**
     * Constructor requires the name of the field to use in the parent view to compute the weighted average on,
     * as well as the name of the field in the parent view to get the weight from.
     * @param fieldNameX is the name of the field within the parent view to use to get numeric data points for this view to
     * compute the average for.
     * @param fieldNameWeight is the field name for the weight to apply to each data point
     * @param statementContext contains required view services
     */
    public WeightedAverageView(StatementContext statementContext, ExprNode fieldNameX, ExprNode fieldNameWeight, EventType eventType, StatViewAdditionalProps additionalProps)
    {
        this.fieldNameX = fieldNameX;
        this.fieldNameXEvaluator = fieldNameX.getExprEvaluator();
        this.fieldNameWeight = fieldNameWeight;
        this.fieldNameWeightEvaluator = fieldNameWeight.getExprEvaluator();
        this.statementContext = statementContext;
        this.eventType = eventType;
        this.additionalProps = additionalProps;
    }

    public View cloneView(StatementContext statementContext)
    {
        return new WeightedAverageView(statementContext, fieldNameX, fieldNameWeight, eventType, additionalProps);
    }

    /**
     * Returns the expression supplying the X values.
     * @return expression supplying X data points
     */
    public final ExprNode getFieldNameX()
    {
        return fieldNameX;
    }

    /**
     * Returns the expression supplying the weight values.
     * @return expression supplying weight
     */
    public final ExprNode getFieldNameWeight()
    {
        return fieldNameWeight;
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        double oldValue = currentValue;

        // add data points to the bean
        if (newData != null)
        {
            for (int i = 0; i < newData.length; i++)
            {
                eventsPerStream[0] = newData[i];
                Number pointnum = (Number) fieldNameXEvaluator.evaluate(eventsPerStream, true, statementContext);
                Number weightnum = (Number) fieldNameWeightEvaluator.evaluate(eventsPerStream, true, statementContext);
                if (pointnum != null && weightnum != null) {
                    double point = pointnum.doubleValue();
                    double weight = weightnum.doubleValue();

                    if (Double.valueOf(sumXtimesW).isNaN())
                    {
                        sumXtimesW = point * weight;
                        sumW = weight;
                    }
                    else
                    {
                        sumXtimesW += point * weight;
                        sumW += weight;
                    }
                }
            }

            if ((additionalProps != null) && (newData.length != 0)) {
                if (lastValuesEventNew == null) {
                    lastValuesEventNew = new Object[additionalProps.getAdditionalExpr().length];
                }
                for (int val = 0; val < additionalProps.getAdditionalExpr().length; val++) {
                    lastValuesEventNew[val] = additionalProps.getAdditionalExpr()[val].evaluate(eventsPerStream, true, statementContext);
                }
            }
        }

        // remove data points from the bean
        if (oldData != null)
        {
            for (int i = 0; i < oldData.length; i++)
            {
                eventsPerStream[0] = oldData[i];
                Number pointnum = (Number) fieldNameXEvaluator.evaluate(eventsPerStream, true, statementContext);
                Number weightnum = (Number) fieldNameWeightEvaluator.evaluate(eventsPerStream, true, statementContext);

                if (pointnum != null && weightnum != null) {
                    double point = pointnum.doubleValue();
                    double weight = weightnum.doubleValue();
                    sumXtimesW -= point * weight;
                    sumW -= weight;
                }
            }
        }

        if (sumW != 0)
        {
            currentValue = sumXtimesW / sumW;
        }
        else
        {
            currentValue = Double.NaN;
        }

        // If there are child view, fireStatementStopped update method
        if (this.hasViews())
        {
            Map<String, Object> newDataMap = new HashMap<String, Object>();
            newDataMap.put(ViewFieldEnum.WEIGHTED_AVERAGE__AVERAGE.getName(), currentValue);
            addProperties(newDataMap);
            EventBean newDataEvent = statementContext.getEventAdapterService().adaptorForTypedMap(newDataMap, eventType);

            if (lastNewEvent == null)
            {
                Map<String, Object> oldDataMap = new HashMap<String, Object>();
                oldDataMap.put(ViewFieldEnum.WEIGHTED_AVERAGE__AVERAGE.getName(), oldValue);
                EventBean oldDataEvent = statementContext.getEventAdapterService().adaptorForTypedMap(oldDataMap, eventType);

                updateChildren(new EventBean[] {newDataEvent}, new EventBean[] {oldDataEvent});
            }
            else
            {
                updateChildren(new EventBean[] {newDataEvent}, new EventBean[] {lastNewEvent});
            }
            lastNewEvent = newDataEvent;
        }
    }

    private void addProperties(Map<String, Object> newDataMap)
    {
        if (additionalProps == null) {
            return;
        }
        additionalProps.addProperties(newDataMap, lastValuesEventNew);
    }

    public final EventType getEventType()
    {
        return eventType;
    }

    public final Iterator<EventBean> iterator()
    {
        Map<String, Object> newDataMap = new HashMap<String, Object>();
        newDataMap.put(ViewFieldEnum.WEIGHTED_AVERAGE__AVERAGE.getName(), currentValue);
        addProperties(newDataMap);
        return new SingleEventIterator(statementContext.getEventAdapterService().adaptorForTypedMap(newDataMap, eventType));
    }

    public final String toString()
    {
        return this.getClass().getName() +
                " fieldName=" + fieldNameX +
                " fieldNameWeight=" + fieldNameWeight;
    }

    /**
     * Creates the event type for this view.
     * @param statementContext is the event adapter service
     * @return event type of view
     */
    public static EventType createEventType(StatementContext statementContext, StatViewAdditionalProps additionalProps)
    {
        Map<String, Object> schemaMap = new HashMap<String, Object>();
        schemaMap.put(ViewFieldEnum.WEIGHTED_AVERAGE__AVERAGE.getName(), Double.class);
        StatViewAdditionalProps.addCheckDupProperties(schemaMap, additionalProps, ViewFieldEnum.WEIGHTED_AVERAGE__AVERAGE);
        return statementContext.getEventAdapterService().createAnonymousMapType(schemaMap);
    }
}
