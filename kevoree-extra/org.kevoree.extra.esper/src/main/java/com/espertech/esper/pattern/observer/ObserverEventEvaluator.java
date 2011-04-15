/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern.observer;

import com.espertech.esper.pattern.*;

/**
 * For use by {@link EventObserver} instances to place an event for processing/evaluation.
 */
public interface ObserverEventEvaluator
{
    /**
     * Indicate an event for evaluation (sub-expression the observer represents has turned true).
     * @param matchEvent is the matched events so far
     */
    public void observerEvaluateTrue(MatchedEventMap matchEvent);

    /**
     * Indicate that the observer turned permanently false.
     */
    public void observerEvaluateFalse();

    public PatternContext getContext();
}
