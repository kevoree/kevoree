package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprJoinWildcardProcessor;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.event.vaevent.ValueAddEventProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class EvalInsertWildcardRevision extends EvalBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalInsertWildcardRevision.class);

    private final ValueAddEventProcessor vaeProcessor;

    public EvalInsertWildcardRevision(SelectExprContext selectExprContext, EventType resultEventType, ValueAddEventProcessor vaeProcessor) {
        super(selectExprContext, resultEventType);
        this.vaeProcessor = vaeProcessor;
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        EventBean event = eventsPerStream[0];
        return vaeProcessor.getValueAddEventBean(event);
    }
}