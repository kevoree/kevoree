/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.client.soda.EPStatementObjectModel;

import java.util.Map;

/**
 * Return result for unmap operators unmapping an intermal statement representation to the SODA object model.
 */
public class StatementSpecUnMapResult
{
    private final EPStatementObjectModel objectModel;
    private final Map<Integer, SubstitutionParameterExpression> indexedParams;

    /**
     * Ctor.
     * @param objectModel of the statement
     * @param indexedParams a map of parameter index and parameter
     */
    public StatementSpecUnMapResult(EPStatementObjectModel objectModel, Map<Integer, SubstitutionParameterExpression> indexedParams)
    {
        this.objectModel = objectModel;
        this.indexedParams = indexedParams;
    }

    /**
     * Returns the object model.
     * @return object model
     */
    public EPStatementObjectModel getObjectModel()
    {
        return objectModel;
    }

    /**
     * Returns the substitution paremeters keyed by the parameter's index.
     * @return map of index and parameter
     */
    public Map<Integer, SubstitutionParameterExpression> getIndexedParams()
    {
        return indexedParams;
    }
}
