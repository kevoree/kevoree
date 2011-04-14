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
 * Configuration information for plugging in a custom single-row function.
 */
public class ConfigurationPlugInSingleRowFunction implements Serializable
{
    private String name;
    private String functionClassName;
    private String functionMethodName;
    private static final long serialVersionUID = 4096734947283212246L;

    /**
     * Ctor.
     */
    public ConfigurationPlugInSingleRowFunction()
    {
    }

    /**
     * Returns the single-row function name for use in EPL.
     * @return single-row function name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the single-row function name for use in EPL.
     * @param name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the single-row function name.
     * @return name
     */
    public String getFunctionClassName()
    {
        return functionClassName;
    }

    /**
     * Sets the single-row function's implementation class name.
     * @param functionClassName is the implementation class name
     */
    public void setFunctionClassName(String functionClassName)
    {
        this.functionClassName = functionClassName;
    }

    /**
     * Returns the name of the single-row function.
     * @return function name
     */
    public String getFunctionMethodName()
    {
        return functionMethodName;
    }

    /**
     * Sets the name of the single-row function.
     * @param functionMethodName single-row function name
     */
    public void setFunctionMethodName(String functionMethodName)
    {
        this.functionMethodName = functionMethodName;
    }
}
