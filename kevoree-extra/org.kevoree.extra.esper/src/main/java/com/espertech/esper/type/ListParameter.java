/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.type;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;

/**
 * Represents a list of values in a set of numeric parameters.
 */
public class ListParameter implements NumberSetParameter
{
    private List<NumberSetParameter> parameters;
    private static final long serialVersionUID = 34502518196561940L;

    /**
     * Ctor.
     */
    public ListParameter()
    {
        this.parameters = new LinkedList<NumberSetParameter>();
    }

    /**
     * Ctor.
     * @param parameters parameters in list
     */
    public ListParameter(List<NumberSetParameter> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Add to the list a further parameter.
     * @param numberSetParameter is the parameter to add
     */
    public void add(NumberSetParameter numberSetParameter)
    {
        parameters.add(numberSetParameter);
    }

    /**
     * Returns list of parameters.
     * @return list of parameters
     */
    public List<NumberSetParameter> getParameters()
    {
        return parameters;
    }

    public boolean isWildcard(int min, int max)
    {
        for (NumberSetParameter param : parameters)
        {
            if (param.isWildcard(min, max))
            {
                return true;
            }
        }
        return false;
    }

    public Set<Integer> getValuesInRange(int min, int max)
    {
        Set<Integer> result = new HashSet<Integer>();

        for (NumberSetParameter param : parameters)
        {
            result.addAll(param.getValuesInRange(min, max));
        }

        return result;
    }
}
