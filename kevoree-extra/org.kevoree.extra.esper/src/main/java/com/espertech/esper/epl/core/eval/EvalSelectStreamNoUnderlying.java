package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.spec.SelectClauseStreamCompiledSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class EvalSelectStreamNoUnderlying extends EvalSelectStreamBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalSelectStreamNoUnderlying.class);

    public EvalSelectStreamNoUnderlying(SelectExprContext selectExprContext, EventType resultEventType, List<SelectClauseStreamCompiledSpec> namedStreams, boolean usingWildcard) {
        super(selectExprContext, resultEventType, namedStreams, usingWildcard);
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream)
    {
        return super.getSelectExprContext().getEventAdapterService().adaptorForTypedMap(props, super.getResultEventType());
    }
}