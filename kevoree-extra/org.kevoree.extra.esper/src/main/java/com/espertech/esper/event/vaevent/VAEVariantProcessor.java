/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.vaevent;

import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.EventTypeMetadata;
import com.espertech.esper.epl.named.NamedWindowRootView;
import com.espertech.esper.epl.named.NamedWindowIndexRepository;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.view.Viewable;
import com.espertech.esper.client.ConfigurationVariantStream;

import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a variant event stream, allowing events of disparate event types to be treated polymophically.
 */
public class VAEVariantProcessor implements ValueAddEventProcessor
{
    /**
     * Specification for the variant stream.
     */
    protected final VariantSpec variantSpec;

    /**
     * The event type representing the variant stream.
     */
    protected VariantEventType variantEventType;

    /**
     * Ctor.
     * @param variantSpec specifies how to handle the disparate events
     */
    public VAEVariantProcessor(VariantSpec variantSpec)
    {
        this.variantSpec = variantSpec;

        VariantPropResolutionStrategy strategy;
        if (variantSpec.getTypeVariance() == ConfigurationVariantStream.TypeVariance.ANY)
        {
            strategy = new VariantPropResolutionStrategyAny(variantSpec);
        }
        else
        {
            strategy = new VariantPropResolutionStrategyDefault(variantSpec);
        }

        EventTypeMetadata metadata = EventTypeMetadata.createValueAdd(variantSpec.getVariantStreamName(), EventTypeMetadata.TypeClass.VARIANT);
        variantEventType = new VariantEventType(metadata, variantSpec, strategy);
    }

    public EventType getValueAddEventType()
    {
        return variantEventType;
    }

    public void validateEventType(EventType eventType) throws ExprValidationException
    {
        if (variantSpec.getTypeVariance() == ConfigurationVariantStream.TypeVariance.ANY)
        {
            return;
        }

        if (eventType == null)
        {
            throw new ExprValidationException(getMessage());
        }

        // try each permitted type
        for (EventType variant : variantSpec.getEventTypes())
        {
            if (variant == eventType)
            {
                return;
            }
        }

        // test if any of the supertypes of the eventtype is a variant type
        for (EventType variant : variantSpec.getEventTypes())
        {
            // Check all the supertypes to see if one of the matches the full or delta types
            Iterator<EventType> deepSupers = eventType.getDeepSuperTypes();
            if (deepSupers == null)
            {
                continue;
            }

            EventType superType;
            for (;deepSupers.hasNext();)
            {
                superType = deepSupers.next();
                if (superType == variant)
                {
                    return;
                }
            }
        }

        throw new ExprValidationException(getMessage());
    }

    public EventBean getValueAddEventBean(EventBean event)
    {
        return new VariantEventBean(variantEventType, event);
    }

    public void onUpdate(EventBean[] newData, EventBean[] oldData, NamedWindowRootView namedWindowRootView, NamedWindowIndexRepository indexRepository)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<EventBean> getSnapshot(EPStatementHandle createWindowStmtHandle, Viewable parent)
    {
        throw new UnsupportedOperationException();
    }

    public void removeOldData(EventBean[] oldData, NamedWindowIndexRepository indexRepository)
    {
        throw new UnsupportedOperationException();
    }

    private String getMessage()
    {
        return "Selected event type is not a valid event type of the variant stream '" + variantSpec.getVariantStreamName() + "'";
    }
}
