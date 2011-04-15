/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.epl.core.MethodResolutionService;

/**
 * Aggregator to return the Nth oldest element to enter, with N=1 the most recent
 * value is returned. If N is larger than the enter minus leave size, null is returned.
 * A maximum N historical values are stored, so it can be safely used to compare
 * recent values in large views without incurring excessive overhead.
 */
public class NthAggregator implements AggregationMethod {

    private final Class returnType;
    private final int sizeBuf;

    private Object[] circularBuffer;
    private int currentBufferElementPointer;
    private long numDataPoints;

    /**
     * Ctor.
     * @param returnType return type
     * @param sizeBuf size
     */
    public NthAggregator(Class returnType, int sizeBuf) {
        this.returnType = returnType;
        this.sizeBuf = sizeBuf;
    }

    public void enter(Object value) {
        Object[] arr = (Object[]) value;
        numDataPoints++;
        if (circularBuffer == null)
        {
            clear();
        }
        circularBuffer[currentBufferElementPointer] = arr[0];
        currentBufferElementPointer = (currentBufferElementPointer + 1) % sizeBuf;
    }

    public void leave(Object value) {
        if (sizeBuf > numDataPoints) {
            final int diff = sizeBuf - (int) numDataPoints;
            circularBuffer[(currentBufferElementPointer + diff - 1) % sizeBuf] = null;
        }
        numDataPoints--;
    }

    public Class getValueType() {
        return returnType;
    }

    public Object getValue() {
        return circularBuffer[(currentBufferElementPointer + sizeBuf) % sizeBuf];
    }

    public void clear() {
        circularBuffer = new Object[sizeBuf];
        numDataPoints = 0;
        currentBufferElementPointer = 0;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService) {
        return methodResolutionService.makeNthAggregator(returnType, sizeBuf);
    }
}