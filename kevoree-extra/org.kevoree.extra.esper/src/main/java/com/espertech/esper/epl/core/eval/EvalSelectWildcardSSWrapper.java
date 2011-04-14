package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.event.DecoratingEventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class EvalSelectWildcardSSWrapper extends EvalBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalSelectWildcardSSWrapper.class);

    public EvalSelectWildcardSSWrapper(SelectExprContext selectExprContext, EventType resultEventType) {
        super(selectExprContext, resultEventType);
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        // In case of a wildcard and single stream that is itself a
        // wrapper bean, we also need to add the map properties
        DecoratingEventBean wrapper = (DecoratingEventBean)eventsPerStream[0];
        if(wrapper != null)
        {
            Map<String, Object> map = wrapper.getDecoratingProperties();
            if ((super.getExprNodes().length == 0) && (!map.isEmpty()))
            {
                props = new HashMap<String, Object>(map);
            }
            else
            {
                props.putAll(map);
            }
        }

        EventBean event = eventsPerStream[0];

        // Using a wrapper bean since we cannot use the same event type else same-type filters match.
        // Wrapping it even when not adding properties is very inexpensive.
        return super.getEventAdapterService().adaptorForTypedWrapper(event, props, super.getResultEventType());
    }
}