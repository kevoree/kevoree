/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.client.*;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.EPRuntimeEventSender;
import com.espertech.esper.core.thread.ThreadingService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.event.bean.BeanEventAdapter;
import com.espertech.esper.event.bean.BeanEventBean;
import com.espertech.esper.event.bean.BeanEventType;
import com.espertech.esper.event.bean.BeanEventTypeFactory;
import com.espertech.esper.event.map.MapEventBean;
import com.espertech.esper.event.map.MapEventType;
import com.espertech.esper.event.xml.*;
import com.espertech.esper.plugin.*;
import com.espertech.esper.util.URIUtil;
import com.espertech.esper.util.UuidGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kevoree.classloader.ClassLoaderInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for resolving event name to event type.
 * <p>
 * The implementation assigned a unique identifier to each event type.
 * For Class-based event types, only one EventType instance and one event type id exists for the same class.
 * <p>
 * Event type names must be unique, that is an name must resolve to a single event type.
 * <p>
 * Each event type can have multiple names defined for it. For example, expressions such as
 * "select * from A" and "select * from B"
 * in which A and B are names for the same class X the select clauses each fireStatementStopped for events of type X.
 * In summary, names A and B point to the same underlying event type and therefore event type id.
 */
public class EventAdapterServiceImpl implements EventAdapterService
{
    private static Log log = LogFactory.getLog(EventAdapterServiceImpl.class);

    private final ConcurrentHashMap<Class, BeanEventType> typesPerJavaBean;
    private final Map<String, EventType> nameToTypeMap;
    private final Map<String, PlugInEventTypeHandler> nameToHandlerMap;
    private BeanEventAdapter beanEventAdapter;
    private Map<String, EventType> xmldomRootElementNames;
    private LinkedHashSet<String> javaPackageNames;
    private final Map<URI, PlugInEventRepresentation> plugInRepresentations;

    /**
     * Ctor.
     */
    public EventAdapterServiceImpl()
    {
        nameToTypeMap = new HashMap<String, EventType>();
        xmldomRootElementNames = new HashMap<String, EventType>();
        javaPackageNames = new LinkedHashSet<String>();
        nameToHandlerMap = new HashMap<String, PlugInEventTypeHandler>();

        // Share the mapping of class to type with the type creation for thread safety
        typesPerJavaBean = new ConcurrentHashMap<Class, BeanEventType>();
        beanEventAdapter = new BeanEventAdapter(typesPerJavaBean, this);
        plugInRepresentations = new HashMap<URI, PlugInEventRepresentation>();
    }

    public Set<WriteablePropertyDescriptor> getWriteableProperties(EventType eventType)
    {
        return EventAdapterServiceHelper.getWriteableProperties(eventType);
    }

    public EventBeanManufacturer getManufacturer(EventType eventType, WriteablePropertyDescriptor[] properties, MethodResolutionService methodResolutionService)
            throws EventBeanManufactureException
    {
        return EventAdapterServiceHelper.getManufacturer(this, eventType, properties, methodResolutionService);
    }

    public EventType[] getAllTypes()
    {
        Collection<EventType> types = nameToTypeMap.values();
        return types.toArray(new EventType[types.size()]);
    }

    public synchronized void addTypeByName(String name, EventType eventType) throws EventAdapterException
    {
        if (nameToTypeMap.containsKey(name))
        {
            throw new EventAdapterException("Event type by name '" + name + "' already exists");
        }
        nameToTypeMap.put(name, eventType);
    }

    public void addEventRepresentation(URI eventRepURI, PlugInEventRepresentation pluginEventRep) throws EventAdapterException
    {
        if (plugInRepresentations.containsKey(eventRepURI))
        {
            throw new EventAdapterException("Plug-in event representation URI by name " + eventRepURI + " already exists");
        }
        plugInRepresentations.put(eventRepURI, pluginEventRep);
    }

    public EventType addPlugInEventType(String eventTypeName, URI[] resolutionURIs, Serializable initializer) throws EventAdapterException
    {
        if (nameToTypeMap.containsKey(eventTypeName))
        {
            throw new EventAdapterException("Event type named '" + eventTypeName +
                    "' has already been declared");
        }

        PlugInEventRepresentation handlingFactory = null;
        URI handledEventTypeURI = null;

        if ((resolutionURIs == null) || (resolutionURIs.length == 0))
        {
            throw new EventAdapterException("Event type named '" + eventTypeName + "' could not be created as" +
                    " no resolution URIs for dynamic resolution of event type names through a plug-in event representation have been defined");
        }

        for (URI eventTypeURI : resolutionURIs)
        {
            // Determine a list of event representations that may handle this type
            Map<URI, Object> allFactories = new HashMap<URI, Object>(plugInRepresentations);
            Collection<Map.Entry<URI, Object>> factories = URIUtil.filterSort(eventTypeURI, allFactories);

            if (factories.isEmpty())
            {
                continue;
            }

            // Ask each in turn to accept the type (the process of resolving the type)
            for (Map.Entry<URI, Object> entry : factories)
            {
                PlugInEventRepresentation factory = (PlugInEventRepresentation) entry.getValue();
                PlugInEventTypeHandlerContext context = new PlugInEventTypeHandlerContext(eventTypeURI, initializer, eventTypeName);
                if (factory.acceptsType(context))
                {
                    handlingFactory = factory;
                    handledEventTypeURI = eventTypeURI;
                    break;
                }
            }

            if (handlingFactory != null)
            {
                break;
            }
        }

        if (handlingFactory == null)
        {
            throw new EventAdapterException("Event type named '" + eventTypeName + "' could not be created as none of the " +
                    "registered plug-in event representations accepts any of the resolution URIs '" + Arrays.toString(resolutionURIs)
                    + "' and initializer");
        }

        PlugInEventTypeHandlerContext context = new PlugInEventTypeHandlerContext(handledEventTypeURI, initializer, eventTypeName);
        PlugInEventTypeHandler handler = handlingFactory.getTypeHandler(context);
        if (handler == null)
        {
            throw new EventAdapterException("Event type named '" + eventTypeName + "' could not be created as no handler was returned");
        }

        EventType eventType = handler.getType();
        nameToTypeMap.put(eventTypeName, eventType);
        nameToHandlerMap.put(eventTypeName, handler);

        return eventType;
    }

    public EventSender getStaticTypeEventSender(EPRuntimeEventSender runtimeEventSender, String eventTypeName, ThreadingService threadingService) throws EventTypeException
    {
        EventType eventType = nameToTypeMap.get(eventTypeName);
        if (eventType == null)
        {
            throw new EventTypeException("Event type named '" + eventTypeName + "' could not be found");
        }

        // handle built-in types
        if (eventType instanceof BeanEventType)
        {
            return new EventSenderBean(runtimeEventSender, (BeanEventType) eventType, this, threadingService);
        }
        if (eventType instanceof MapEventType)
        {
            return new EventSenderMap(runtimeEventSender, (MapEventType) eventType, this, threadingService);
        }
        if (eventType instanceof BaseXMLEventType)
        {
            return new EventSenderXMLDOM(runtimeEventSender, (BaseXMLEventType) eventType, this, threadingService);
        }

        PlugInEventTypeHandler handlers = nameToHandlerMap.get(eventTypeName);
        if (handlers != null)
        {
            return handlers.getSender(runtimeEventSender);
        }
        throw new EventTypeException("An event sender for event type named '" + eventTypeName + "' could not be created as the type is internal");
    }

    public void updateMapEventType(String mapeventTypeName, Map<String, Object> typeMap) throws EventAdapterException
    {
        EventType type = nameToTypeMap.get(mapeventTypeName);
        if (type == null)
        {
            throw new EventAdapterException("Event type named '" + mapeventTypeName + "' has not been declared");
        }
        if (!(type instanceof MapEventType))
        {
            throw new EventAdapterException("Event type by name '" + mapeventTypeName + "' is not a Map event type");
        }

        MapEventType mapEventType = (MapEventType) type;
        mapEventType.addAdditionalProperties(typeMap, this);
    }

    public EventSender getDynamicTypeEventSender(EPRuntimeEventSender epRuntime, URI[] uri, ThreadingService threadingService) throws EventTypeException
    {
        List<EventSenderURIDesc> handlingFactories = new ArrayList<EventSenderURIDesc>();
        for (URI resolutionURI : uri)
        {
            // Determine a list of event representations that may handle this type
            Map<URI, Object> allFactories = new HashMap<URI, Object>(plugInRepresentations);
            Collection<Map.Entry<URI, Object>> factories = URIUtil.filterSort(resolutionURI, allFactories);

            if (factories.isEmpty())
            {
                continue;
            }

            // Ask each in turn to accept the type (the process of resolving the type)
            for (Map.Entry<URI, Object> entry : factories)
            {
                PlugInEventRepresentation factory = (PlugInEventRepresentation) entry.getValue();
                PlugInEventBeanReflectorContext context = new PlugInEventBeanReflectorContext(resolutionURI);
                if (factory.acceptsEventBeanResolution(context))
                {
                    PlugInEventBeanFactory beanFactory = factory.getEventBeanFactory(context);
                    if (beanFactory == null)
                    {
                        log.warn("Plug-in event representation returned a null bean factory, ignoring entry");
                        continue;
                    }
                    EventSenderURIDesc desc = new EventSenderURIDesc(beanFactory, resolutionURI, entry.getKey());
                    handlingFactories.add(desc);
                }
            }
        }

        if (handlingFactories.isEmpty())
        {
            throw new EventTypeException("Event sender for resolution URIs '" + Arrays.toString(uri)
                    + "' did not return at least one event representation's event factory");
        }

        return new EventSenderImpl(handlingFactories, epRuntime, threadingService);
    }

    public BeanEventTypeFactory getBeanEventTypeFactory() {
        return beanEventAdapter;
    }

    /**
     * Set the legacy Java class type information.
     * @param classToLegacyConfigs is the legacy class configs
     */
    public void setClassLegacyConfigs(Map<String, ConfigurationEventTypeLegacy> classToLegacyConfigs)
    {
        beanEventAdapter.setClassToLegacyConfigs(classToLegacyConfigs);
    }

    /**
     * Sets the default property resolution style.
     * @param defaultPropertyResolutionStyle is the default style
     */
    public void setDefaultPropertyResolutionStyle(Configuration.PropertyResolutionStyle defaultPropertyResolutionStyle)
    {
        beanEventAdapter.setDefaultPropertyResolutionStyle(defaultPropertyResolutionStyle);
    }

    public void setDefaultAccessorStyle(ConfigurationEventTypeLegacy.AccessorStyle defaultAccessorStyle)
    {
        beanEventAdapter.setDefaultAccessorStyle(defaultAccessorStyle);
    }

    public EventType getExistsTypeByName(String eventTypeName)
    {
        if (eventTypeName == null)
        {
            throw new IllegalStateException("Null event type name parameter");
        }
        return nameToTypeMap.get(eventTypeName);
    }

    /**
     * Add an name and class as an event type.
     * @param eventTypeName is the name
     * @param clazz is the Java class to add
     * @return event type
     * @throws EventAdapterException to indicate an error constructing the type
     */
    public synchronized EventType addBeanType(String eventTypeName, Class clazz, boolean isPreconfiguredStatic, boolean isPreconfigured, boolean isConfigured) throws EventAdapterException
    {
        if (log.isDebugEnabled())
        {
            log.debug(".addBeanType Adding " + eventTypeName + " for type " + clazz.getName());
        }

        EventType existingType = nameToTypeMap.get(eventTypeName);
        if (existingType != null)
        {
            if (existingType.getUnderlyingType().equals(clazz))
            {
                return existingType;
            }

            throw new EventAdapterException("Event type named '" + eventTypeName +
                    "' has already been declared with differing underlying type information:" + existingType.getUnderlyingType().getName() +
                    " versus " + clazz.getName());
        }

        EventType eventType = beanEventAdapter.createBeanType(eventTypeName, clazz, isPreconfiguredStatic, isPreconfigured, isConfigured);
        nameToTypeMap.put(eventTypeName, eventType);

        return eventType;
    }

    /**
     * Create an event bean given an event of object id.
     * @param event is the event class
     * @return event
     */
    public EventBean adapterForBean(Object event)
    {
        EventType eventType = typesPerJavaBean.get(event.getClass());
        if (eventType == null)
        {
            // This will update the typesPerJavaBean mapping
            eventType = beanEventAdapter.createBeanType(event.getClass().getName(), event.getClass(), false, false, false);
        }
        return new BeanEventBean(event, eventType);
    }

    /**
     * Add an event type for the given Java class name.
     * @param eventTypeName is the name
     * @param fullyQualClassName is the Java class name
     * @return event type
     * @throws EventAdapterException if the Class name cannot resolve or other error occured
     */
    public synchronized EventType addBeanType(String eventTypeName, String fullyQualClassName, boolean considerAutoName, boolean isPreconfiguredStatic, boolean isPreconfigured, boolean isConfigured) throws EventAdapterException
    {
        if (log.isDebugEnabled())
        {
            log.debug(".addBeanType Adding " + eventTypeName + " for type " + fullyQualClassName);
        }

        EventType existingType = nameToTypeMap.get(eventTypeName);
        if (existingType != null)
        {
            if ((existingType.getUnderlyingType().getName().equals(fullyQualClassName)) ||
                (existingType.getUnderlyingType().getSimpleName().equals(fullyQualClassName)))
            {
                if (log.isDebugEnabled())
                {
                    log.debug(".addBeanType Returning existing type for " + eventTypeName);
                }
                return existingType;
            }

            throw new EventAdapterException("Event type named '" + eventTypeName +
                    "' has already been declared with differing underlying type information: Class " + existingType.getUnderlyingType().getName() +
                    " versus " + fullyQualClassName);
        }

        // Try to resolve as a fully-qualified class name first
        Class clazz = null;
        try
        {
                        //ClassLoader cl = Thread.currentThread().getContextClassLoader();
        	clazz  = ClassLoaderInterface.instance.loadClass(fullyQualClassName);
 //           clazz = Class.forName(fullyQualClassName, true, cl);
        }
        catch (ClassNotFoundException ex)
        {
            if (!considerAutoName)
            {
                throw new EventAdapterException("Event type or class named '" + fullyQualClassName + "' was not found", ex);
            }

            // Attempt to resolve from auto-name packages
            for (String javaPackageName : javaPackageNames)
            {
                String generatedClassName = javaPackageName + "." + fullyQualClassName;
                try
                {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Class resolvedClass = Class.forName(generatedClassName, true, cl);
                    if (clazz != null)
                    {
                        throw new EventAdapterException("Failed to resolve name '" + eventTypeName + "', the class was ambigously found both in " +
                                "package '" + clazz.getPackage().getName() + "' and in " +
                                "package '" + resolvedClass.getPackage().getName() + "'" , ex);
                    }
                    clazz = resolvedClass;
                }
                catch (ClassNotFoundException ex1)
                {
                    // expected, class may not exists in all packages
                }
            }
            if (clazz == null)
            {
                throw new EventAdapterException("Event type or class named '" + fullyQualClassName + "' was not found", ex);
            }
        }

        EventType eventType = beanEventAdapter.createBeanType(eventTypeName, clazz, isPreconfiguredStatic, isPreconfigured, isConfigured);
        nameToTypeMap.put(eventTypeName, eventType);

        return eventType;
    }

    public synchronized EventType addNestableMapType(String eventTypeName, Map<String, Object> propertyTypes, Set<String> optionalSuperType, boolean isPreconfiguredStatic, boolean isPreconfigured, boolean isConfigured, boolean namedWindow, boolean insertInto) throws EventAdapterException
    {
        Pair<EventType[], Set<EventType>> mapSuperTypes = getMapSuperTypes(optionalSuperType);
        EventTypeMetadata metadata = EventTypeMetadata.createMapType(eventTypeName, isPreconfiguredStatic, isPreconfigured, isConfigured, namedWindow, insertInto);
        MapEventType newEventType = new MapEventType(metadata, eventTypeName, this, propertyTypes, mapSuperTypes.getFirst(), mapSuperTypes.getSecond());

        EventType existingType = nameToTypeMap.get(eventTypeName);
        if (existingType != null)
        {
            // The existing type must be the same as the type createdStatement
            if (!newEventType.equals(existingType))
            {
                String message = newEventType.getEqualsMessage(existingType);
                throw new EventAdapterException("Event type named '" + eventTypeName +
                        "' has already been declared with differing column name or type information: " + message);
            }

            // Since it's the same, return the existing type
            return existingType;
        }

        nameToTypeMap.put(eventTypeName, newEventType);

        return newEventType;
    }

    public EventBean adapterForMap(Map<String, Object> event, String eventTypeName) throws EventAdapterException
    {
        EventType existingType = nameToTypeMap.get(eventTypeName);
        if (existingType == null)
        {
            throw new EventAdapterException("Event type named '" + eventTypeName + "' has not been defined");
        }

        return adaptorForTypedMap(event, existingType);
    }

    public EventBean adapterForDOM(Node node)
    {
        Node namedNode;
        if (node instanceof Document)
        {
            namedNode = ((Document) node).getDocumentElement();
        }
        else if (node instanceof Element)
        {
            namedNode = node;
        }
        else
        {
            throw new EPException("Unexpected DOM node of type '" + node.getClass() + "' encountered, please supply a Document or Element node");
        }

        String rootElementName = namedNode.getLocalName();
        if (rootElementName == null)
        {
            rootElementName = namedNode.getNodeName();
        }

        EventType eventType = xmldomRootElementNames.get(rootElementName);
        if (eventType == null)
        {
            throw new EventAdapterException("DOM event root element name '" + rootElementName +
                    "' has not been configured");
        }

        return new XMLEventBean(namedNode, eventType);
    }

    public EventBean adapterForTypedDOM(Node node, EventType eventType)
    {
        return new XMLEventBean(node, eventType);
    }

    /**
     * Add a configured XML DOM event type.
     * @param eventTypeName is the name name of the event type
     * @param configurationEventTypeXMLDOM configures the event type schema and namespace and XPath
     * property information.
     */
    public synchronized EventType addXMLDOMType(String eventTypeName, ConfigurationEventTypeXMLDOM configurationEventTypeXMLDOM, SchemaModel optionalSchemaModel, boolean isPreconfiguredStatic)
    {
        return addXMLDOMType(eventTypeName, configurationEventTypeXMLDOM, optionalSchemaModel, isPreconfiguredStatic, false);
    }

    @Override
    public EventType replaceXMLEventType(String xmlEventTypeName, ConfigurationEventTypeXMLDOM config, SchemaModel schemaModel) {
        return addXMLDOMType(xmlEventTypeName, config, schemaModel, false, true);
    }

    /**
     * Add a configured XML DOM event type.
     * @param eventTypeName is the name name of the event type
     * @param configurationEventTypeXMLDOM configures the event type schema and namespace and XPath
     * property information.
     */
    private synchronized EventType addXMLDOMType(String eventTypeName, ConfigurationEventTypeXMLDOM configurationEventTypeXMLDOM, SchemaModel optionalSchemaModel, boolean isPreconfiguredStatic, boolean allowOverrideExisting)
    {
        if (configurationEventTypeXMLDOM.getRootElementName() == null)
        {
            throw new EventAdapterException("Required root element name has not been supplied");
        }

        if (!allowOverrideExisting) {
            EventType existingType = nameToTypeMap.get(eventTypeName);
            if (existingType != null)
            {
                String message = "Event type named '" + eventTypeName + "' has already been declared with differing column name or type information";
                if (!(existingType instanceof BaseXMLEventType))
                {
                    throw new EventAdapterException(message);
                }
                ConfigurationEventTypeXMLDOM config = ((BaseXMLEventType) existingType).getConfigurationEventTypeXMLDOM();
                if (!config.equals(configurationEventTypeXMLDOM))
                {
                    throw new EventAdapterException(message);
                }

                return existingType;
            }
        }

        EventTypeMetadata metadata = EventTypeMetadata.createXMLType(eventTypeName, isPreconfiguredStatic, configurationEventTypeXMLDOM.getSchemaResource() == null && configurationEventTypeXMLDOM.getSchemaText() == null);
        EventType type;
        if ((configurationEventTypeXMLDOM.getSchemaResource() == null) && (configurationEventTypeXMLDOM.getSchemaText() == null))
        {
            type = new SimpleXMLEventType(metadata, configurationEventTypeXMLDOM, this);
        }
        else
        {
            if (optionalSchemaModel == null)
            {
                throw new EPException("Schema model has not been provided");
            }
            type = new SchemaXMLEventType(metadata, configurationEventTypeXMLDOM, optionalSchemaModel, this);
        }

        nameToTypeMap.put(eventTypeName, type);
        xmldomRootElementNames.put(configurationEventTypeXMLDOM.getRootElementName(), type);

        return type;
    }

    public final EventBean adapterForType(Object event, EventType eventType) {
        return EventAdapterServiceHelper.adapterForType(event, eventType, this);
    }

    public final EventBean adaptorForTypedMap(Map<String, Object> properties, EventType eventType)
    {
        return new MapEventBean(properties, eventType);
    }

    public synchronized EventType addWrapperType(String eventTypeName, EventType underlyingEventType, Map<String, Object> propertyTypes, boolean isNamedWindow, boolean isInsertInto) throws EventAdapterException
	{
        // If we are wrapping an underlying type that is itself a wrapper, then this is a special case
        if (underlyingEventType instanceof WrapperEventType)
        {
            WrapperEventType underlyingWrapperType = (WrapperEventType) underlyingEventType;

            // the underlying type becomes the type already wrapped
            // properties are a superset of the wrapped properties and the additional properties
            underlyingEventType = underlyingWrapperType.getUnderlyingEventType();
            Map<String, Object> propertiesSuperset = new HashMap<String, Object>();
            propertiesSuperset.putAll(underlyingWrapperType.getUnderlyingMapType().getTypes());
            propertiesSuperset.putAll(propertyTypes);
            propertyTypes = propertiesSuperset;
        }

        boolean isPropertyAgnostic = false;
        if (underlyingEventType instanceof EventTypeSPI)
        {
            isPropertyAgnostic = ((EventTypeSPI) underlyingEventType).getMetadata().isPropertyAgnostic();
        }

        EventTypeMetadata metadata = EventTypeMetadata.createWrapper(eventTypeName, isNamedWindow, isInsertInto, isPropertyAgnostic);
        WrapperEventType newEventType = new WrapperEventType(metadata, eventTypeName, underlyingEventType, propertyTypes, this);

	    EventType existingType = nameToTypeMap.get(eventTypeName);
	    if (existingType != null)
	    {
	        // The existing type must be the same as the type created
	        if (!newEventType.equals(existingType))
	        {
                // It is possible that the wrapped event type is compatible: a child type of the desired type
                String message = isCompatibleWrapper(existingType, underlyingEventType, propertyTypes);
                if (message == null)
                {
                    return existingType;
                }

                throw new EventAdapterException("Event type named '" + eventTypeName +
	                    "' has already been declared with differing column name or type information: " + message);
	        }

	        // Since it's the same, return the existing type
	        return existingType;
	    }

	    nameToTypeMap.put(eventTypeName, newEventType);

	    return newEventType;
	}

    /**
     * Returns true if the wrapper type is compatible with an existing wrapper type, for the reason that
     * the underlying event is a subtype of the existing underlying wrapper's type.
     * @param existingType is the existing wrapper type
     * @param underlyingType is the proposed new wrapper type's underlying type
     * @param propertyTypes is the additional properties
     * @return true for compatible, or false if not
     */
    public static String isCompatibleWrapper(EventType existingType, EventType underlyingType, Map<String, Object> propertyTypes)
    {
        if (!(existingType instanceof WrapperEventType))
        {
            return "Type '" + existingType.getName() + "' is not compatible";
        }
        WrapperEventType existingWrapper = (WrapperEventType) existingType;

        String message = MapEventType.isDeepEqualsProperties(existingType.getName(), existingWrapper.getUnderlyingMapType().getTypes(), propertyTypes);
        if (message != null)
        {
            return message;
        }
        EventType existingUnderlyingType = existingWrapper.getUnderlyingEventType();

        // If one of the supertypes of the underlying type is the existing underlying type, we are compatible
        if (underlyingType.getSuperTypes() == null)
        {
            return "Type '" + existingType.getName() + "' is not compatible";
        }
        for (Iterator<EventType> it = underlyingType.getDeepSuperTypes(); it.hasNext();)
        {
            EventType superUnderlying = it.next();
            if (superUnderlying == existingUnderlyingType)
            {
                return null;
            }
        }
        return "Type '" + existingType.getName() + "' is not compatible";
    }

    public final EventType createAnonymousMapType(Map<String, Object> propertyTypes) throws EventAdapterException
    {
        String name = UuidGenerator.generate();
        EventTypeMetadata metadata = EventTypeMetadata.createAnonymous(name);
        return new MapEventType(metadata, name, this, propertyTypes, null, null);
    }

    public EventType createSemiAnonymousMapType(Map<String, Pair<EventType, String>> taggedEventTypes, Map<String, Pair<EventType, String>> arrayEventTypes, boolean isUsedByChildViews)
    {
        Map<String, Object> mapProperties = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Pair<EventType, String>> entry : taggedEventTypes.entrySet())
        {
            mapProperties.put(entry.getKey(), entry.getValue().getFirst());
        }
        for (Map.Entry<String, Pair<EventType, String>> entry : arrayEventTypes.entrySet())
        {
            mapProperties.put(entry.getKey(), new EventType[] {entry.getValue().getFirst()});
        }
        return createAnonymousMapType(mapProperties);
    }

    public final EventType createAnonymousWrapperType(EventType underlyingEventType, Map<String, Object> propertyTypes) throws EventAdapterException
    {
        String name = UuidGenerator.generate();
        EventTypeMetadata metadata = EventTypeMetadata.createAnonymous(name);

        // If we are wrapping an underlying type that is itself a wrapper, then this is a special case: unwrap
        if (underlyingEventType instanceof WrapperEventType)
        {
            WrapperEventType underlyingWrapperType = (WrapperEventType) underlyingEventType;

            // the underlying type becomes the type already wrapped
            // properties are a superset of the wrapped properties and the additional properties
            underlyingEventType = underlyingWrapperType.getUnderlyingEventType();
            Map<String, Object> propertiesSuperset = new HashMap<String, Object>();
            propertiesSuperset.putAll(underlyingWrapperType.getUnderlyingMapType().getTypes());
            propertiesSuperset.putAll(propertyTypes);
            propertyTypes = propertiesSuperset;
        }

    	return new WrapperEventType(metadata, name, underlyingEventType, propertyTypes, this);
    }

	public final EventBean adaptorForTypedWrapper(EventBean event, Map<String, Object> properties, EventType eventType)
	{
        if (event instanceof DecoratingEventBean)
        {
            DecoratingEventBean wrapper = (DecoratingEventBean) event;
            properties.putAll(wrapper.getDecoratingProperties());
            return new WrapperEventBean(wrapper.getUnderlyingEvent(), properties, eventType);
        }
        else
        {
            return new WrapperEventBean(event, properties, eventType);
        }
    }

    public final EventBean adapterForTypedBean(Object bean, BeanEventType eventType)
    {
        return new BeanEventBean(bean, eventType);
    }

    public void addAutoNamePackage(String javaPackageName)
    {
        javaPackageNames.add(javaPackageName);
    }

    private Pair<EventType[], Set<EventType>> getMapSuperTypes(Set<String> optionalSuperTypes)
            throws EventAdapterException
    {
        if ((optionalSuperTypes == null) || (optionalSuperTypes.isEmpty()))
        {
            return new Pair<EventType[], Set<EventType>>(null,null);
        }

        EventType[] superTypes = new EventType[optionalSuperTypes.size()];
        Set<EventType> deepSuperTypes = new LinkedHashSet<EventType>();

        int count = 0;
        for (String superName : optionalSuperTypes)
        {
            EventType type = this.nameToTypeMap.get(superName);
            if (type == null)
            {
                throw new EventAdapterException("Map supertype by name '" + superName + "' could not be found");
            }
            if (!(type instanceof MapEventType))
            {
                throw new EventAdapterException("Supertype by name '" + superName + "' is not a Map, expected a Map event type as a supertype");
            }
            superTypes[count++] = type;
            deepSuperTypes.add(type);
            addRecursiveSupertypes(deepSuperTypes, type);
        }
        return new Pair<EventType[], Set<EventType>>(superTypes,deepSuperTypes);
    }

    private static void addRecursiveSupertypes(Set<EventType> superTypes, EventType child)
    {
        if (child.getSuperTypes() != null)
        {
            for (int i = 0; i < child.getSuperTypes().length; i++)
            {
                superTypes.add(child.getSuperTypes()[i]);
                addRecursiveSupertypes(superTypes, child.getSuperTypes()[i]);
            }
        }
    }

    public EventBean[] typeCast(List<EventBean> events, EventType targetType)
    {
        EventBean[] convertedArray = new EventBean[events.size()];
        int count = 0;
        for (EventBean event : events)
        {
            EventBean converted;
            if (event instanceof DecoratingEventBean)
            {
                DecoratingEventBean wrapper = (DecoratingEventBean) event;
                if (targetType instanceof MapEventType)
                {
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.putAll(wrapper.getDecoratingProperties());
                    for (EventPropertyDescriptor propDesc : wrapper.getUnderlyingEvent().getEventType().getPropertyDescriptors())
                    {
                        props.put(propDesc.getPropertyName(), wrapper.getUnderlyingEvent().get(propDesc.getPropertyName()));
                    }
                    converted = adaptorForTypedMap(props, targetType);
                }
                else
                {
                    converted = adaptorForTypedWrapper(wrapper.getUnderlyingEvent(), wrapper.getDecoratingProperties(), targetType);
                }
            }
            else if ((event.getEventType() instanceof MapEventType) && (targetType instanceof MapEventType))
            {
                MappedEventBean mapEvent = (MappedEventBean) event;
                converted = this.adaptorForTypedMap(mapEvent.getProperties(), targetType);
            }
            else if ((event.getEventType() instanceof MapEventType) && (targetType instanceof WrapperEventType))
            {
                converted = this.adaptorForTypedWrapper(event, Collections.EMPTY_MAP, targetType);
            }
            else
            {
                throw new EPException("Unknown event type " + event.getEventType());
            }
            convertedArray[count] = converted;
            count++;
        }
        return convertedArray;
    }

    public boolean removeType(String name)
    {
        EventType eventType = nameToTypeMap.remove(name);
        if (eventType == null)
        {
            return false;
        }

        if (eventType instanceof BaseXMLEventType)
        {
            BaseXMLEventType baseXML = (BaseXMLEventType) eventType;
            xmldomRootElementNames.remove(baseXML.getRootElementName());
        }

        nameToHandlerMap.remove(name);
        return true;
    }
}
