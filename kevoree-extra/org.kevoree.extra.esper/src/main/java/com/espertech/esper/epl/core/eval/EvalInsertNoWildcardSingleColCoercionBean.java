package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;

public class EvalInsertNoWildcardSingleColCoercionBean extends EvalBaseFirstProp implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalInsertNoWildcardSingleColCoercionBean.class);

    public EvalInsertNoWildcardSingleColCoercionBean(SelectExprContext selectExprContext, EventType resultEventType) {
        super(selectExprContext, resultEventType);
    }

    public EventBean processFirstCol(Object result) {
        EventBean wrappedEvent = super.getEventAdapterService().adapterForBean(result);
        return super.getEventAdapterService().adaptorForTypedWrapper(wrappedEvent, Collections.EMPTY_MAP, super.getResultEventType());
    }
}