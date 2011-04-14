package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprJoinWildcardProcessor;
import com.espertech.esper.epl.core.SelectExprProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class EvalSelectWildcardJoin extends EvalBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalSelectWildcardJoin.class);

    private final SelectExprJoinWildcardProcessor joinWildcardProcessor;

    public EvalSelectWildcardJoin(SelectExprContext selectExprContext, EventType resultEventType, SelectExprJoinWildcardProcessor joinWildcardProcessor) {
        super(selectExprContext, resultEventType);
        this.joinWildcardProcessor = joinWildcardProcessor;
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        EventBean event = joinWildcardProcessor.process(eventsPerStream, isNewData, isSynthesize);

        // Using a wrapper bean since we cannot use the same event type else same-type filters match.
        // Wrapping it even when not adding properties is very inexpensive.
        return super.getEventAdapterService().adaptorForTypedWrapper(event, props, super.getResultEventType());
    }
}