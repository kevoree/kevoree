/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import java.io.Serializable;

/**
 * This interface represents one filter parameter in an {@link FilterValueSet} filter specification.
 * <p> Each filtering parameter has an property name and operator type, and a value to filter for.
 */
public interface FilterValueSetParam extends Serializable
{
    /**
     * Returns the property name for the filter parameter.
     * @return property name
     */
    public String getPropertyName();

    /**
     * Returns the filter operator type.
     * @return filter operator type
     */
    public FilterOperator getFilterOperator();

    /**
     * Return the filter parameter constant to filter for.
     * @return filter parameter constant's value
     */
    public Object getFilterForValue();
}
