package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.event.vaevent.ValueAddEventProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class EvalInsertNoWildcard extends EvalBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalInsertNoWildcard.class);

    public EvalInsertNoWildcard(SelectExprContext selectExprContext, EventType resultEventType) {
        super(selectExprContext, resultEventType);
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize) {
        return super.getEventAdapterService().adaptorForTypedMap(props, super.getResultEventType());
    }
}
