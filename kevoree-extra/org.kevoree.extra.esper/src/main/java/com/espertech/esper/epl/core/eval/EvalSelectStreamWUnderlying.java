package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.spec.SelectClauseStreamCompiledSpec;
import com.espertech.esper.event.DecoratingEventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class EvalSelectStreamWUnderlying extends EvalSelectStreamBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalSelectStreamWUnderlying.class);

    private final List<SelectClauseStreamCompiledSpec> unnamedStreams;
    private final boolean singleStreamWrapper;
    private final boolean underlyingIsFragmentEvent;
    private final int underlyingStreamNumber;
    private final EventPropertyGetter underlyingPropertyEventGetter;

    public EvalSelectStreamWUnderlying(SelectExprContext selectExprContext, EventType resultEventType, List<SelectClauseStreamCompiledSpec> namedStreams, boolean usingWildcard, List<SelectClauseStreamCompiledSpec> unnamedStreams, EventType underlyingEventType, boolean singleStreamWrapper, boolean underlyingIsFragmentEvent, int underlyingStreamNumber, EventPropertyGetter underlyingPropertyEventGetter) {
        super(selectExprContext, resultEventType, namedStreams, usingWildcard);
        this.unnamedStreams = unnamedStreams;
        this.singleStreamWrapper = singleStreamWrapper;
        this.underlyingIsFragmentEvent = underlyingIsFragmentEvent;
        this.underlyingStreamNumber = underlyingStreamNumber;
        this.underlyingPropertyEventGetter = underlyingPropertyEventGetter;
    }

    public EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream)
    {
        // In case of a wildcard and single stream that is itself a
        // wrapper bean, we also need to add the map properties
        if(singleStreamWrapper)
        {
            DecoratingEventBean wrapper = (DecoratingEventBean)eventsPerStream[0];
            if(wrapper != null)
            {
                Map<String, Object> map = wrapper.getDecoratingProperties();
                props.putAll(map);
            }
        }

        EventBean event = null;
        if (underlyingIsFragmentEvent)
        {
            EventBean eventBean = eventsPerStream[underlyingStreamNumber];
            event = (EventBean) eventBean.getFragment(unnamedStreams.get(0).getStreamName());
        }
        else if (underlyingPropertyEventGetter != null)
        {
            Object value = underlyingPropertyEventGetter.get(eventsPerStream[underlyingStreamNumber]);
            if (value != null)
            {
                event = super.getSelectExprContext().getEventAdapterService().adapterForBean(value);
            }
        }
        else
        {
            event = eventsPerStream[underlyingStreamNumber];
        }

        // Using a wrapper bean since we cannot use the same event type else same-type filters match.
        // Wrapping it even when not adding properties is very inexpensive.
        return super.getSelectExprContext().getEventAdapterService().adaptorForTypedWrapper(event, props, super.getResultEventType());
    }
}