package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.event.vaevent.ValueAddEventProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;

public class EvalInsertNoWildcardSingleColCoercionRevisionMap extends EvalBaseFirstProp implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalInsertNoWildcardSingleColCoercionRevisionMap.class);

    private final ValueAddEventProcessor vaeProcessor;
    private final EventType vaeInnerEventType;

    public EvalInsertNoWildcardSingleColCoercionRevisionMap(SelectExprContext selectExprContext, EventType resultEventType, ValueAddEventProcessor vaeProcessor, EventType vaeInnerEventType) {
        super(selectExprContext, resultEventType);
        this.vaeProcessor = vaeProcessor;
        this.vaeInnerEventType = vaeInnerEventType;
    }

    public EventBean processFirstCol(Object result) {
        EventBean wrappedEvent = super.getEventAdapterService().adaptorForTypedMap((Map)result, super.getResultEventType());
        return vaeProcessor.getValueAddEventBean(super.getEventAdapterService().adaptorForTypedWrapper(wrappedEvent, Collections.EMPTY_MAP, vaeInnerEventType));
    }
}