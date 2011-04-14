/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import java.util.Map;
import java.util.HashMap;

/**
 * Un-mapping context for mapping from an internal specifications to an SODA object model.
 */
public class StatementSpecUnMapContext
{
    private final Map<Integer, SubstitutionParameterExpression> indexedParams;

    /**
     * Ctor.
     */
    public StatementSpecUnMapContext()
    {
        indexedParams = new HashMap<Integer, SubstitutionParameterExpression>();
    }

    /**
     * Adds a substitution parameters.
     * @param index is the index of the parameter
     * @param subsParam is the parameter expression node
     */
    public void add(int index, SubstitutionParameterExpression subsParam)
    {
        if (indexedParams.containsKey(index))
        {
            throw new IllegalStateException("Index '" + index + "' already found in collection");
        }
        indexedParams.put(index, subsParam);
    }

    /**
     * Returns all indexed parameters.
     * @return map of parameter index and parameter expression node
     */
    public Map<Integer, SubstitutionParameterExpression> getIndexedParams()
    {
        return indexedParams;
    }

    /**
     * Adds all substitution parameters. Checks if indexes already exists
     * and throws an exception if they do.
     * @param inner to indexes and parameters to add
     */
    public void addAll(Map<Integer, SubstitutionParameterExpression> inner)
    {
        for (Map.Entry<Integer, SubstitutionParameterExpression> entry : inner.entrySet())
        {
            if (indexedParams.containsKey(entry.getKey()))
            {
                throw new IllegalStateException("Index '" + entry.getKey() + "' already found in collection");
            }
            indexedParams.put(entry.getKey(), entry.getValue());
        }
    }
}
