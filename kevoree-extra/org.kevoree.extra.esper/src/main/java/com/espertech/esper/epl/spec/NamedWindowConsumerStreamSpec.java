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
import com.espertech.esper.epl.property.PropertyEvaluator;

import java.util.List;

/**
 * Specification for use of an existing named window.
 */
public class NamedWindowConsumerStreamSpec extends StreamSpecBase implements StreamSpecCompiled
{
    private String windowName;
    private List<ExprNode> filterExpressions;
    private transient PropertyEvaluator optPropertyEvaluator;
    private static final long serialVersionUID = -8549850729310756432L;

    /**
     * Ctor.
     * @param windowName - specifies the name of the named window
     * @param optionalAsName - a name or null if none defined
     * @param viewSpecs - is the view specifications
     * @param filterExpressions - the named window filters
     * @param streamSpecOptions - additional options such as unidirectional stream in a join
     */
    public NamedWindowConsumerStreamSpec(String windowName, String optionalAsName, List<ViewSpec> viewSpecs, List<ExprNode> filterExpressions, StreamSpecOptions streamSpecOptions, PropertyEvaluator optPropertyEvaluator)
    {
        super(optionalAsName, viewSpecs, streamSpecOptions);
        this.windowName = windowName;
        this.filterExpressions = filterExpressions;
        this.optPropertyEvaluator = optPropertyEvaluator;
    }

    /**
     * Returns the window name.
     * @return window name
     */
    public String getWindowName()
    {
        return windowName;
    }

    /**
     * Returns list of filter expressions onto the named window, or no filter expressions if none defined.
     * @return list of filter expressions
     */
    public List<ExprNode> getFilterExpressions()
    {
        return filterExpressions;
    }

    public PropertyEvaluator getOptPropertyEvaluator()
    {
        return optPropertyEvaluator;
    }
}
