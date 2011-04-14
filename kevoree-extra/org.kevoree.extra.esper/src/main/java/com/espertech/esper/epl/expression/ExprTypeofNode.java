/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.event.vaevent.VariantEvent;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Map;

/**
 * Represents the TYPEOF(a) function is an expression tree.
 */
public class ExprTypeofNode extends ExprNode
{
    private static final long serialVersionUID = -612634538694877204L;
    private transient ExprEvaluator evaluator;

    /**
     * Ctor.
     */
    public ExprTypeofNode()
    {
    }

    public ExprEvaluator getExprEvaluator()
    {
        return evaluator;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 1)
        {
            throw new ExprValidationException("Typeof node must have 1 child expression node supplying the expression to test");
        }

        if (this.getChildNodes().get(0) instanceof ExprStreamUnderlyingNode) {
            ExprStreamUnderlyingNode stream = (ExprStreamUnderlyingNode) getChildNodes().get(0);
            evaluator = new StreamEventTypeEval(stream.getStreamId());
            return;            
        }

        if (this.getChildNodes().get(0) instanceof ExprIdentNode) {
            ExprIdentNode ident = (ExprIdentNode) getChildNodes().get(0);
            int streamNum = streamTypeService.getStreamNumForStreamName(ident.getFullUnresolvedName());
            if (streamNum != -1) {
                evaluator = new StreamEventTypeEval(streamNum);
                return;
            }

            EventType eventType = streamTypeService.getEventTypes()[ident.getStreamId()];
            if (eventType.getFragmentType(ident.getResolvedPropertyName()) != null) {
                evaluator = new FragmentTypeEval(ident.getStreamId(), eventType, ident.getResolvedPropertyName());
                return;
            }
        }

        evaluator = new InnerEvaluator(this.getChildNodes().get(0).getExprEvaluator());
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return String.class;
    }

    /*
    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (streamNum != -1) {
        }

        if (getter != null) {
            getter.getFragment()

        }
        

    }
        */

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("typeof(");
        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        return node instanceof ExprTypeofNode;
    }

    public static class StreamEventTypeEval implements ExprEvaluator {
        private final int streamNum;

        public StreamEventTypeEval(int streamNum) {
            this.streamNum = streamNum;
        }

        @Override
        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
            EventBean event = eventsPerStream[streamNum];
            if (event == null) {
                return null;
            }
            if (event instanceof VariantEvent) {
                return ((VariantEvent) event).getUnderlyingEventBean().getEventType().getName();
            }
            return event.getEventType().getName();
        }

        @Override
        public Class getType() {
            return String.class;
        }

        @Override
        public Map<String, Object> getEventType() throws ExprValidationException {
            return null;
        }
    }

    public static class FragmentTypeEval implements ExprEvaluator {

        private final int streamId;
        private final EventPropertyGetter getter;
        private final String fragmentType;

        public FragmentTypeEval(int streamId, EventType eventType, String resolvedPropertyName) {
            this.streamId = streamId;
            getter = eventType.getGetter(resolvedPropertyName);
            fragmentType = eventType.getFragmentType(resolvedPropertyName).getFragmentType().getName();
        }

        @Override
        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
            EventBean event = eventsPerStream[streamId];
            if (event == null) {
                return null;
            }
            Object fragment = getter.getFragment(event);
            if (fragment == null) {
                return null;
            }
            if (fragment instanceof EventBean) {
                return ((EventBean) fragment).getEventType().getName();
            }
            if (fragment.getClass().isArray()) {
                return fragmentType + "[]";
            }
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Class getType() {
            return String.class;
        }

        @Override
        public Map<String, Object> getEventType() throws ExprValidationException {
            return null;
        }
    }

    private static class InnerEvaluator implements ExprEvaluator {
        private final ExprEvaluator evaluator;

        public InnerEvaluator(ExprEvaluator evaluator) {
            this.evaluator = evaluator;
        }

        @Override
        public Map<String, Object> getEventType() throws ExprValidationException {
            return null;
        }

        @Override
        public Class getType() {
            return String.class;
        }

        @Override
        public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
            Object result = evaluator.evaluate(eventsPerStream, isNewData, context);
            if (result == null)
            {
                return null;
            }
            return result.getClass().getSimpleName();
        }
    }
}
