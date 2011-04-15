/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.client.ConfigurationPlugInView;
import com.espertech.esper.client.ConfigurationException;
import com.espertech.esper.client.ConfigurationPlugInPatternObject;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Repository for pluggable objects of different types that follow a "namespace:name" notation.
 */
public class PluggableObjectCollection
{
    // Map of namespace, name and class plus type
    private Map<String, Map<String, Pair<Class, PluggableObjectType>>> pluggables;

    /**
     * Ctor.
     */
    public PluggableObjectCollection()
    {
        pluggables = new HashMap<String, Map<String, Pair<Class, PluggableObjectType>>>();
    }

    /**
     * Add a plug-in view.
     * @param configurationPlugInViews is a list of configured plug-in view objects.
     * @throws ConfigurationException if the configured views don't resolve
     */
    public void addViews(List<ConfigurationPlugInView> configurationPlugInViews) throws ConfigurationException
    {
        initViews(configurationPlugInViews);
    }

    /**
     * Add a plug-in pattern object.
     * @param configPattern is a list of configured plug-in pattern objects.
     * @throws ConfigurationException if the configured patterns don't resolve
     */
    public void addPatternObjects(List<ConfigurationPlugInPatternObject> configPattern) throws ConfigurationException
    {
        initPatterns(configPattern);
    }

    /**
     * Add the plug-in objects for another collection.
     * @param other is the collection to add
     */
    public void addObjects(PluggableObjectCollection other)
    {
        for (Map.Entry<String, Map<String, Pair<Class, PluggableObjectType>>> entry : other.getPluggables().entrySet())
        {
            Map<String, Pair<Class, PluggableObjectType>> namespaceMap = pluggables.get(entry.getKey());
            if (namespaceMap == null)
            {
                namespaceMap = new HashMap<String, Pair<Class, PluggableObjectType>>();
                pluggables.put(entry.getKey(), namespaceMap);
            }

            for (String name : entry.getValue().keySet())
            {
                if (namespaceMap.containsKey(name))
                {
                    throw new ConfigurationException("Duplicate object detected in namespace '" + entry.getKey() +
                                "' by name '" + name + "'");
                }
            }

            namespaceMap.putAll(entry.getValue());
        }
    }

    /**
     * Add a single object to the collection.
     * @param namespace is the object's namespace
     * @param name is the object's name
     * @param clazz is the class the object resolves to
     * @param type is the object type
     */
    public void addObject(String namespace, String name, Class clazz, PluggableObjectType type)
    {
        Map<String, Pair<Class, PluggableObjectType>> namespaceMap = pluggables.get(namespace);
        if (namespaceMap == null)
        {
            namespaceMap = new HashMap<String, Pair<Class, PluggableObjectType>>();
            pluggables.put(namespace, namespaceMap);
        }
        namespaceMap.put(name, new Pair<Class, PluggableObjectType>(clazz, type));
    }

    /**
     * Returns the underlying nested map of namespace keys and name-to-object maps.
     * @return pluggable object collected
     */
    public Map<String, Map<String, Pair<Class, PluggableObjectType>>> getPluggables()
    {
        return pluggables;
    }

    private void initViews(List<ConfigurationPlugInView> configurationPlugInViews)
    {
        if (configurationPlugInViews == null)
        {
            return;
        }

        for (ConfigurationPlugInView entry : configurationPlugInViews)
        {
            if (entry.getFactoryClassName() == null)
            {
                throw new ConfigurationException("Factory class name has not been supplied for object '" + entry.getName() + "'");
            }
            if (entry.getNamespace() == null)
            {
                throw new ConfigurationException("Namespace name has not been supplied for object '" + entry.getName() + "'");
            }
            if (entry.getName() == null)
            {
                throw new ConfigurationException("Name has not been supplied for object in namespace '" + entry.getNamespace() + "'");
            }

            Class clazz;
            try
            {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                clazz = Class.forName(entry.getFactoryClassName(), true, cl);
            }
            catch (ClassNotFoundException ex)
            {
                throw new ConfigurationException("View factory class " + entry.getFactoryClassName() + " could not be loaded");
            }

            Map<String, Pair<Class, PluggableObjectType>> namespaceMap = pluggables.get(entry.getNamespace());
            if (namespaceMap == null)
            {
                namespaceMap = new HashMap<String, Pair<Class, PluggableObjectType>>();
                pluggables.put(entry.getNamespace(), namespaceMap);
            }
            namespaceMap.put(entry.getName(), new Pair<Class, PluggableObjectType>(clazz, PluggableObjectType.VIEW));
        }
    }

    private void initPatterns(List<ConfigurationPlugInPatternObject> configEntries) throws ConfigurationException
    {
        if (configEntries == null)
        {
            return;
        }

        for (ConfigurationPlugInPatternObject entry : configEntries)
        {
            if (entry.getFactoryClassName() == null)
            {
                throw new ConfigurationException("Factory class name has not been supplied for object '" + entry.getName() + "'");
            }
            if (entry.getNamespace() == null)
            {
                throw new ConfigurationException("Namespace name has not been supplied for object '" + entry.getName() + "'");
            }
            if (entry.getName() == null)
            {
                throw new ConfigurationException("Name has not been supplied for object in namespace '" + entry.getNamespace() + "'");
            }
            if (entry.getPatternObjectType() == null)
            {
                throw new ConfigurationException("Pattern object type has not been supplied for object '" + entry.getName() + "'");
            }

            Class clazz;
            try
            {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                clazz = Class.forName(entry.getFactoryClassName(), true, cl);
            }
            catch (ClassNotFoundException ex)
            {
                throw new ConfigurationException("Pattern object factory class " + entry.getFactoryClassName() + " could not be loaded");
            }

            Map<String, Pair<Class, PluggableObjectType>> namespaceMap = pluggables.get(entry.getNamespace());
            if (namespaceMap == null)
            {
                namespaceMap = new HashMap<String, Pair<Class, PluggableObjectType>>();
                pluggables.put(entry.getNamespace(), namespaceMap);
            }

            PluggableObjectType typeEnum;
            if (entry.getPatternObjectType() == ConfigurationPlugInPatternObject.PatternObjectType.GUARD)
            {
                typeEnum =  PluggableObjectType.PATTERN_GUARD;
            }
            else if (entry.getPatternObjectType() == ConfigurationPlugInPatternObject.PatternObjectType.OBSERVER)
            {
                typeEnum =  PluggableObjectType.PATTERN_OBSERVER;
            }
            else
            {
                throw new IllegalArgumentException("Pattern object type '" + entry.getPatternObjectType() + "' not known");
            }
            namespaceMap.put(entry.getName(), new Pair<Class, PluggableObjectType>(clazz, typeEnum));
        }
    }

}
