/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

import java.io.Serializable;

/**
 * Configuration information for plugging in a custom aggregation function.
 */
public class ConfigurationPlugInAggregationFunction implements Serializable
{
    private String name;
    private String functionClassName;
    private static final long serialVersionUID = 4096734947283212246L;

    /**
     * Ctor.
     */
    public ConfigurationPlugInAggregationFunction()
    {
    }

    /**
     * Returns the aggregation function name.
     * @return aggregation function name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the aggregation function name.
     * @param name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the aggregation function name.
     * @return name
     */
    public String getFunctionClassName()
    {
        return functionClassName;
    }

    /**
     * Sets the aggregation function's implementation class name.
     * @param functionClassName is the implementation class name
     */
    public void setFunctionClassName(String functionClassName)
    {
        this.functionClassName = functionClassName;
    }
}
