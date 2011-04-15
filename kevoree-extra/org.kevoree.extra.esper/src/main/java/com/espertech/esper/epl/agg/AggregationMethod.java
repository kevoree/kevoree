/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.epl.core.MethodResolutionService;

/**
 * Maintains aggregation state applying values as entering and leaving the state.
 * <P>Implementations must also act as a factory for further independent copies of aggregation states such that
 * new aggregation state holders and be created from a prototype.
 */
public interface AggregationMethod
{
    /**
     * Apply the value as entering aggregation (entering window).
     * <p>The value can be null since 'null' values may be counted as unique separate values.
     * @param value to add to aggregate
     */
    public void enter(Object value);

    /**
     * Apply the value as leaving aggregation (leaving window).
     * <p>The value can be null since 'null' values may be counted as unique separate values.
     * @param value to remove from aggregate
     */
    public void leave(Object value);

    /**
     * Returns the current value held.
     * @return current value
     */
    public Object getValue();

    /**
     * Returns the type of the current value.
     * @return type of values held
     */
    public Class getValueType();

    /**
     * Clear out the collection.
     */
    public void clear();

    /**
     * Make a new, initalized aggregation state.
     * @param methodResolutionService for use in creating new aggregation method instances as a factory
     * @return initialized copy of the aggregator
     */
    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService);
}
