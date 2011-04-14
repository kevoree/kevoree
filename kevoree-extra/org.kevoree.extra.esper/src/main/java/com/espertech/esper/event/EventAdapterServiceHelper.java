/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.event.bean.BeanEventType;
import com.espertech.esper.event.bean.EventBeanManufacturerBean;
import com.espertech.esper.event.bean.PropertyHelper;
import com.espertech.esper.event.map.MapEventType;
import com.espertech.esper.util.JavaClassHelper;
import net.sf.cglib.reflect.FastClass;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * Helper for writeable events.
 */
public class EventAdapterServiceHelper
{
    /**
     * Returns descriptors for all writable properties.
     * @param eventType to reflect on
     * @return list of writable properties
     */
    public static Set<WriteablePropertyDescriptor> getWriteableProperties(EventType eventType)
    {
        if (!(eventType instanceof EventTypeSPI))
        {
            return null;
        }
        if (eventType instanceof BeanEventType)
        {
            BeanEventType beanEventType = (BeanEventType) eventType;
            FastClass fastClass = beanEventType.getFastClass();
            return PropertyHelper.getWritableProperties(fastClass.getJavaClass());
        }
        EventTypeSPI typeSPI = (EventTypeSPI) eventType;
        if (!typeSPI.getMetadata().isApplicationConfigured())
        {
            return null;
        }
        if (eventType instanceof MapEventType)
        {
            Map<String, Object> mapdef = ((MapEventType) eventType).getTypes();
            Set<WriteablePropertyDescriptor> writables = new HashSet<WriteablePropertyDescriptor>();
            for (Map.Entry<String, Object> types : mapdef.entrySet())
            {
                if (types.getValue() instanceof Class)
                {
                    writables.add(new WriteablePropertyDescriptor(types.getKey(), (Class) types.getValue(), null));
                }
                if (types.getValue() instanceof String)
                {
                    String typeName = types.getValue().toString();
                    Class clazz = JavaClassHelper.getPrimitiveClassForName(typeName);
                    if (clazz != null)
                    {
                        writables.add(new WriteablePropertyDescriptor(types.getKey(), clazz, null));
                    }                    
                }
            }
            return writables;
        }
        else
        {
            return null;
        }
    }

    /**
     * Return an adapter for the given type of event using the pre-validated object.
     * @param event value object
     * @param eventType type of event
     * @param eventAdapterService service for instances
     * @return event adapter
     */
    public static EventBean adapterForType(Object event, EventType eventType, EventAdapterService eventAdapterService)
    {
        if (event == null) {
            return null;
        }
        if (eventType instanceof BeanEventType)
        {
            return eventAdapterService.adapterForTypedBean(event, (BeanEventType) eventType);
        }
        else if (eventType instanceof MapEventType)
        {
            return eventAdapterService.adaptorForTypedMap((Map) event, eventType);
        }
        else if (eventType instanceof BaseConfigurableEventType)
        {
            return eventAdapterService.adapterForTypedDOM((Node) event, eventType);
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns a factory for creating and populating event object instances for the given type.
     * @param eventType to create underlying objects for
     * @param properties to write
     * @param methodResolutionService for resolving methods
     * @param eventAdapterService fatory for event
     * @return factory
     * @throws EventBeanManufactureException if a factory cannot be created for the type
     */
    public static EventBeanManufacturer getManufacturer(EventAdapterService eventAdapterService, EventType eventType, WriteablePropertyDescriptor[] properties, MethodResolutionService methodResolutionService)
            throws EventBeanManufactureException
    {
        if (!(eventType instanceof EventTypeSPI))
        {
            return null;
        }
        if (eventType instanceof BeanEventType)
        {
            BeanEventType beanEventType = (BeanEventType) eventType;
            return new EventBeanManufacturerBean(beanEventType, eventAdapterService, properties, methodResolutionService);
        }
        EventTypeSPI typeSPI = (EventTypeSPI) eventType;
        if (!typeSPI.getMetadata().isApplicationConfigured())
        {
            return null;
        }
        if (eventType instanceof MapEventType)
        {
            MapEventType mapEventType = (MapEventType) eventType;
            return new EventBeanManufacturerMap(mapEventType, eventAdapterService, properties);
        }
        return null;
    }
}
