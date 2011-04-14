package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.annotation.Drop;
import com.espertech.esper.client.annotation.Priority;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.spec.OnTriggerSetAssignment;
import com.espertech.esper.epl.spec.UpdateDesc;
import com.espertech.esper.event.EventBeanCopyMethod;
import com.espertech.esper.event.EventBeanWriter;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.util.NullableObject;
import com.espertech.esper.util.TypeWidener;
import com.espertech.esper.util.TypeWidenerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routing implementation that allows to pre-process events.
 */
public class InternalEventRouterImpl implements InternalEventRouter
{
    private static final Log log = LogFactory.getLog(InternalEventRouterImpl.class);

    private boolean hasPreprocessing = false;
    private final ConcurrentHashMap<EventType, NullableObject<InternalEventRouterPreprocessor>> preprocessors;
    private final Map<UpdateDesc, IRDescEntry> descriptors;

    /**
     * Ctor.
     */
    public InternalEventRouterImpl()
    {
        this.preprocessors = new ConcurrentHashMap<EventType, NullableObject<InternalEventRouterPreprocessor>>();
        this.descriptors = new LinkedHashMap<UpdateDesc, IRDescEntry>();
    }

    /**
     * Return true to indicate that there is pre-processing to take place.
     * @return preprocessing indicator
     */
    public boolean isHasPreprocessing()
    {
        return hasPreprocessing;
    }

    /**
     * Pre-process the event.
     * @param eventBean to preprocess
     * @param exprEvaluatorContext expression evaluation context
     * @return preprocessed event
     */
    public EventBean preprocess(EventBean eventBean, ExprEvaluatorContext exprEvaluatorContext)
    {
        return getPreprocessedEvent(eventBean, exprEvaluatorContext);
    }

    public void route(EventBean event, EPStatementHandle statementHandle, InternalEventRouteDest routeDest, ExprEvaluatorContext exprEvaluatorContext, boolean addToFront)
    {
        if (!hasPreprocessing)
        {
            routeDest.route(event, statementHandle, addToFront);
            return;
        }

        EventBean preprocessed = getPreprocessedEvent(event, exprEvaluatorContext);
        if (preprocessed != null)
        {
            routeDest.route(preprocessed, statementHandle, addToFront);
        }
    }

    public void addPreprocessing(EventType eventType, UpdateDesc desc, Annotation[] annotations, InternalRoutePreprocessView outputView)
            throws ExprValidationException
    {
        if (log.isInfoEnabled())
        {
            log.info("Adding route preprocessing for type '" + eventType.getName() + "'");
        }

        if (!(eventType instanceof EventTypeSPI))
        {
            throw new ExprValidationException("Update statements require the event type to implement the " + EventTypeSPI.class + " interface");
        }
        EventTypeSPI eventTypeSPI = (EventTypeSPI) eventType;

        TypeWidener[] wideners = new TypeWidener[desc.getAssignments().size()];
        List<String> properties = new ArrayList<String>();
        for (int i = 0; i < desc.getAssignments().size(); i++)
        {
            OnTriggerSetAssignment assignment = desc.getAssignments().get(i);
            EventPropertyDescriptor writableProperty = eventTypeSPI.getWritableProperty(assignment.getVariableName());

            if (writableProperty == null)
            {
                throw new ExprValidationException("Property '" + assignment.getVariableName() + "' is not available for write access");
            }

            wideners[i] = TypeWidenerFactory.getCheckPropertyAssignType(assignment.getExpression().toExpressionString(), assignment.getExpression().getExprEvaluator().getType(),
                    writableProperty.getPropertyType(), assignment.getVariableName());
            properties.add(assignment.getVariableName());
        }

        // check copy-able
        EventBeanCopyMethod copyMethod = eventTypeSPI.getCopyMethod(properties.toArray(new String[properties.size()]));
        if (copyMethod == null)
        {
            throw new ExprValidationException("The update-clause requires the underlying event representation to support copy (via Serializable by default)");
        }

        descriptors.put(desc, new IRDescEntry(eventType, annotations, wideners, outputView));

        // remove all preprocessors for this type as well as any known child types, forcing re-init on next use
        removePreprocessors(eventType);

        hasPreprocessing = true;
    }

    public void removePreprocessing(EventType eventType, UpdateDesc desc)
    {
        if (log.isInfoEnabled())
        {
            log.info("Removing route preprocessing for type '" + eventType.getName());
        }

        // remove all preprocessors for this type as well as any known child types
        removePreprocessors(eventType);

        descriptors.remove(desc);
        if (descriptors.isEmpty())
        {
            hasPreprocessing = false;
            preprocessors.clear();
        }
    }

    private EventBean getPreprocessedEvent(EventBean event, ExprEvaluatorContext exprEvaluatorContext)
    {
        NullableObject<InternalEventRouterPreprocessor> processor = preprocessors.get(event.getEventType());
        if (processor == null)
        {
            synchronized (this)
            {
                processor = initialize(event.getEventType());
                preprocessors.put(event.getEventType(), processor);
            }
        }

        if (processor.getObject() == null)
        {
            return event;
        }
        else
        {
            return processor.getObject().process(event, exprEvaluatorContext);
        }
    }

    private void removePreprocessors(EventType eventType)
    {
        preprocessors.remove(eventType);

        // find each child type entry
        for (EventType type : preprocessors.keySet())
        {
            if (type.getDeepSuperTypes() != null)
            {
                for (Iterator<EventType> it = type.getDeepSuperTypes(); it.hasNext();)
                {
                    if (it.next() == eventType)
                    {
                        preprocessors.remove(type);
                    }
                }
            }
        }
    }

    private NullableObject<InternalEventRouterPreprocessor> initialize(EventType eventType)
    {
        EventTypeSPI eventTypeSPI = (EventTypeSPI) eventType;
        List<InternalEventRouterEntry> desc = new ArrayList<InternalEventRouterEntry>();

        // determine which ones to process for this types, and what priority and drop
        Set<String> eventPropertiesWritten = new HashSet<String>();
        for (Map.Entry<UpdateDesc, IRDescEntry> entry : descriptors.entrySet())
        {
            boolean applicable = entry.getValue().getEventType() == eventType;
            if (!applicable)
            {
                if (eventType.getDeepSuperTypes() != null)
                {
                    for (Iterator<EventType> it = eventType.getDeepSuperTypes(); it.hasNext();)
                    {
                        if (it.next() == entry.getValue().getEventType())
                        {
                            applicable = true;
                            break;
                        }
                    }
                }
            }

            if (!applicable)
            {
                continue;
            }

            int priority = 0;
            boolean isDrop = false;
            Annotation[] annotations = entry.getValue().getAnnotations();
            for (int i = 0; i < annotations.length; i++)
            {
                if (annotations[i] instanceof Priority)
                {
                    priority = ((Priority) annotations[i]).value();
                }
                if (annotations[i] instanceof Drop)
                {
                    isDrop = true;
                }
            }

            List<String> properties = new ArrayList<String>();
            ExprNode[] expressions = new ExprNode[entry.getKey().getAssignments().size()];
            for (int i = 0; i < entry.getKey().getAssignments().size(); i++)
            {
                OnTriggerSetAssignment assignment = entry.getKey().getAssignments().get(i);
                expressions[i] = assignment.getExpression();
                properties.add(assignment.getVariableName());
                eventPropertiesWritten.add(assignment.getVariableName());
            }
            EventBeanWriter writer = eventTypeSPI.getWriter(properties.toArray(new String[properties.size()]));
            desc.add(new InternalEventRouterEntry(priority, isDrop, entry.getKey().getOptionalWhereClause(), expressions, writer, entry.getValue().getWideners(), entry.getValue().getOutputView()));
        }

        EventBeanCopyMethod copyMethod = eventTypeSPI.getCopyMethod(eventPropertiesWritten.toArray(new String[eventPropertiesWritten.size()]));
        if (copyMethod == null)
        {
            return new NullableObject<InternalEventRouterPreprocessor>(null);
        }
        return new NullableObject<InternalEventRouterPreprocessor>(new InternalEventRouterPreprocessor(copyMethod, desc));
    }

    private static class IRDescEntry
    {
        private EventType eventType;
        private Annotation[] annotations;
        private TypeWidener[] wideners;
        private InternalRoutePreprocessView outputView;

        public IRDescEntry(EventType eventType, Annotation[] annotations, TypeWidener[] wideners, InternalRoutePreprocessView outputView)
        {
            this.eventType = eventType;
            this.annotations = annotations;
            this.wideners = wideners;
            this.outputView = outputView;
        }

        public EventType getEventType()
        {
            return eventType;
        }

        public Annotation[] getAnnotations()
        {
            return annotations;
        }

        public TypeWidener[] getWideners()
        {
            return wideners;
        }

        public InternalRoutePreprocessView getOutputView() {
            return outputView;
        }
    }
}
