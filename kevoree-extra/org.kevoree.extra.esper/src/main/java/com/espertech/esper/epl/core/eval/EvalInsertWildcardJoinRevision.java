package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprJoinWildcardProcessor;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.event.vaevent.ValueAddEventProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class EvalInsertWildcardJoinRevision extends EvalBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalInsertWildcardJoinRevision.class);

    private final SelectExprJoinWildcardProcessor joinWildcardProcessor;
    private final ValueAddEventProcessor vaeProcessor;

    public EvalInsertWildcardJoinRevision(SelectExprContext selectExprContext, EventType resultEventType, SelectExprJoinWildcardProcessor joinWildcardProcessor, ValueAddEventProcessor vaeProcessor) {
        super(selectExprContext, resultEventType);
        this.joinWildcardProcessor = joinWildcardProcessor;
        this.vaeProcessor = vaeProcessor;
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        EventBean event = joinWildcardProcessor.process(eventsPerStream, isNewData, isSynthesize);
        return vaeProcessor.getValueAddEventBean(event);
    }
}