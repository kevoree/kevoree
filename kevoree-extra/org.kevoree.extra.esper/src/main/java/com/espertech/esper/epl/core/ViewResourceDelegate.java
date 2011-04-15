/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.view.ViewCapability;
import com.espertech.esper.epl.expression.ExprValidationException;

/**
 * Service to expression nodes for indicating view resource requirements.
 */
public interface ViewResourceDelegate
{
    /**
     * Request a view resource.
     * @param streamNumber is the stream number to provide the resource
     * @param requestedCabability describes the view capability required
     * @param resourceCallback for the delegate to supply the resource
     * @return true to indicate the resource can be granted
     * @throws ExprValidationException for use by capabilities if a capability cannot be provide or a policy is violated
     */
    public boolean requestCapability(int streamNumber, ViewCapability requestedCabability, ViewResourceCallback resourceCallback)
            throws ExprValidationException;
}
