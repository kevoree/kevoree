package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.MappedEventBean;

public class EvalInsertMapTypeCoercion implements SelectExprProcessor {

    private EventType resultEventType;
    private EventAdapterService eventAdapterService;

    public EvalInsertMapTypeCoercion(EventType resultEventType, EventAdapterService eventAdapterService) {
        this.resultEventType = resultEventType;
        this.eventAdapterService = eventAdapterService;
    }

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize) {
        MappedEventBean event = (MappedEventBean) eventsPerStream[0];
        return eventAdapterService.adaptorForTypedMap(event.getProperties(), resultEventType);
    }

    public EventType getResultEventType() {
        return resultEventType;
    }
}
