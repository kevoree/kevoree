/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.map;

import com.espertech.esper.client.*;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.parse.ASTFilterSpecHelper;
import com.espertech.esper.event.*;
import com.espertech.esper.event.bean.BeanEventType;
import com.espertech.esper.event.property.*;
import com.espertech.esper.util.GraphUtil;
import com.espertech.esper.util.JavaClassHelper;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Implementation of the {@link EventType} interface for handling plain Maps containing name value pairs.
 */
public class MapEventType implements EventTypeSPI
{
    private final EventTypeMetadata metadata;
    private final String typeName;
    private final EventAdapterService eventAdapterService;
    private final EventType[] optionalSuperTypes;
    private final Set<EventType> optionalDeepSupertypes;

    // Simple (not-nested) properties are stored here
    private String[] propertyNames;       // Cache an array of property names so not to construct one frequently
    private EventPropertyDescriptor[] propertyDescriptors;
    private Map<String, EventPropertyDescriptor> propertyDescriptorMap;

    private final Map<String, FragmentEventType> simpleFragmentTypes;     // Mapping of property name (fragment-only) and type
    private final Map<String, Class> simplePropertyTypes;     // Mapping of property name (simple-only) and type
    private final Map<String, MapEventPropertyGetter> propertyGetters;   // Mapping of simple property name and getters
    private final Map<String, MapEventPropertyGetter> propertyGetterCache; // Mapping of all property names and getters

    // Nestable definition of Map contents is here
    private Map<String, Object> nestableTypes;  // Deep definition of the map-type, containing nested maps and objects
    private Map<String, Pair<EventPropertyDescriptor, EventPropertyWriter>> propertyWriters;
    private EventPropertyDescriptor[] writablePropertyDescriptors;

    private int hashCode;

    /**
     * Constructor takes a type name, map of property names and types, for
     * use with nestable Map events.
     * @param typeName is the event type name used to distinquish map types that have the same property types,
     * empty string for anonymous maps, or for insert-into statements generating map events
     * the stream name
     * @param propertyTypes is pairs of property name and type
     * @param eventAdapterService is required for access to objects properties within map values
     * @param optionalSuperTypes the supertypes to this type if any, or null if there are no supertypes
     * @param optionalDeepSupertypes the deep supertypes to this type if any, or null if there are no deep supertypes
     * @param metadata event type metadata
     */
    public MapEventType(EventTypeMetadata metadata,
                        String typeName,
                        EventAdapterService eventAdapterService,
                        Map<String, Object> propertyTypes,
                        EventType[] optionalSuperTypes,
                        Set<EventType> optionalDeepSupertypes)
    {
        this.metadata = metadata;
        this.typeName = typeName;
        this.eventAdapterService = eventAdapterService;

        this.nestableTypes = new HashMap<String, Object>();
        this.nestableTypes.putAll(propertyTypes);

        this.optionalSuperTypes = optionalSuperTypes;
        if (optionalDeepSupertypes == null)
        {
            this.optionalDeepSupertypes = Collections.emptySet();
        }
        else
        {
            this.optionalDeepSupertypes = optionalDeepSupertypes;
        }

        // determine property set and prepare getters
        PropertySetDescriptor propertySet = getNestableMapProperties(propertyTypes, eventAdapterService);
        
        List<String> propertyNameList = propertySet.getPropertyNameList();
        propertyNames = propertyNameList.toArray(new String[propertyNameList.size()]);
        propertyGetters = propertySet.getPropertyGetters();
        propertyGetterCache = new HashMap<String, MapEventPropertyGetter>();
        simplePropertyTypes = propertySet.getSimplePropertyTypes();
        simpleFragmentTypes = propertySet.getSimpleFragmentTypes();
        propertyDescriptors = propertySet.getPropertyDescriptors().toArray(new EventPropertyDescriptor[propertySet.getPropertyDescriptors().size()]);
        propertyDescriptorMap = new HashMap<String, EventPropertyDescriptor>();
        for (EventPropertyDescriptor desc : propertyDescriptors)
        {
            propertyDescriptorMap.put(desc.getPropertyName(), desc);                                
        }

        hashCode = typeName.hashCode();
        for (Map.Entry<String, Class> entry : simplePropertyTypes.entrySet())
        {
            hashCode *= 31;
            hashCode ^= entry.getKey().hashCode();
        }

        // Copy parent properties to child
        copySuperTypes();
    }

    public String getName()
    {
        return typeName;
    }

    public EventBeanReader getReader()
    {
        return new MapEventBeanReader(this);
    }

    public final Class getPropertyType(String propertyName)
    {
        Class result = simplePropertyTypes.get(ASTFilterSpecHelper.unescapeDot(propertyName));
        if (result != null)
        {
            return result;
        }

        // see if this is a nested property
        int index = ASTFilterSpecHelper.unescapedIndexOfDot(propertyName);
        if (index == -1)
        {
            // dynamic simple property
            if (propertyName.endsWith("?"))
            {
                return Object.class;
            }

            // parse, can be an indexed property
            Property property = PropertyParser.parse(propertyName, false);
            if (property instanceof IndexedProperty)
            {
                IndexedProperty indexedProp = (IndexedProperty) property;
                Object type = nestableTypes.get(indexedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                else if (type instanceof EventType[])
                {
                    return ((EventType[]) type)[0].getUnderlyingType();
                }
                else if (type instanceof String)
                {
                    String propTypeName = type.toString();
                    boolean isArray = isPropertyArray(propTypeName);
                    if (isArray) {
                        propTypeName = getPropertyRemoveArray(propTypeName);
                    }
                    EventType innerType = eventAdapterService.getExistsTypeByName(propTypeName);
                    return innerType.getUnderlyingType();
                }
                if (!(type instanceof Class))
                {
                    return null;
                }
                if (!((Class) type).isArray())
                {
                    return null;
                }
                // its an array
                return ((Class)type).getComponentType();
            }
            else if (property instanceof MappedProperty)
            {
                MappedProperty mappedProp = (MappedProperty) property;
                Object type = nestableTypes.get(mappedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                if (type instanceof Class)
                {
                    if (JavaClassHelper.isImplementsInterface((Class) type, Map.class))
                    {
                        return Object.class;
                    }
                }
                return null;
            }
            else
            {
                return null;
            }
        }

        // Map event types allow 2 types of properties inside:
        //   - a property that is a Java object is interrogated via bean property getters and BeanEventType
        //   - a property that is a Map itself is interrogated via map property getters
        // The property getters therefore act on

        // Take apart the nested property into a map key and a nested value class property name
        String propertyMap = ASTFilterSpecHelper.unescapeDot(propertyName.substring(0, index));
        String propertyNested = propertyName.substring(index + 1, propertyName.length());
        boolean isRootedDynamic = false;

        // If the property is dynamic, remove the ? since the property type is defined without
        if (propertyMap.endsWith("?"))
        {
            propertyMap = propertyMap.substring(0, propertyMap.length() - 1);
            isRootedDynamic = true;
        }

        Object nestedType = nestableTypes.get(propertyMap);
        if (nestedType == null)
        {
            // parse, can be an indexed property
            Property property = PropertyParser.parse(propertyMap, false);
            if (property instanceof IndexedProperty)
            {
                IndexedProperty indexedProp = (IndexedProperty) property;
                Object type = nestableTypes.get(indexedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                // handle map-in-map case
                if (type instanceof String) {
                    String propTypeName = type.toString();
                    boolean isArray = isPropertyArray(propTypeName);
                    if (isArray) {
                        propTypeName = getPropertyRemoveArray(propTypeName);
                    }
                    EventType innerType = eventAdapterService.getExistsTypeByName(propTypeName);
                    if (!(innerType instanceof MapEventType))
                    {
                        return null;
                    }
                    return innerType.getPropertyType(propertyNested);
                }
                // handle eventtype[] in map
                else if (type instanceof EventType[])
                {
                    EventType innerType = ((EventType[]) type)[0];
                    return innerType.getPropertyType(propertyNested);
                }
                // handle array class in map case
                else
                {
                    if (!(type instanceof Class))
                    {
                        return null;
                    }
                    if (!((Class) type).isArray())
                    {
                        return null;
                    }
                    Class componentType = ((Class) type).getComponentType();
                    EventType nestedEventType = eventAdapterService.addBeanType(componentType.getName(), componentType, false, false, false);
                    return nestedEventType.getPropertyType(propertyNested);
                }
            }
            else if (property instanceof MappedProperty)
            {
                return null;    // Since no type information is available for the property
            }
            else
            {
                return null;
            }            
        }

        // If there is a map value in the map, return the Object value if this is a dynamic property
        if (nestedType == Map.class)
        {
            Property prop = PropertyParser.parse(propertyNested, isRootedDynamic);
            return prop.getPropertyTypeMap(null, eventAdapterService);   // we don't have a definition of the nested props
        }
        else if (nestedType instanceof Map)
        {
            Property prop = PropertyParser.parse(propertyNested, isRootedDynamic);
            Map nestedTypes = (Map) nestedType;
            return prop.getPropertyTypeMap(nestedTypes, eventAdapterService);
        }
        else if (nestedType instanceof Class)
        {
            Class simpleClass = (Class) nestedType;
            EventType nestedEventType = eventAdapterService.addBeanType(simpleClass.getName(), simpleClass, false, false, false);
            return nestedEventType.getPropertyType(propertyNested);
        }
        else if (nestedType instanceof EventType)
        {
            EventType innerType = (EventType) nestedType;
            return innerType.getPropertyType(propertyNested);
        }
        else if (nestedType instanceof EventType[])
        {
            return null;    // requires indexed property
        }
        else if (nestedType instanceof String)
        {
            String nestedName = nestedType.toString();
            boolean isArray = isPropertyArray(nestedName);
            if (isArray) {
                nestedName = getPropertyRemoveArray(nestedName);
            }
            EventType innerType = eventAdapterService.getExistsTypeByName(nestedName);
            if (!(innerType instanceof MapEventType))
            {
                return null;
            }
            return innerType.getPropertyType(propertyNested);
        }
        else
        {
            String message = "Nestable map type configuration encountered an unexpected value type of '"
                + nestedType.getClass() + " for property '" + propertyName + "', expected Class, Map.class or Map<String, Object> as value type";
            throw new PropertyAccessException(message);
        }
    }

    public final Class getUnderlyingType()
    {
        return java.util.Map.class;
    }

    public MapEventPropertyGetter getGetter(final String propertyName)
    {
        MapEventPropertyGetter cachedGetter = propertyGetterCache.get(propertyName);
        if (cachedGetter != null)
        {
            return cachedGetter;
        }

        String unescapePropName = ASTFilterSpecHelper.unescapeDot(propertyName);
        MapEventPropertyGetter getter = propertyGetters.get(unescapePropName);
        if (getter != null)
        {
            propertyGetterCache.put(propertyName, getter);
            return getter;
        }

        // see if this is a nested property
        int index = ASTFilterSpecHelper.unescapedIndexOfDot(propertyName);
        if (index == -1)
        {
            Property prop = PropertyParser.parse(propertyName, false);
            if (prop instanceof DynamicProperty)
            {
                MapEventPropertyGetter getterDyn = prop.getGetterMap(null, eventAdapterService);
                propertyGetterCache.put(propertyName, getterDyn);
                return getterDyn;
            }
            else if (prop instanceof IndexedProperty)
            {
                IndexedProperty indexedProp = (IndexedProperty) prop;
                Object type = nestableTypes.get(indexedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                else if (type instanceof EventType[])
                {
                    MapEventPropertyGetter getterArr = new MapEventBeanArrayIndexedPropertyGetter(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex());
                    propertyGetterCache.put(propertyName, getterArr);
                    return getterArr;
                }
                else if (type instanceof String)
                {
                    String nestedTypeName = type.toString();
                    boolean isArray = isPropertyArray(nestedTypeName);
                    if (isArray) {
                        nestedTypeName = getPropertyRemoveArray(nestedTypeName);
                    }
                    EventType innerType = eventAdapterService.getExistsTypeByName(nestedTypeName);
                    if (!(innerType instanceof MapEventType))
                    {
                        return null;
                    }
                    MapEventPropertyGetter typeGetter;
                    if (!isArray)
                    {
                        typeGetter = new MapMaptypedUndPropertyGetter(indexedProp.getPropertyNameAtomic(), eventAdapterService, innerType);
                    }
                    else
                    {
                        typeGetter = new MapArrayMaptypedUndPropertyGetter(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex(), eventAdapterService, innerType);
                    }
                    propertyGetterCache.put(propertyName, typeGetter);
                    return typeGetter;
                }
                // handle map type name in map
                if (!(type instanceof Class))
                {
                    return null;
                }
                if (!((Class) type).isArray())
                {
                    return null;
                }

                // its an array
                Class componentType = ((Class) type).getComponentType();
                MapEventPropertyGetter indexedGetter = new MapArrayPOJOEntryIndexedPropertyGetter(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex(), eventAdapterService, componentType);
                propertyGetterCache.put(propertyName, indexedGetter);
                return indexedGetter;
            }
            else if (prop instanceof MappedProperty)
            {
                MappedProperty mappedProp = (MappedProperty) prop;
                Object type = nestableTypes.get(mappedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                if (type instanceof Class)
                {
                    if (JavaClassHelper.isImplementsInterface((Class) type, Map.class))
                    {
                        return new MapMappedPropertyGetter(mappedProp.getPropertyNameAtomic(), mappedProp.getKey());
                    }
                }
                return null;
            }
            else
            {
                return null;
            }
        }

        // Take apart the nested property into a map key and a nested value class property name
        String propertyMap = ASTFilterSpecHelper.unescapeDot(propertyName.substring(0, index));
        String propertyNested = propertyName.substring(index + 1, propertyName.length());
        boolean isRootedDynamic = false;

        // If the property is dynamic, remove the ? since the property type is defined without
        if (propertyMap.endsWith("?"))
        {
            propertyMap = propertyMap.substring(0, propertyMap.length() - 1);
            isRootedDynamic = true;
        }

        Object nestedType = nestableTypes.get(propertyMap);
        if (nestedType == null)
        {
            // parse, can be an indexed property
            Property property = PropertyParser.parse(propertyMap, false);
            if (property instanceof IndexedProperty)
            {
                IndexedProperty indexedProp = (IndexedProperty) property;
                Object type = nestableTypes.get(indexedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                if (type instanceof String)
                {
                    String nestedTypeName = type.toString();
                    boolean isArray = isPropertyArray(nestedTypeName);
                    if (isArray) {
                        nestedTypeName = getPropertyRemoveArray(nestedTypeName);
                    }
                    EventType innerType = eventAdapterService.getExistsTypeByName(nestedTypeName);
                    if (!(innerType instanceof MapEventType))
                    {
                        return null;
                    }
                    MapEventPropertyGetter typeGetter;
                    if (!isArray)
                    {
                        typeGetter = new MapMaptypedEntryPropertyGetter(propertyMap, innerType.getGetter(propertyNested), (MapEventType) innerType, eventAdapterService);
                    }
                    else
                    {
                        typeGetter = new MapArrayMaptypedEntryPropertyGetter(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex(), innerType.getGetter(propertyNested), innerType, eventAdapterService);
                    }
                    propertyGetterCache.put(propertyName, typeGetter);
                    return typeGetter;
                }
                else if (type instanceof EventType[])
                {
                    EventType componentType = ((EventType[]) type)[0];
                    final EventPropertyGetter nestedGetter = componentType.getGetter(propertyNested);
                    if (nestedGetter == null)
                    {
                        return null;
                    }
                    MapEventPropertyGetter typeGetter = new MapEventBeanArrayIndexedElementPropertyGetter(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex(), nestedGetter);
                    propertyGetterCache.put(propertyName, typeGetter);
                    return typeGetter;
                }
                else
                {
                    if (!(type instanceof Class))
                    {
                        return null;
                    }
                    if (!((Class) type).isArray())
                    {
                        return null;
                    }
                    Class componentType = ((Class) type).getComponentType();
                    EventType nestedEventType = eventAdapterService.addBeanType(componentType.getName(), componentType, false, false, false);

                    final EventPropertyGetter nestedGetter = nestedEventType.getGetter(propertyNested);
                    if (nestedGetter == null)
                    {
                        return null;
                    }
                    Class propertyTypeGetter = nestedEventType.getPropertyType(propertyNested);
                    // construct getter for nested property
                    MapEventPropertyGetter indexGetter = new MapArrayPOJOBeanEntryIndexedPropertyGetter(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex(), nestedGetter, eventAdapterService, propertyTypeGetter);
                    propertyGetterCache.put(propertyName, indexGetter);
                    return indexGetter;
                }
            }
            else if (property instanceof MappedProperty)
            {
                return null;    // Since no type information is available for the property
            }
            else
            {
                return null;
            }
        }

        // The map contains another map, we resolve the property dynamically
        if (nestedType == Map.class)
        {
            Property prop = PropertyParser.parse(propertyNested, isRootedDynamic);
            EventPropertyGetter getterNestedMap = prop.getGetterMap(null, eventAdapterService);
            if (getterNestedMap == null)
            {
                return null;
            }
            MapEventPropertyGetter mapGetter = new MapPropertyGetter(propertyMap, getterNestedMap);
            propertyGetterCache.put(propertyName, mapGetter);
            return mapGetter;
        }
        else if (nestedType instanceof Map)
        {
            Property prop = PropertyParser.parse(propertyNested, isRootedDynamic);
            Map nestedTypes = (Map) nestedType;
            EventPropertyGetter getterNestedMap = prop.getGetterMap(nestedTypes, eventAdapterService);
            if (getterNestedMap == null)
            {
                return null;
            }
            MapEventPropertyGetter mapGetter = new MapPropertyGetter(propertyMap, getterNestedMap);
            propertyGetterCache.put(propertyName, mapGetter);
            return mapGetter;
        }
        else if (nestedType instanceof Class)
        {
            // ask the nested class to resolve the property
            Class simpleClass = (Class) nestedType;
            EventType nestedEventType = eventAdapterService.addBeanType(simpleClass.getName(), simpleClass, false, false, false);
            final EventPropertyGetter nestedGetter = nestedEventType.getGetter(propertyNested);
            if (nestedGetter == null)
            {
                return null;
            }
            Class nestedReturnType = nestedEventType.getPropertyType(propertyNested);

            // construct getter for nested property
            getter = new MapPOJOEntryPropertyGetter(propertyMap, nestedGetter, eventAdapterService, nestedReturnType);
            propertyGetterCache.put(propertyName, getter);
            return getter;
        }
        else if (nestedType instanceof EventType)
        {
            // ask the nested class to resolve the property
            EventType innerType = (EventType) nestedType;
            final EventPropertyGetter nestedGetter = innerType.getGetter(propertyNested);
            if (nestedGetter == null)
            {
                return null;
            }

            // construct getter for nested property
            getter = new MapEventBeanEntryPropertyGetter(propertyMap, nestedGetter);
            propertyGetterCache.put(propertyName, getter);
            return getter;
        }
        else if (nestedType instanceof EventType[])
        {
            EventType[] typeArray = (EventType[]) nestedType;
            Class underlying = typeArray[0].getUnderlyingType();
            MapEventPropertyGetter beanArrGetter = new MapEventBeanArrayPropertyGetter(propertyMap, underlying);
            propertyGetterCache.put(propertyName, beanArrGetter);
            return beanArrGetter;            
        }
        else if (nestedType instanceof String)
        {
            String nestedName = nestedType.toString();
            boolean isArray = isPropertyArray(nestedName);
            if (isArray) {
                nestedName = getPropertyRemoveArray(nestedName);
            }
            EventType innerType = eventAdapterService.getExistsTypeByName(nestedName);
            if (!(innerType instanceof MapEventType))
            {
                return null;
            }
            EventPropertyGetter innerGetter = innerType.getGetter(propertyNested);
            if (innerGetter == null)
            {
                return null;
            }
            MapEventPropertyGetter maptypeGetter;
            if (!isArray)
            {
                maptypeGetter = new MapMaptypedEntryPropertyGetter(propertyMap, innerGetter, (MapEventType) innerType, eventAdapterService);
            }
            else
            {
                maptypeGetter = new MapArrayMaptypedEntryPropertyGetter(propertyMap, 0, innerGetter, innerType, eventAdapterService);
            }
            propertyGetterCache.put(propertyName, maptypeGetter);
            return maptypeGetter;
        }
        else
        {
            String message = "Nestable map type configuration encountered an unexpected value type of '"
                + nestedType.getClass() + " for property '" + propertyName + "', expected Class, Map.class or Map<String, Object> as value type";
            throw new PropertyAccessException(message);
        }
    }

    /**
     * Returns the value of the given property, allowing nested property names.
     * @param propertyName is the name of the property
     * @param values is the map to get the value from
     * @return property value
     */
    public Object getValue(String propertyName, Map values)
    {
        // if a known type, return value
        if (simplePropertyTypes.get(ASTFilterSpecHelper.unescapeDot(propertyName)) != null)
        {
            return values.get(ASTFilterSpecHelper.unescapeDot(propertyName));
        }

        // see if this is a nested property
        int index = ASTFilterSpecHelper.unescapedIndexOfDot(propertyName);
        if (index == -1)
        {
            return null;
        }

        // Take apart the nested property into a map key and a nested value class property name
        final String propertyMap = ASTFilterSpecHelper.unescapeDot(propertyName.substring(0, index));
        String propertyNested = propertyName.substring(index + 1, propertyName.length());

        Class result = simplePropertyTypes.get(propertyMap);
        if (result == null)
        {
            return null;
        }

        // ask the nested class to resolve the property
        EventType nestedType = eventAdapterService.addBeanType(result.getName(), result, false, false, false);
        final EventPropertyGetter nestedGetter = nestedType.getGetter(propertyNested);
        if (nestedGetter == null)
        {
            return null;
        }

        // Wrap object
        Object value = values.get(propertyMap);
        if (value == null)
        {
            return null;
        }
        EventBean event = MapEventType.this.eventAdapterService.adapterForBean(value);
        return nestedGetter.get(event);
    }

    public String[] getPropertyNames()
    {
        return propertyNames;
    }

    public boolean isProperty(String propertyName)
    {
        Class propertyType = getPropertyType(propertyName);
        if (propertyType == null)
        {
            // Could be a native null type, such as "insert into A select null as field..."
            if (simplePropertyTypes.containsKey(ASTFilterSpecHelper.unescapeDot(propertyName)))
            {
                return true;
            }
        }
        return propertyType != null;
    }

    public EventType[] getSuperTypes()
    {
        return optionalSuperTypes;
    }

    public Iterator<EventType> getDeepSuperTypes()
    {
        return optionalDeepSupertypes.iterator();
    }

    public String toString()
    {
        return "MapEventType " +
                "typeName=" + typeName +
                " propertyNames=" + Arrays.toString(propertyNames);
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof EventType))
        {
            return false;
        }

        String message = getEqualsMessage((EventType)obj);
        return message == null;
    }

    public int hashCode()
    {
        return hashCode;
    }

    /**
     * Returns the name-type map of map properties, each value in the map
     * can be a Class or a Map<String, Object> (for nested maps).
     * @return is the property name and types
     */
    public Map<String, Object> getTypes()
    {
        return this.nestableTypes;
    }

    /**
     * Adds additional properties that do not yet exist on the given type.
     * <p.
     * Ignores properties already present. Allows nesting.
     * @param typeMap properties to add
     * @param eventAdapterService for resolving further map event types that are property types
     */
    public void addAdditionalProperties(Map<String, Object> typeMap, EventAdapterService eventAdapterService)
    {
        // merge type graphs
        nestableTypes = GraphUtil.mergeNestableMap(typeMap, nestableTypes);

        // construct getters and types for each property (new or old)
        PropertySetDescriptor propertySet = getNestableMapProperties(typeMap, eventAdapterService);

        // add each new descriptor
        List<EventPropertyDescriptor> newPropertyDescriptors = new ArrayList<EventPropertyDescriptor>();
        for (EventPropertyDescriptor propertyDesc : propertySet.getPropertyDescriptors())
        {
            if (propertyGetters.containsKey(propertyDesc.getPropertyName()))  // not a new property
            {
                continue;
            }
            newPropertyDescriptors.add(propertyDesc);
        }

        // add each that is not already present
        List<String> newPropertyNames = new ArrayList<String>();
        for (String propertyName : propertySet.getPropertyNameList())
        {
            if (propertyGetters.containsKey(propertyName))  // not a new property
            {
                continue;
            }
            newPropertyNames.add(propertyName);
            propertyGetters.put(propertyName, propertySet.getPropertyGetters().get(propertyName));
            simplePropertyTypes.put(propertyName, propertySet.getSimplePropertyTypes().get(propertyName));
        }

        // expand property name array
        String[] allPropertyNames = new String[propertyNames.length + newPropertyNames.size()];
        System.arraycopy(propertyNames, 0, allPropertyNames, 0, propertyNames.length);
        int count = propertyNames.length;
        for (String newProperty : newPropertyNames)
        {
            allPropertyNames[count++] = newProperty;
        }
        propertyNames = allPropertyNames;

        // expand descriptor array
        EventPropertyDescriptor[] allPropertyDescriptors = new EventPropertyDescriptor[propertyDescriptors.length + newPropertyNames.size()];
        System.arraycopy(propertyDescriptors, 0, allPropertyDescriptors, 0, propertyDescriptors.length);
        count = propertyDescriptors.length;
        for (EventPropertyDescriptor desc : newPropertyDescriptors)
        {
            allPropertyDescriptors[count++] = desc;
        }
        propertyDescriptors = allPropertyDescriptors;
    }

    public EventPropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors;
    }

    /**
     * Compares two sets of properties and determines if they are the same, allowing for
     * boxed/unboxed types, and nested map types.
     * @param setOne is the first set of properties
     * @param setTwo is the second set of properties
     * @param otherName name of the type compared to
     * @return null if the property set is equivalent or message if not
     */
    public static String isDeepEqualsProperties(String otherName, Map<String, Object> setOne, Map<String, Object> setTwo)
    {
        // Should have the same number of properties
        if (setOne.size() != setTwo.size())
        {
            return "Type by name '" + otherName + "' expects " + setOne.size() + " properties but receives " + setTwo.size() + " properties";
        }

        // Compare property by property
        for (Map.Entry<String, Object> entry : setOne.entrySet())
        {
            String propName = entry.getKey();
            Object setTwoType = setTwo.get(entry.getKey());
            boolean setTwoTypeFound = setTwo.containsKey(entry.getKey());
            Object setOneType = entry.getValue();

            // allow null for nested event types
            if ((setOneType instanceof String || setOneType instanceof EventType) && setTwoType == null) {
                continue;
            }
            if ((setTwoType instanceof String || setTwoType instanceof EventType) && setOneType == null) {
                continue;
            }
            if (!setTwoTypeFound) {
                return "The property '" + propName + "' is not provided but required";
            }
            if (((setTwoType == null) && (setOneType != null)) ||
                 (setTwoType != null) && (setOneType == null))
            {
                return "Type by name '" + otherName + "' in property '" + propName + "' incompatible with null-type or property name not found in target";
            }
            if (setTwoType == null)
            {
                continue;
            }

            if ((setTwoType instanceof Class) && (setOneType instanceof Class))
            {
                Class boxedOther = JavaClassHelper.getBoxedType((Class)setTwoType);
                Class boxedThis = JavaClassHelper.getBoxedType((Class)setOneType);
                if (!boxedOther.equals(boxedThis))
                {
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected " + boxedThis + " but receives " + boxedOther;
                }
            }
            else if ((setTwoType instanceof Map) && (setOneType instanceof Map))
            {
                String messageIsDeepEquals = isDeepEqualsProperties(propName, (Map<String, Object>)setOneType, (Map<String, Object>)setTwoType);
                if (messageIsDeepEquals != null)
                {
                    return messageIsDeepEquals;
                }
            }
            else if ((setTwoType instanceof EventType) && (setOneType instanceof EventType))
            {
                if (!setOneType.equals(setTwoType))
                {
                    EventType setOneEventType = (EventType) setOneType;
                    EventType setTwoEventType = (EventType) setTwoType;
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected event type '" + setOneEventType.getName() + "' but receives event type '" + setTwoEventType.getName() + "'";
                }
            }
            else if ((setTwoType instanceof String) && (setOneType instanceof EventType))
            {
                if (!((EventType) setOneType).getName().equals(setTwoType))
                {
                    EventType setOneEventType = (EventType) setOneType;
                    String setTwoEventType = (String) setTwoType;
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected event type '" + setOneEventType.getName() + "' but receives event type '" + setTwoEventType + "'";
                }
            }
            else if ((setTwoType instanceof EventType) && (setOneType instanceof String))
            {
                if (!((EventType) setTwoType).getName().equals(setOneType))
                {
                    String setOneEventType = (String) setOneType;
                    EventType setTwoEventType = (EventType) setTwoType;
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected event type '" + setOneEventType + "' but receives event type '" + setTwoEventType.getName() + "'";
                }
            }
            else if ((setTwoType instanceof String) && (setOneType instanceof String))
            {
                if (!setTwoType.equals(setOneType))
                {
                    String setOneEventType = (String) setOneType;
                    String setTwoEventType = (String) setTwoType;
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected event type '" + setOneEventType + "' but receives event type '" + setTwoEventType + "'";
                }
            }
            else if ((setTwoType instanceof EventType[]) && (setOneType instanceof String))
            {
                EventType[] setTwoTypeArr = (EventType[]) setTwoType;
                EventType setTwoFragmentType = setTwoTypeArr[0];
                String setOneTypeString = (String)setOneType;
                if (!(setOneTypeString.endsWith("[]"))) {
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected event type '" + setOneType + "' but receives event type '" + setTwoFragmentType.getName() + "[]'";
                }
                String setOneTypeNoArray = (setOneTypeString).replaceAll("\\[\\]", "");
                if (!(setTwoFragmentType.getName().equals(setOneTypeNoArray)))
                {
                    return "Type by name '" + otherName + "' in property '" + propName + "' expected event type '" + setOneTypeNoArray + "[]' but receives event type '" + setTwoFragmentType.getName() + "'";
                }
            }
            else
            {
                String typeOne = getTypeName(setOneType);
                String typeTwo = getTypeName(setTwoType);
                return "Type by name '" + otherName + "' in property '" + propName + "' expected " + typeOne + " but receives " + typeTwo;
            }
        }

        return null;
    }

    private static String getTypeName(Object setOneType)
    {
        if (setOneType == null)
        {
            return "null";
        }
        if (setOneType instanceof Class)
        {
            return ((Class) setOneType).getName();
        }
        if (setOneType instanceof EventType)
        {
            return "event type '" + ((EventType)setOneType).getName() + "'";
        }
        return setOneType.getClass().getName();
    }

    private static void generateExceptionNestedProp(String name, Object value) throws EPException
    {
        String clazzName = (value == null) ? "null" : value.getClass().getSimpleName();
        throw new EPException("Nestable map type configuration encountered an unexpected property type of '"
            + clazzName + "' for property '" + name + "', expected java.lang.Class or java.util.Map or the name of a previously-declared Map type");
    }

    private void copySuperTypes()
    {
        if (optionalSuperTypes != null)
        {
            Set<String> allProperties = new LinkedHashSet<String>(Arrays.asList(propertyNames));
            Map<String, EventPropertyDescriptor> allDescriptors = new LinkedHashMap<String, EventPropertyDescriptor>();
            for (EventPropertyDescriptor current : propertyDescriptors)
            {
               allDescriptors.put(current.getPropertyName(), current); 
            }
            for (int i = 0; i < optionalSuperTypes.length; i++)
            {
                allProperties.addAll(Arrays.asList(optionalSuperTypes[i].getPropertyNames()));
                MapEventType mapSuperType = (MapEventType) optionalSuperTypes[i];
                simplePropertyTypes.putAll(mapSuperType.simplePropertyTypes);
                propertyGetters.putAll(mapSuperType.propertyGetters);
                nestableTypes.putAll(mapSuperType.nestableTypes);
                for (EventPropertyDescriptor desc : optionalSuperTypes[i].getPropertyDescriptors())
                {
                    allDescriptors.put(desc.getPropertyName(), desc);
                }
            }
            propertyNames = allProperties.toArray(new String[allProperties.size()]);
            Collection<EventPropertyDescriptor> descs = allDescriptors.values();
            propertyDescriptors = descs.toArray(new EventPropertyDescriptor[descs.size()]);
            for (EventPropertyDescriptor desc : propertyDescriptors)
            {
                propertyDescriptorMap.put(desc.getPropertyName(), desc);
            }            
        }
    }

    private static PropertySetDescriptor getNestableMapProperties(Map<String, Object> propertiesToAdd, EventAdapterService eventAdapterService)
            throws EPException
    {
        List<String> propertyNameList = new ArrayList<String>();
        List<EventPropertyDescriptor> propertyDescriptors = new ArrayList<EventPropertyDescriptor>();
        Map<String, Class> simplePropertyTypes = new HashMap<String, Class>();
        Map<String, MapEventPropertyGetter> propertyGetters = new HashMap<String, MapEventPropertyGetter>();
        Map<String, FragmentEventType> eventTypeFragments = new HashMap<String, FragmentEventType>();

        // Initialize getters and names array: at this time we do not care about nested types,
        // these are handled at the time someone is asking for them
        for (Map.Entry<String, Object> entry : propertiesToAdd.entrySet())
        {
            if (!(entry.getKey() instanceof String))
            {
                throw new EPException("Invalid map type configuration: property name is not a String-type value");
            }
            String name = entry.getKey();

            // handle types that are String values
            if (entry.getValue() instanceof String)
            {
                String value = entry.getValue().toString().trim();
                Class clazz = JavaClassHelper.getPrimitiveClassForName(value);
                if (clazz != null)
                {
                    entry.setValue(clazz);
                }
            }

            if (entry.getValue() instanceof Class)
            {
                Class classType = (Class) entry.getValue();
                simplePropertyTypes.put(name, classType);
                propertyNameList.add(name);

                boolean isArray = classType.isArray();
                Class componentType = null;
                if (isArray)
                {
                    componentType = classType.getComponentType();
                }
                boolean isFragment = JavaClassHelper.isFragmentableType(classType);
                BeanEventType nativeFragmentType = null;
                if (isFragment)
                {
                    FragmentEventType fragmentType = EventBeanUtility.createNativeFragmentType(classType, null, eventAdapterService);
                    if (fragmentType != null)
                    {
                        nativeFragmentType = (BeanEventType) fragmentType.getFragmentType();
                        eventTypeFragments.put(name, fragmentType);
                    }
                    else
                    {
                        isFragment = false;
                    }
                }
                else
                {
                    eventTypeFragments.put(name, null);
                }
                propertyDescriptors.add(new EventPropertyDescriptor(name, classType, componentType, false, false, isArray, false, isFragment));

                MapEventPropertyGetter getter = new MapEntryPropertyGetter(name, nativeFragmentType, eventAdapterService);
                propertyGetters.put(name, getter);
                continue;
            }

            // A null-type is also allowed
            if (entry.getValue() == null)
            {
                simplePropertyTypes.put(name, null);
                propertyNameList.add(name);
                MapEventPropertyGetter getter = new MapEntryPropertyGetter(name, null, null);
                propertyGetters.put(name, getter);
                propertyDescriptors.add(new EventPropertyDescriptor(name, null, null, false, false, false, false, false));
                eventTypeFragments.put(name, null);
                continue;
            }

            if (entry.getValue() instanceof Map)
            {
                // Add Map itself as a property
                simplePropertyTypes.put(name, Map.class);
                propertyNameList.add(name);
                MapEventPropertyGetter getter = new MapEntryPropertyGetter(name, null, null);
                propertyGetters.put(name, getter);
                propertyDescriptors.add(new EventPropertyDescriptor(name, Map.class, null, false, false, false, true, false));
                eventTypeFragments.put(name, null);
                continue;
            }

            if (entry.getValue() instanceof EventType)
            {
                // Add EventType itself as a property
                EventType eventType = (EventType) entry.getValue();
                simplePropertyTypes.put(name, eventType.getUnderlyingType());
                propertyNameList.add(name);
                MapEventPropertyGetter getter = new MapEventBeanPropertyGetter(name);
                propertyGetters.put(name, getter);
                propertyDescriptors.add(new EventPropertyDescriptor(name, eventType.getUnderlyingType(), null, false, false, false, false, true));
                eventTypeFragments.put(name, new FragmentEventType(eventType, false, false));
                continue;
            }

            if (entry.getValue() instanceof EventType[])
            {
                // Add EventType array itself as a property, type is expected to be first array element
                EventType eventType = ((EventType[]) entry.getValue())[0];
                Object prototypeArray = Array.newInstance(eventType.getUnderlyingType(), 0);
                simplePropertyTypes.put(name, prototypeArray.getClass());
                propertyNameList.add(name);
                MapEventPropertyGetter getter = new MapEventBeanArrayPropertyGetter(name, eventType.getUnderlyingType());
                propertyGetters.put(name, getter);
                propertyDescriptors.add(new EventPropertyDescriptor(name, prototypeArray.getClass(), eventType.getUnderlyingType(), false, false, true, false, true));
                eventTypeFragments.put(name, new FragmentEventType(eventType, true, false));
                continue;
            }

            if (entry.getValue() instanceof String)
            {
                String propertyName = entry.getValue().toString();
                boolean isArray = isPropertyArray(propertyName);
                if (isArray) {
                    propertyName = getPropertyRemoveArray(propertyName);
                }

                // Add EventType itself as a property
                EventType eventType = eventAdapterService.getExistsTypeByName(propertyName);
                if (!(eventType instanceof MapEventType))
                {
                    throw new EPException("Nestable map type configuration encountered an unexpected property type name '"
                        + entry.getValue() + "' for property '" + name + "', expected java.lang.Class or java.util.Map or the name of a previously-declared Map type");

                }

                Class underlyingType = eventType.getUnderlyingType();
                if (isArray)
                {
                    underlyingType = Array.newInstance(underlyingType, 0).getClass();
                }
                simplePropertyTypes.put(name, underlyingType);
                propertyNameList.add(name);
                MapEventPropertyGetter getter;
                if (!isArray)
                {
                    getter = new MapMaptypedPropertyGetter(name, eventType, eventAdapterService);
                }
                else
                {
                    getter = new MapMaptypedArrayPropertyGetter(name, eventType, eventAdapterService);
                }
                propertyGetters.put(name, getter);
                propertyDescriptors.add(new EventPropertyDescriptor(name, underlyingType, null, false, false, isArray, false, true));
                eventTypeFragments.put(name, new FragmentEventType(eventType, isArray, false));
                continue;
            }

            generateExceptionNestedProp(name, entry.getValue());
        }

        return new PropertySetDescriptor(propertyNameList, propertyDescriptors, simplePropertyTypes, propertyGetters, eventTypeFragments);
    }

    public EventPropertyDescriptor getPropertyDescriptor(String propertyName)
    {
        return propertyDescriptorMap.get(propertyName);
    }    

    public EventTypeMetadata getMetadata()
    {
        return metadata;
    }

    public FragmentEventType getFragmentType(String propertyName)
    {
        if (simpleFragmentTypes.containsKey(propertyName))  // may contain null values
        {
            return simpleFragmentTypes.get(propertyName);
        }

        // see if this is a nested property
        int index = ASTFilterSpecHelper.unescapedIndexOfDot(propertyName);
        if (index == -1)
        {
            // dynamic simple property
            if (propertyName.endsWith("?"))
            {
                return null;
            }

            // parse, can be an indexed property
            Property property = PropertyParser.parse(propertyName, false);
            if (property instanceof IndexedProperty)
            {
                IndexedProperty indexedProp = (IndexedProperty) property;
                Object type = nestableTypes.get(indexedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                else if (type instanceof EventType[])
                {
                    EventType eventType = ((EventType[]) type)[0];
                    return new FragmentEventType(eventType, false, false);
                }
                else if (type instanceof String)
                {
                    String propTypeName = type.toString();
                    boolean isArray = isPropertyArray(propTypeName);
                    if (!isArray) {
                        return null;
                    }
                    propTypeName = getPropertyRemoveArray(propTypeName);
                    EventType innerType = eventAdapterService.getExistsTypeByName(propTypeName);
                    if (!(innerType instanceof MapEventType))
                    {
                        return null;
                    }
                    return new FragmentEventType(innerType, false, false);  // false since an index is present
                }
                if (!(type instanceof Class))
                {
                    return null;
                }
                if (!((Class) type).isArray())
                {
                    return null;
                }
                // its an array
                return EventBeanUtility.createNativeFragmentType(((Class)type).getComponentType(), null, eventAdapterService);
            }
            else if (property instanceof MappedProperty)
            {
                // No type information available for the inner event
                return null;
            }
            else
            {
                return null;
            }
        }

        // Map event types allow 2 types of properties inside:
        //   - a property that is a Java object is interrogated via bean property getters and BeanEventType
        //   - a property that is a Map itself is interrogated via map property getters
        // The property getters therefore act on

        // Take apart the nested property into a map key and a nested value class property name
        String propertyMap = ASTFilterSpecHelper.unescapeDot(propertyName.substring(0, index));
        String propertyNested = propertyName.substring(index + 1, propertyName.length());

        // If the property is dynamic, it cannot be a fragment
        if (propertyMap.endsWith("?"))
        {
            return null;
        }

        Object nestedType = nestableTypes.get(propertyMap);
        if (nestedType == null)
        {
            // parse, can be an indexed property
            Property property = PropertyParser.parse(propertyMap, false);
            if (property instanceof IndexedProperty)
            {
                IndexedProperty indexedProp = (IndexedProperty) property;
                Object type = nestableTypes.get(indexedProp.getPropertyNameAtomic());
                if (type == null)
                {
                    return null;
                }
                // handle map-in-map case
                if (type instanceof String) {
                    String propTypeName = type.toString();
                    boolean isArray = isPropertyArray(propTypeName);
                    if (isArray) {
                        propTypeName = getPropertyRemoveArray(propTypeName);
                    }
                    EventType innerType = eventAdapterService.getExistsTypeByName(propTypeName);
                    if (!(innerType instanceof MapEventType))
                    {
                        return null;
                    }
                    return innerType.getFragmentType(propertyNested);
                }
                // handle eventtype[] in map
                else if (type instanceof EventType[])
                {
                    EventType innerType = ((EventType[]) type)[0];
                    return innerType.getFragmentType(propertyNested);
                }
                // handle array class in map case
                else
                {
                    if (!(type instanceof Class))
                    {
                        return null;
                    }
                    if (!((Class) type).isArray())
                    {
                        return null;
                    }
                    FragmentEventType fragmentParent = EventBeanUtility.createNativeFragmentType((Class) type, null, eventAdapterService);
                    if (fragmentParent == null)
                    {
                        return null;
                    }
                    return fragmentParent.getFragmentType().getFragmentType(propertyNested);
                }
            }
            else if (property instanceof MappedProperty)
            {
                // No type information available for the property's map value
                return null;
            }
            else
            {
                return null;
            }
        }

        // If there is a map value in the map, return the Object value if this is a dynamic property
        if (nestedType == Map.class)
        {
            return null;
        }
        else if (nestedType instanceof Map)
        {
            return null;
        }
        else if (nestedType instanceof Class)
        {
            Class simpleClass = (Class) nestedType;
            if (!JavaClassHelper.isFragmentableType(simpleClass))
            {
                return null;
            }
            EventType nestedEventType = eventAdapterService.getBeanEventTypeFactory().createBeanTypeDefaultName(simpleClass);
            return nestedEventType.getFragmentType(propertyNested);
        }
        else if (nestedType instanceof EventType)
        {
            EventType innerType = (EventType) nestedType;
            return innerType.getFragmentType(propertyNested);
        }
        else if (nestedType instanceof EventType[])
        {
            EventType[] innerType = (EventType[]) nestedType;
            return innerType[0].getFragmentType(propertyNested);
        }
        else if (nestedType instanceof String)
        {
            String nestedName = nestedType.toString();
            boolean isArray = isPropertyArray(nestedName);
            if (isArray) {
                nestedName = getPropertyRemoveArray(nestedName);
            }
            EventType innerType = eventAdapterService.getExistsTypeByName(nestedName);
            if (!(innerType instanceof MapEventType))
            {
                return null;
            }
            return innerType.getFragmentType(propertyNested);
        }
        else
        {
            String message = "Nestable map type configuration encountered an unexpected value type of '"
                + nestedType.getClass() + " for property '" + propertyName + "', expected Class, Map.class or Map<String, Object> as value type";
            throw new PropertyAccessException(message);
        }
    }

    /**
     * Returns true if the name indicates that the type is an array type.
     * @param name the property name
     * @return true if array type
     */
    public static boolean isPropertyArray(String name)
    {
        return name.trim().endsWith("[]");
    }

    /**
     * Returns the property name without the array type extension, if present.
     * @param name property name
     * @return property name with removed array extension name
     */
    public static String getPropertyRemoveArray(String name)
    {
        return name.replaceAll("\\[", "").replaceAll("\\]", "");
    }

    /**
     * Returns a message if the type, compared to this type, is not compatible in regards to the property numbers
     * and types.
     * @param otherType to compare to
     * @return message
     */
    public String getEqualsMessage(EventType otherType)
    {
        if (!(otherType instanceof MapEventType))
        {
            return "Type by name '" + otherType.getName() + "' is not a compatible type (target type underlying is '" + otherType.getUnderlyingType().getSimpleName() + "')";
        }

        MapEventType other = (MapEventType) otherType;

        if ((metadata.getTypeClass() != EventTypeMetadata.TypeClass.ANONYMOUS) && (!other.typeName.equals(this.typeName)))
        {
            return "Type by name '" + otherType.getName() + "' is not the same name";
        }

        return isDeepEqualsProperties(otherType.getName(), other.nestableTypes, this.nestableTypes);
    }

    public EventPropertyWriter getWriter(String propertyName)
    {
        if (writablePropertyDescriptors == null)
        {
            initializeWriters();
        }
        Pair<EventPropertyDescriptor, EventPropertyWriter> pair = propertyWriters.get(propertyName);
        if (pair == null)
        {
            return null;
        }
        return pair.getSecond();
    }

    public EventPropertyDescriptor getWritableProperty(String propertyName)
    {
        if (writablePropertyDescriptors == null)
        {
            initializeWriters();
        }
        Pair<EventPropertyDescriptor, EventPropertyWriter> pair = propertyWriters.get(propertyName);
        if (pair == null)
        {
            return null;
        }
        return pair.getFirst();
    }

    public EventPropertyDescriptor[] getWriteableProperties()
    {
        if (writablePropertyDescriptors == null)
        {
            initializeWriters();
        }
        return writablePropertyDescriptors;
    }

    private void initializeWriters()
    {
        List<EventPropertyDescriptor> writeableProps = new ArrayList<EventPropertyDescriptor>();
        Map<String, Pair<EventPropertyDescriptor, EventPropertyWriter>> propertWritersMap = new HashMap<String, Pair<EventPropertyDescriptor, EventPropertyWriter>>();
        for (EventPropertyDescriptor prop : propertyDescriptors)
        {
            if (prop.isFragment() || prop.isIndexed() || prop.isMapped())
            {
                continue;
            }
            writeableProps.add(prop);
            final String propertyName = prop.getPropertyName();
            EventPropertyWriter eventPropertyWriter = new EventPropertyWriter()
            {
                public void write(Object value, EventBean target)
                {
                    MappedEventBean map = (MappedEventBean) target;
                    map.getProperties().put(propertyName, value);
                }
            };

            propertWritersMap.put(propertyName, new Pair<EventPropertyDescriptor, EventPropertyWriter>(prop, eventPropertyWriter));
        }

        propertyWriters = propertWritersMap;
        writablePropertyDescriptors = writeableProps.toArray(new EventPropertyDescriptor[writeableProps.size()]);
    }

    public EventBeanWriter getWriter(String[] properties)
    {
        if (writablePropertyDescriptors == null)
        {
            initializeWriters();
        }
        for (int i = 0; i < properties.length; i++)
        {
            if (!propertyWriters.containsKey(properties[i]))
            {
                return null;
            }
        }
        return new MapEventBeanWriter(properties);
    }

    public EventBeanCopyMethod getCopyMethod(String[] properties)
    {
        return new MapEventBeanCopyMethod(this, eventAdapterService);
    }

    /**
     * Descriptor of a property set.
     */
    public static class PropertySetDescriptor
    {
        private final List<String> propertyNameList;
        private final List<EventPropertyDescriptor> propertyDescriptors;
        private final Map<String, Class> simplePropertyTypes;
        private final Map<String, MapEventPropertyGetter> propertyGetters;
        private final Map<String, FragmentEventType> simpleFragmentTypes;

        /**
         * Ctor.
         * @param propertyNameList property name list
         * @param simplePropertyTypes property types
         * @param propertyDescriptors property descriptors
         * @param propertyGetters property getters
         * @param simpleFragmentTypes fragment types per property 
         */
        public PropertySetDescriptor(List<String> propertyNameList, List<EventPropertyDescriptor> propertyDescriptors, Map<String, Class> simplePropertyTypes, Map<String, MapEventPropertyGetter> propertyGetters, Map<String, FragmentEventType> simpleFragmentTypes)
        {
            this.propertyNameList = propertyNameList;
            this.propertyDescriptors = propertyDescriptors;
            this.simplePropertyTypes = simplePropertyTypes;
            this.propertyGetters = propertyGetters;
            this.simpleFragmentTypes = simpleFragmentTypes;
        }

        /**
         * Returns map of property name and class.
         * @return property name and class
         */
        public Map<String, Class> getSimplePropertyTypes()
        {
            return simplePropertyTypes;
        }

        /**
         * Returns map of property name and getter.
         * @return property name and getter
         */
        public Map<String, MapEventPropertyGetter> getPropertyGetters()
        {
            return propertyGetters;
        }

        /**
         * Returns property name list.
         * @return property name list
         */
        public List<String> getPropertyNameList()
        {
            return propertyNameList;
        }

        /**
         * Returns the property descriptors.
         * @return property descriptors
         */
        public List<EventPropertyDescriptor> getPropertyDescriptors()
        {
            return propertyDescriptors;
        }

        /**
         * Returns the property fragment types.
         * @return fragment types.
         */
        public Map<String, FragmentEventType> getSimpleFragmentTypes()
        {
            return simpleFragmentTypes;
        }
    }
}
