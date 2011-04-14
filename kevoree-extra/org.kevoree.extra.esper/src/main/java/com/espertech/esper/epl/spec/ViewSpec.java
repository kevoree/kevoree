/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.expression.ExprNode;

import java.util.List;

/**
 * Specification for a view object consists of a namespace, name and view object parameters. 
 */
public final class ViewSpec extends ObjectSpec
{
    private static final long serialVersionUID = -2881179463072647071L;

    /**
     * Constructor.
     * @param namespace if the namespace the object is in
     * @param objectName is the name of the object
     * @param viewParameters is a list of expressions representing the view parameters
     */
    public ViewSpec(String namespace, String objectName, List<ExprNode> viewParameters)
    {
        super(namespace, objectName, viewParameters);
    }
}
