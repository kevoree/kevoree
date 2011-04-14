/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

import com.espertech.esper.client.annotation.Name;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * An instance of <tt>Configuration</tt> allows the application
 * to specify properties to be used when
 * creating a <tt>EPServiceProvider</tt>. Usually an application will create
 * a single <tt>Configuration</tt>, then get one or more instances of
 * {@link EPServiceProvider} via {@link EPServiceProviderManager}.
 * The <tt>Configuration</tt> is meant
 * only as an initialization-time object. <tt>EPServiceProvider</tt>s are
 * immutable and do not retain any association back to the
 * <tt>Configuration</tt>.
 * <br>
 * The format of an Esper XML configuration file is defined in
 * <tt>esper-configuration-2.0.xsd</tt>.
 */
public class Configuration implements ConfigurationOperations, ConfigurationInformation, Serializable
{
    private static final long serialVersionUID = -220881974438617882L;
    private static Log log = LogFactory.getLog( Configuration.class );

    /**
     * Default name of the configuration file.
     */
    protected static final String ESPER_DEFAULT_CONFIG = "esper.cfg.xml";

    /**
     * Map of event name and fully-qualified class name.
     */
	protected Map<String, String> eventClasses;

    /**
     * Map of event type name and XML DOM configuration.
     */
	protected Map<String, ConfigurationEventTypeXMLDOM> eventTypesXMLDOM;

    /**
     * Map of event type name and Legacy-type event configuration.
     */
	protected Map<String, ConfigurationEventTypeLegacy> eventTypesLegacy;

	/**
	 * The type names for events that are backed by java.util.Map,
     * not containing strongly-typed nested maps.
	 */
	protected Map<String, Properties> mapNames;

    /**
     * The type names for events that are backed by java.util.Map,
     * possibly containing strongly-typed nested maps.
     * <p>
     * Each entrie's value must be either a Class or a Map<String,Object> to
     * define nested maps.
     */
    protected Map<String, Map<String, Object>> nestableMapNames;

    /**
     * Map event types that are subtypes of one or more Map event types
     */
    protected Map<String, Set<String>> mapSuperTypes;

	/**
	 * The class and package name imports that
	 * will be used to resolve partial class names.
	 */
	protected List<String> imports;

    /**
     * The class and package name imports that
     * will be used to resolve partial class names.
     */
    protected Map<String, ConfigurationDBRef> databaseReferences;

	/**
	 * True until the user calls addAutoImport().
	 */
	private boolean isUsingDefaultImports = true;

    /**
     * Optional classname to use for constructing services context.
     */
    protected String epServicesContextFactoryClassName;

    /**
     * List of configured plug-in views.
     */
    protected List<ConfigurationPlugInView> plugInViews;

    /**
     * List of configured plug-in pattern objects.
     */
    protected List<ConfigurationPlugInPatternObject> plugInPatternObjects;

    /**
     * List of configured plug-in aggregation functions.
     */
    protected List<ConfigurationPlugInAggregationFunction> plugInAggregationFunctions;

    /**
     * List of configured plug-in single-row functions.
     */
    protected List<ConfigurationPlugInSingleRowFunction> plugInSingleRowFunctions;

    /**
     * List of adapter loaders.
     */
    protected List<ConfigurationPluginLoader> pluginLoaders;

    /**
     * Saves engine default configs such as threading settings
     */
    protected ConfigurationEngineDefaults engineDefaults;

    /**
     * Saves the packages to search to resolve event type names.
     */
    protected Set<String> eventTypeAutoNamePackages;

    /**
     * Map of variables.
     */
    protected Map<String, ConfigurationVariable> variables;

    /**
     * Map of class name and configuration for method invocations on that class.
     */
	protected Map<String, ConfigurationMethodRef> methodInvocationReferences;

    /**
     * Map of plug-in event representation name and configuration
     */
	protected Map<URI, ConfigurationPlugInEventRepresentation> plugInEventRepresentation;

    /**
     * Map of plug-in event types.
     */
	protected Map<String, ConfigurationPlugInEventType> plugInEventTypes;

    /**
     * URIs that point to plug-in event representations that are given a chance to dynamically resolve an event type name to an
     * event type, as it occurs in a new EPL statement.
     */
    protected URI[] plugInEventTypeResolutionURIs;

    /**
     * All revision event types which allow updates to past events.
     */
    protected Map<String, ConfigurationRevisionEventType> revisionEventTypes;

    /**
     * Variant streams allow events of disparate types to be treated the same.
     */
    protected Map<String, ConfigurationVariantStream> variantStreams;

    /**
     * Constructs an empty configuration. The auto import values
     * are set by default to java.lang, java.math, java.text and
     * java.util.
     */
    public Configuration()
    {
        reset();
    }

    /**
     * Sets the class name of the services context factory class to use.
     * @param epServicesContextFactoryClassName service context factory class name
     */
    public void setEPServicesContextFactoryClassName(String epServicesContextFactoryClassName)
    {
        this.epServicesContextFactoryClassName = epServicesContextFactoryClassName;
    }

    public String getEPServicesContextFactoryClassName()
    {
        return epServicesContextFactoryClassName;
    }

    public void addPlugInAggregationFunction(String functionName, String aggregationClassName)
    {
        ConfigurationPlugInAggregationFunction entry = new ConfigurationPlugInAggregationFunction();
        entry.setFunctionClassName(aggregationClassName);
        entry.setName(functionName);
        plugInAggregationFunctions.add(entry);
    }

    public void addPlugInSingleRowFunction(String functionName, String className, String methodName) throws ConfigurationException
    {
        ConfigurationPlugInSingleRowFunction entry = new ConfigurationPlugInSingleRowFunction();
        entry.setFunctionClassName(className);
        entry.setFunctionMethodName(methodName);
        entry.setName(functionName);
        plugInSingleRowFunctions.add(entry);
    }

    /**
     * Checks if an event type has already been registered for that name.
     * @since 2.1
     * @param eventTypeName the name
     * @return true if already registered
     */
    public boolean isEventTypeExists(String eventTypeName) {
        return eventClasses.containsKey(eventTypeName)
                || mapNames.containsKey(eventTypeName)
                || nestableMapNames.containsKey(eventTypeName)
                || eventTypesXMLDOM.containsKey(eventTypeName);
        //note: no need to check legacy as they get added as class event type
    }

    /**
     * Add an name for an event type represented by Java-bean plain-old Java object events.
     * @param eventTypeName is the name for the event type
     * @param eventClassName fully-qualified class name of the event type
     */
    public void addEventType(String eventTypeName, String eventClassName)
    {
        eventClasses.put(eventTypeName, eventClassName);
    }

    /**
     * Add an name for an event type represented by Java-bean plain-old Java object events.
     * @param eventTypeName is the name for the event type
     * @param eventClass is the Java event class for which to add the name
     */
    public void addEventType(String eventTypeName, Class eventClass)
    {
        addEventType(eventTypeName, eventClass.getName());
    }

    /**
     * Add an name for an event type represented by Java-bean plain-old Java object events,
     * and the name is the simple class name of the class.
     * @param eventClass is the Java event class for which to add the name
     */
    public void addEventType(Class eventClass)
    {
        addEventType(eventClass.getSimpleName(), eventClass.getName());
    }

    /**
     * Add an name for an event type that represents java.util.Map events.
     * <p>
     * Each entry in the type map is the property name and the fully-qualified
     * Java class name or primitive type name.
     * @param eventTypeName is the name for the event type
     * @param typeMap maps the name of each property in the Map event to the type
     * (fully qualified classname) of its value in Map event instances.
     */
    public void addEventType(String eventTypeName, Properties typeMap)
    {
    	mapNames.put(eventTypeName, typeMap);
    }

    public void addEventType(String eventTypeName, Map<String, Object> typeMap)
    {
        nestableMapNames.put(eventTypeName, typeMap);
    }

    public void addEventType(String eventTypeName, Map<String, Object> typeMap, String[] superTypes)
    {
        nestableMapNames.put(eventTypeName, typeMap);
        if (superTypes != null)
        {
            for (int i = 0; i < superTypes.length; i++)
            {
                this.addMapSuperType(eventTypeName, superTypes[i]);
            }
        }
    }

    /**
     * Add, for a given Map event type identified by the first parameter, the supertype (by its event type name).
     * <p>
     * Each Map event type may have any number of supertypes, each supertype must also be of a Map-type event.
     * @param mapeventTypeName the name of a Map event type, that is to have a supertype
     * @param mapSupertypeName the name of a Map event type that is the supertype
     */
    public void addMapSuperType(String mapeventTypeName, String mapSupertypeName)
    {
        Set<String> superTypes = mapSuperTypes.get(mapeventTypeName);
        if (superTypes == null)
        {
            superTypes = new HashSet<String>();
            mapSuperTypes.put(mapeventTypeName, superTypes);
        }
        superTypes.add(mapSupertypeName);
    }

    /**
     * Add an name for an event type that represents org.w3c.dom.Node events.
     * @param eventTypeName is the name for the event type
     * @param xmlDOMEventTypeDesc descriptor containing property and mapping information for XML-DOM events
     */
    public void addEventType(String eventTypeName, ConfigurationEventTypeXMLDOM xmlDOMEventTypeDesc)
    {
        eventTypesXMLDOM.put(eventTypeName, xmlDOMEventTypeDesc);
    }

    public void addRevisionEventType(String revisioneventTypeName, ConfigurationRevisionEventType revisionEventTypeConfig)
    {
        revisionEventTypes.put(revisioneventTypeName, revisionEventTypeConfig);
    }

    /**
     * Add a database reference with a given database name.
     * @param name is the database name
     * @param configurationDBRef descriptor containing database connection and access policy information
     */
    public void addDatabaseReference(String name, ConfigurationDBRef configurationDBRef)
    {
        databaseReferences.put(name, configurationDBRef);
    }

    /**
     * Add an name for an event type that represents legacy Java type (non-JavaBean style) events.
     * @param eventTypeName is the name for the event type
     * @param eventClass fully-qualified class name of the event type
     * @param legacyEventTypeDesc descriptor containing property and mapping information for Legacy Java type events
     */
    public void addEventType(String eventTypeName, String eventClass, ConfigurationEventTypeLegacy legacyEventTypeDesc)
    {
        eventClasses.put(eventTypeName, eventClass);
        eventTypesLegacy.put(eventTypeName, legacyEventTypeDesc);
    }

    public void addImport(String autoImport)
    {
		if(isUsingDefaultImports)
		{
			isUsingDefaultImports = false;
			imports.clear();
            imports.add(Name.class.getPackage().getName() + ".*");
		}
    	imports.add(autoImport);
    }

    public void addImport(Class autoImport)
    {
        addImport(autoImport.getName());
    }

    /**
     * Adds a cache configuration for a class providing methods for use in the from-clause.
     * @param className is the class name (simple or fully-qualified) providing methods
     * @param methodInvocationConfig is the cache configuration
     */
    public void addMethodRef(String className, ConfigurationMethodRef methodInvocationConfig)
    {
        this.methodInvocationReferences.put(className, methodInvocationConfig);
    }

    /**
     * Adds a cache configuration for a class providing methods for use in the from-clause.
     * @param clazz is the class providing methods
     * @param methodInvocationConfig is the cache configuration
     */
    public void addMethodRef(Class clazz, ConfigurationMethodRef methodInvocationConfig)
    {
        this.methodInvocationReferences.put(clazz.getName(), methodInvocationConfig);
    }

    public Map<String, String> getEventTypeNames()
    {
        return eventClasses;
    }

    public Map<String, Properties> getEventTypesMapEvents()
    {
    	return mapNames;
    }

    public Map<String, Map<String, Object>> getEventTypesNestableMapEvents()
    {
    	return nestableMapNames;
    }

    public Map<String, ConfigurationEventTypeXMLDOM> getEventTypesXMLDOM()
    {
        return eventTypesXMLDOM;
    }

    public Map<String, ConfigurationEventTypeLegacy> getEventTypesLegacy()
    {
        return eventTypesLegacy;
    }

	public List<String> getImports()
	{
		return imports;
	}

    public Map<String, ConfigurationDBRef> getDatabaseReferences()
    {
        return databaseReferences;
    }

    public List<ConfigurationPlugInView> getPlugInViews()
    {
        return plugInViews;
    }

    public List<ConfigurationPluginLoader> getPluginLoaders()
    {
        return pluginLoaders;
    }

    public List<ConfigurationPlugInAggregationFunction> getPlugInAggregationFunctions()
    {
        return plugInAggregationFunctions;
    }

    public List<ConfigurationPlugInSingleRowFunction> getPlugInSingleRowFunctions()
    {
        return plugInSingleRowFunctions;
    }

    public List<ConfigurationPlugInPatternObject> getPlugInPatternObjects()
    {
        return plugInPatternObjects;
    }

    public Map<String, ConfigurationVariable> getVariables()
    {
        return variables;
    }

    public Map<String, ConfigurationMethodRef> getMethodInvocationReferences()
    {
        return methodInvocationReferences;
    }

    public Map<String, ConfigurationRevisionEventType> getRevisionEventTypes()
    {
        return revisionEventTypes;
    }

    public Map<String, Set<String>> getMapSuperTypes()
    {
        return mapSuperTypes;
    }

    /**
     * Add a plugin loader (f.e. an input/output adapter loader).
     * @param loaderName is the name of the loader
     * @param className is the fully-qualified classname of the loader class
     * @param configuration is loader cofiguration entries
     */
    public void addPluginLoader(String loaderName, String className, Properties configuration)
    {
        addPluginLoader(loaderName, className, configuration, null);
    }

    /**
     * Add a plugin loader (f.e. an input/output adapter loader).
     * @param loaderName is the name of the loader
     * @param className is the fully-qualified classname of the loader class
     * @param configuration is loader cofiguration entries
     * @param configurationXML config xml if any
     */
    public void addPluginLoader(String loaderName, String className, Properties configuration, String configurationXML)
    {
        ConfigurationPluginLoader pluginLoader = new ConfigurationPluginLoader();
        pluginLoader.setLoaderName(loaderName);
        pluginLoader.setClassName(className);
        pluginLoader.setConfigProperties(configuration);
        pluginLoader.setConfigurationXML(configurationXML);
        pluginLoaders.add(pluginLoader);
    }

    /**
     * Add a view for plug-in.
     * @param namespace is the namespace the view should be available under
     * @param name is the name of the view
     * @param viewFactoryClass is the view factory class to use
     */
    public void addPlugInView(String namespace, String name, String viewFactoryClass)
    {
        ConfigurationPlugInView configurationPlugInView = new ConfigurationPlugInView();
        configurationPlugInView.setNamespace(namespace);
        configurationPlugInView.setName(name);
        configurationPlugInView.setFactoryClassName(viewFactoryClass);
        plugInViews.add(configurationPlugInView);
    }

    /**
     * Add a pattern event observer for plug-in.
     * @param namespace is the namespace the observer should be available under
     * @param name is the name of the observer
     * @param observerFactoryClass is the observer factory class to use
     */
    public void addPlugInPatternObserver(String namespace, String name, String observerFactoryClass)
    {
        ConfigurationPlugInPatternObject entry = new ConfigurationPlugInPatternObject();
        entry.setNamespace(namespace);
        entry.setName(name);
        entry.setFactoryClassName(observerFactoryClass);
        entry.setPatternObjectType(ConfigurationPlugInPatternObject.PatternObjectType.OBSERVER);
        plugInPatternObjects.add(entry);
    }

    /**
     * Add a pattern guard for plug-in.
     * @param namespace is the namespace the guard should be available under
     * @param name is the name of the guard
     * @param guardFactoryClass is the guard factory class to use
     */
    public void addPlugInPatternGuard(String namespace, String name, String guardFactoryClass)
    {
        ConfigurationPlugInPatternObject entry = new ConfigurationPlugInPatternObject();
        entry.setNamespace(namespace);
        entry.setName(name);
        entry.setFactoryClassName(guardFactoryClass);
        entry.setPatternObjectType(ConfigurationPlugInPatternObject.PatternObjectType.GUARD);
        plugInPatternObjects.add(entry);
    }

    public void addEventTypeAutoName(String packageName)
    {
        eventTypeAutoNamePackages.add(packageName);
    }

    public void addVariable(String variableName, Class type, Object initializationValue)
    {
        ConfigurationVariable configVar = new ConfigurationVariable();
        configVar.setType(type.getName());
        configVar.setInitializationValue(initializationValue);
        variables.put(variableName, configVar);
    }

    public void addVariable(String variableName, String type, Object initializationValue) throws ConfigurationException
    {
        ConfigurationVariable configVar = new ConfigurationVariable();
        configVar.setType(type);
        configVar.setInitializationValue(initializationValue);
        variables.put(variableName, configVar);
    }

    /**
     * Adds an event representation responsible for creating event types (event metadata) and event bean instances (events) for
     * a certain kind of object representation that holds the event property values.
     * @param eventRepresentationRootURI uniquely identifies the event representation and acts as a parent
     * for child URIs used in resolving
     * @param eventRepresentationClassName is the name of the class implementing {@link com.espertech.esper.plugin.PlugInEventRepresentation}.
     * @param initializer is optional configuration or initialization information, or null if none required 
     */
    public void addPlugInEventRepresentation(URI eventRepresentationRootURI, String eventRepresentationClassName, Serializable initializer)
    {
        ConfigurationPlugInEventRepresentation config = new ConfigurationPlugInEventRepresentation();
        config.setEventRepresentationClassName(eventRepresentationClassName);
        config.setInitializer(initializer);
        this.plugInEventRepresentation.put(eventRepresentationRootURI, config);
    }

    /**
     * Adds an event representation responsible for creating event types (event metadata) and event bean instances (events) for
     * a certain kind of object representation that holds the event property values.
     * @param eventRepresentationRootURI uniquely identifies the event representation and acts as a parent
     * for child URIs used in resolving
     * @param eventRepresentationClass is the class implementing {@link com.espertech.esper.plugin.PlugInEventRepresentation}.
     * @param initializer is optional configuration or initialization information, or null if none required
     */
    public void addPlugInEventRepresentation(URI eventRepresentationRootURI, Class eventRepresentationClass, Serializable initializer)
    {
        addPlugInEventRepresentation(eventRepresentationRootURI, eventRepresentationClass.getName(), initializer);
    }

    public void addPlugInEventType(String eventTypeName, URI[] resolutionURIs, Serializable initializer)
    {
        ConfigurationPlugInEventType config = new ConfigurationPlugInEventType();
        config.setEventRepresentationResolutionURIs(resolutionURIs);
        config.setInitializer(initializer);
        plugInEventTypes.put(eventTypeName, config);
    }

    public void setPlugInEventTypeResolutionURIs(URI[] urisToResolveName)
    {
        plugInEventTypeResolutionURIs = urisToResolveName;
    }

    public URI[] getPlugInEventTypeResolutionURIs()
    {
        return plugInEventTypeResolutionURIs;
    }

    public Map<URI, ConfigurationPlugInEventRepresentation> getPlugInEventRepresentation()
    {
        return plugInEventRepresentation;
    }

    public Map<String, ConfigurationPlugInEventType> getPlugInEventTypes()
    {
        return plugInEventTypes;
    }

    public Set<String> getEventTypeAutoNamePackages()
    {
        return eventTypeAutoNamePackages;
    }

    public ConfigurationEngineDefaults getEngineDefaults()
    {
        return engineDefaults;
    }

    public void addVariantStream(String varianteventTypeName, ConfigurationVariantStream variantStreamConfig)
    {
        variantStreams.put(varianteventTypeName, variantStreamConfig);
    }

    public Map<String, ConfigurationVariantStream> getVariantStreams()
    {
        return variantStreams;
    }

    public void updateMapEventType(String mapeventTypeName, Map<String, Object> typeMap) throws ConfigurationException
    {
        throw new UnsupportedOperationException("Map type update is only available in runtime configuration");
    }

    public void replaceXMLEventType(String xmlEventTypeName, ConfigurationEventTypeXMLDOM config) throws ConfigurationException {
        throw new UnsupportedOperationException("XML type update is only available in runtime configuration");
    }

    public Set<String> getEventTypeNameUsedBy(String name)
    {
        throw new UnsupportedOperationException("Get event type by name is only available in runtime configuration");
    }

    public boolean isVariantStreamExists(String name)
    {
        return variantStreams.containsKey(name);
    }

    public void setMetricsReportingInterval(String stmtGroupName, long newInterval)
    {
        this.getEngineDefaults().getMetricsReporting().setStatementGroupInterval(stmtGroupName, newInterval);
    }

    public void setMetricsReportingStmtEnabled(String statementName)
    {
        throw new UnsupportedOperationException("Statement metric reporting can only be enabled or disabled at runtime");
    }

    public void setMetricsReportingStmtDisabled(String statementName)
    {
        throw new UnsupportedOperationException("Statement metric reporting can only be enabled or disabled at runtime");
    }

    public void setMetricsReportingEnabled()
    {
        this.getEngineDefaults().getMetricsReporting().setEnableMetricsReporting(true);
    }

    public void setMetricsReportingDisabled()
    {
        this.getEngineDefaults().getMetricsReporting().setEnableMetricsReporting(false);
    }

    /**
	 * Use the configuration specified in an application
	 * resource named <tt>esper.cfg.xml</tt>.
     * @return Configuration initialized from the resource
     * @throws EPException thrown to indicate error reading configuration
     */
	public Configuration configure() throws EPException
    {
		configure('/' + ESPER_DEFAULT_CONFIG);
		return this;
	}

    /**
     * Use the configuration specified in the given application
     * resource. The format of the resource is defined in
     * <tt>esper-configuration-2.0.xsd</tt>.
     * <p/>
     * The resource is found via <tt>getConfigurationInputStream(resource)</tt>.
     * That method can be overridden to implement an arbitrary lookup strategy.
     * <p/>
     * See <tt>getResourceAsStream</tt> for information on how the resource name is resolved.
     * @param resource if the file name of the resource
     * @return Configuration initialized from the resource
     * @throws EPException thrown to indicate error reading configuration
     */
    public Configuration configure(String resource) throws EPException
    {
        if (log.isDebugEnabled())
        {
            log.debug( "Configuring from resource: " + resource );
        }
        InputStream stream = getConfigurationInputStream(resource );
        ConfigurationParser.doConfigure(this, stream, resource );
        return this;
    }

    /**
     * Get the configuration file as an <tt>InputStream</tt>. Might be overridden
     * by subclasses to allow the configuration to be located by some arbitrary
     * mechanism.
     * <p>
     * See <tt>getResourceAsStream</tt> for information on how the resource name is resolved.
     * @param resource is the resource name
     * @return input stream for resource
     * @throws EPException thrown to indicate error reading configuration
     */
    protected static InputStream getConfigurationInputStream(String resource) throws EPException
    {
        return getResourceAsStream(resource);
    }


	/**
	 * Use the configuration specified by the given URL.
	 * The format of the document obtained from the URL is defined in
	 * <tt>esper-configuration-2.0.xsd</tt>.
	 *
	 * @param url URL from which you wish to load the configuration
	 * @return A configuration configured via the file
	 * @throws EPException is thrown when the URL could not be access
	 */
	public Configuration configure(URL url) throws EPException
    {
        if (log.isDebugEnabled())
        {
            log.debug( "configuring from url: " + url.toString() );
        }
        try {
            ConfigurationParser.doConfigure(this, url.openStream(), url.toString());
            return this;
		}
		catch (IOException ioe) {
			throw new EPException("could not configure from URL: " + url, ioe );
		}
	}

	/**
	 * Use the configuration specified in the given application
	 * file. The format of the file is defined in
	 * <tt>esper-configuration-2.0.xsd</tt>.
	 *
	 * @param configFile <tt>File</tt> from which you wish to load the configuration
	 * @return A configuration configured via the file
	 * @throws EPException when the file could not be found
	 */
	public Configuration configure(File configFile) throws EPException
    {
        if (log.isDebugEnabled())
        {
            log.debug( "configuring from file: " + configFile.getName() );
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
            ConfigurationParser.doConfigure(this, inputStream, configFile.toString());
		}
		catch (FileNotFoundException fnfe) {
			throw new EPException( "could not find file: " + configFile, fnfe );
		}
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.debug("Error closing input stream", e);
                }
            }
        }
        return this;
    }

    public boolean removeEventType(String eventTypeName, boolean force) throws ConfigurationException
    {
        eventClasses.remove(eventTypeName);
        eventTypesXMLDOM.remove(eventTypeName);
        eventTypesLegacy.remove(eventTypeName);
        mapNames.remove(eventTypeName);
        nestableMapNames.remove(eventTypeName);
        mapSuperTypes.remove(eventTypeName);
        plugInEventTypes.remove(eventTypeName);
        revisionEventTypes.remove(eventTypeName);
        variantStreams.remove(eventTypeName);
        return true;
    }

    public Set<String> getVariableNameUsedBy(String variableName) {
        throw new UnsupportedOperationException("Get variable use information is only available in runtime configuration");
    }

    public boolean removeVariable(String name, boolean force) throws ConfigurationException {
        return this.variables.remove(name) != null;
    }

    /**
	 * Use the mappings and properties specified in the given XML document.
	 * The format of the file is defined in
	 * <tt>esper-configuration-2.0.xsd</tt>.
	 *
	 * @param document an XML document from which you wish to load the configuration
	 * @return A configuration configured via the <tt>Document</tt>
	 * @throws EPException if there is problem in accessing the document.
	 */
	public Configuration configure(Document document) throws EPException
    {
        if (log.isDebugEnabled())
        {
		    log.debug( "configuring from XML document" );
        }
        ConfigurationParser.doConfigure(this, document);
        return this;
    }

    /**
     * Returns an input stream from an application resource in the classpath.
     * <p>
     * The method first removes the '/' character from the resource name if
     * the first character is '/'.
     * <p>
     * The lookup order is as follows:
     * <p>
     * If a thread context class loader exists, use <tt>Thread.currentThread().getResourceAsStream</tt>
     * to obtain an InputStream.
     * <p>
     * If no input stream was returned, use the <tt>Configuration.class.getResourceAsStream</tt>.
     * to obtain an InputStream.
     * <p>
     * If no input stream was returned, use the <tt>Configuration.class.getClassLoader().getResourceAsStream</tt>.
     * to obtain an InputStream.
     * <p>
     * If no input stream was returned, throw an Exception.
     *
     * @param resource to get input stream for
     * @return input stream for resource
     */
    protected static InputStream getResourceAsStream(String resource)
    {
        String stripped = resource.startsWith("/") ?
                resource.substring(1) : resource;

        InputStream stream = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader!=null) {
            stream = classLoader.getResourceAsStream( stripped );
        }
        if ( stream == null ) {
            stream = Configuration.class.getResourceAsStream( resource );
        }
        if ( stream == null ) {
            stream = Configuration.class.getClassLoader().getResourceAsStream( stripped );
        }
        if ( stream == null ) {
            throw new EPException( resource + " not found" );
        }
        return stream;
    }

    /**
     * Reset to an empty configuration.
     */
    protected void reset()
    {
        eventClasses = new HashMap<String, String>();
        mapNames = new HashMap<String, Properties>();
        nestableMapNames = new HashMap<String, Map<String, Object>>();
        eventTypesXMLDOM = new HashMap<String, ConfigurationEventTypeXMLDOM>();
        eventTypesLegacy = new HashMap<String, ConfigurationEventTypeLegacy>();
        databaseReferences = new HashMap<String, ConfigurationDBRef>();
        imports = new ArrayList<String>();
        addDefaultImports();
        isUsingDefaultImports = true;
        plugInViews = new ArrayList<ConfigurationPlugInView>();
        pluginLoaders = new ArrayList<ConfigurationPluginLoader>();
        plugInAggregationFunctions = new ArrayList<ConfigurationPlugInAggregationFunction>();
        plugInSingleRowFunctions = new ArrayList<ConfigurationPlugInSingleRowFunction>();
        plugInPatternObjects = new ArrayList<ConfigurationPlugInPatternObject>();
        engineDefaults = new ConfigurationEngineDefaults();
        eventTypeAutoNamePackages = new LinkedHashSet<String>();
        variables = new HashMap<String, ConfigurationVariable>();
        methodInvocationReferences = new HashMap<String, ConfigurationMethodRef>();
        plugInEventRepresentation = new HashMap<URI, ConfigurationPlugInEventRepresentation>();
        plugInEventTypes = new HashMap<String, ConfigurationPlugInEventType>();
        revisionEventTypes = new HashMap<String, ConfigurationRevisionEventType>();
        variantStreams = new HashMap<String, ConfigurationVariantStream>();
        mapSuperTypes = new HashMap<String, Set<String>>();
    }

    /**
     * Use these imports until the user specifies something else.
     */
    private void addDefaultImports()
    {
    	imports.add("java.lang.*");
    	imports.add("java.math.*");
    	imports.add("java.text.*");
    	imports.add("java.util.*");
        imports.add("com.espertech.esper.client.annotation.*");
    }

    /**
     * Enumeration of different resolution styles for resolving property names.
     */
    public static enum PropertyResolutionStyle
    {
        /**
         * Properties are only matched if the names are identical in name
         * and case to the original property name.
         */
        CASE_SENSITIVE,

        /**
         * Properties are matched if the names are identical.  A case insensitive
         * search is used and will choose the first property that matches
         * the name exactly or the first property that matches case insensitively
         * should no match be found.
         */
        CASE_INSENSITIVE,

        /**
         * Properties are matched if the names are identical.  A case insensitive
         * search is used and will choose the first property that matches
         * the name exactly case insensitively.  If more than one 'name' can be
         * mapped to the property an exception is thrown.
         */
        DISTINCT_CASE_INSENSITIVE;

        /**
         * Returns the default property resolution style.
         * @return is the case-sensitive resolution
         */
        public static PropertyResolutionStyle getDefault()
        {
            return CASE_SENSITIVE;
        }
    }
}
