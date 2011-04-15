/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.client.ConfigurationMethodRef;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.agg.AggregationSupport;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.MethodResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for engine-level imports.
 */
public class EngineImportServiceImpl implements EngineImportService
{
    private static final Log log = LogFactory.getLog(EngineImportServiceImpl.class);
    
	private final List<String> imports;
    private final Map<String, String> aggregationFunctions;
    private final Map<String, Pair<String, String>> singleRowFunctions;
    private final Map<String, ConfigurationMethodRef> methodInvocationRef;
    private final boolean allowExtendedAggregationFunc;

    /**
	 * Ctor
     * @param allowExtendedAggregationFunc true to allow non-SQL standard builtin agg functions.
	 */
	public EngineImportServiceImpl(boolean allowExtendedAggregationFunc)
    {
        imports = new ArrayList<String>();
        aggregationFunctions = new HashMap<String, String>();
        singleRowFunctions = new HashMap<String, Pair<String, String>>();
        methodInvocationRef = new HashMap<String, ConfigurationMethodRef>();
        this.allowExtendedAggregationFunc = allowExtendedAggregationFunc;
    }

    public ConfigurationMethodRef getConfigurationMethodRef(String className)
    {
        return methodInvocationRef.get(className);
    }

    /**
     * Adds cache configs for method invocations for from-clause.
     * @param configs cache configs
     */
    public void addMethodRefs(Map<String, ConfigurationMethodRef> configs)
    {
        methodInvocationRef.putAll(configs);
    }

    public void addImport(String importName) throws EngineImportException
    {
        if(!isClassName(importName) && !isPackageName(importName))
        {
            throw new EngineImportException("Invalid import name '" + importName + "'");
        }
        if (log.isDebugEnabled()) {
            log.debug("Adding import " + importName);
        }

        imports.add(importName);
    }

    public void addAggregation(String functionName, String aggregationClass) throws EngineImportException
    {
        String existing = aggregationFunctions.get(functionName);
        if (existing != null)
        {
            throw new EngineImportException("Aggregation function by name '" + functionName + "' is already defined");
        }
        if (singleRowFunctions.containsKey(functionName))
        {
            throw new EngineImportException("Single-row function by name '" + functionName + "' is already defined");
        }
        if(!isFunctionName(functionName))
        {
            throw new EngineImportException("Invalid aggregation function name '" + functionName + "'");
        }
        if(!isClassName(aggregationClass))
        {
            throw new EngineImportException("Invalid class name for aggregation '" + aggregationClass + "'");
        }
        aggregationFunctions.put(functionName.toLowerCase(), aggregationClass);
    }

    public void addSingleRow(String functionName, String singleRowFuncClass, String methodName) throws EngineImportException
    {
        Pair<String, String> existing = singleRowFunctions.get(functionName);
        if (existing != null)
        {
            throw new EngineImportException("Single-Row function by name '" + functionName + "' is already defined");
        }
        if (aggregationFunctions.containsKey(functionName))
        {
            throw new EngineImportException("Aggregation function by name '" + functionName + "' is already defined");
        }
        if(!isFunctionName(functionName))
        {
            throw new EngineImportException("Invalid single-row function name '" + functionName + "'");
        }
        if(!isClassName(singleRowFuncClass))
        {
            throw new EngineImportException("Invalid class name for aggregation '" + singleRowFuncClass + "'");
        }
        singleRowFunctions.put(functionName.toLowerCase(), new Pair<String, String>(singleRowFuncClass, methodName));
    }

    public AggregationSupport resolveAggregation(String name) throws EngineImportException, EngineImportUndefinedException
    {
        String className = aggregationFunctions.get(name);
        if (className == null)
        {
            className = aggregationFunctions.get(name.toLowerCase());
        }
        if (className == null)
        {
            throw new EngineImportUndefinedException("A function named '" + name + "' is not defined");
        }

        Class clazz;
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            clazz = Class.forName(className, true, cl);
        }
        catch (ClassNotFoundException ex)
        {
            throw new EngineImportException("Could not load aggregation class by name '" + className + "'", ex);
        }

        Object object;
        try
        {
            object = clazz.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new EngineImportException("Error instantiating aggregation class by name '" + className + "'", e);
        }
        catch (IllegalAccessException e)
        {
            throw new EngineImportException("Illegal access instatiating aggregation function class by name '" + className + "'", e);
        }

        if (!(object instanceof AggregationSupport))
        {
            throw new EngineImportException("Aggregation class by name '" + className + "' does not subclass AggregationSupport");
        }
        return (AggregationSupport) object;
    }

    public Pair<Class, String> resolveSingleRow(String name) throws EngineImportException, EngineImportUndefinedException
    {
        Pair<String, String> pair = singleRowFunctions.get(name);
        if (pair == null)
        {
            pair = singleRowFunctions.get(name.toLowerCase());
        }
        if (pair == null)
        {
            throw new EngineImportUndefinedException("A function named '" + name + "' is not defined");
        }

        Class clazz;
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            clazz = Class.forName(pair.getFirst(), true, cl);
        }
        catch (ClassNotFoundException ex)
        {
            throw new EngineImportException("Could not load single-row function class by name '" + pair.getFirst() + "'", ex);
        }
        return new Pair<Class, String>(clazz, pair.getSecond());
    }

    public Method resolveMethod(String className, String methodName, Class[] paramTypes)
			throws EngineImportException
    {
        Class clazz;
        try
        {
            clazz = resolveClassInternal(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new EngineImportException("Could not load class by name '" + className + "', please check imports", e);
        }

        try
        {
            return MethodResolver.resolveMethod(clazz, methodName, paramTypes, false);
        }
        catch (EngineNoSuchMethodException e)
        {
            throw convert(clazz, methodName, paramTypes, e, false);
        }
    }

    public Method resolveMethod(String className, String methodName)
			throws EngineImportException
    {
        Class clazz;
        try
        {
            clazz = resolveClassInternal(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new EngineImportException("Could not load class by name '" + className + "', please check imports", e);
        }

        Method methods[] = clazz.getMethods();
        Method methodByName = null;

        // check each method by name
        for (Method method : methods)
        {
            if (method.getName().equals(methodName))
            {
                if (methodByName != null)
                {
                    throw new EngineImportException("Ambiguous method name: method by name '" + methodName + "' is overloaded in class '" + className + "'");
                }
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
                {
                    methodByName = method;
                }
            }
        }

        if (methodByName == null)
        {
            throw new EngineImportException("Could not find static method named '" + methodName + "' in class '" + className + "'");
        }
        return methodByName;
    }

    public Class resolveClass(String className)
			throws EngineImportException
    {
        Class clazz;
        try
        {
            clazz = resolveClassInternal(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new EngineImportException("Could not load class by name '" + className + "', please check imports", e);
        }

        return clazz;
    }

    /**
     * Finds a class by class name using the auto-import information provided.
     * @param className is the class name to find
     * @return class
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    protected Class resolveClassInternal(String className) throws ClassNotFoundException
    {
		// Attempt to retrieve the class with the name as-is
		try
		{
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return Class.forName(className, true, cl);
		}
		catch(ClassNotFoundException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Class not found for resolving from name as-is '" + className + "'");
            }
        }

		// Try all the imports
		for(String importName : imports)
		{
			boolean isClassName = isClassName(importName);

			// Import is a class name
			if(isClassName)
			{
				if(importName.endsWith(className))
				{
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    return Class.forName(importName, true, cl);
				}
			}
			else
			{
				// Import is a package name
				String prefixedClassName = getPackageName(importName) + '.' + className;
				try
				{
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    return Class.forName(prefixedClassName, true, cl);
				}
				catch(ClassNotFoundException e){
                    if (log.isDebugEnabled())
                    {
                        log.debug("Class not found for resolving from name '" + prefixedClassName + "'");
                    }
                }
			}
		}

        // try to resolve from method references
        for (String name : methodInvocationRef.keySet())
        {
            if (JavaClassHelper.isSimpleNameFullyQualfied(className, name))
            {
                try
                {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    return Class.forName(name, true, cl);
                }
                catch (ClassNotFoundException e1)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Class not found for resolving from method invocation ref:" + name);
                    }
                }
            }
        }

        // No import worked, the class isn't resolved
		throw new ClassNotFoundException("Unknown class " + className);
	}

    public Method resolveMethod(Class clazz, String methodName, Class[] paramTypes)
			throws EngineImportException
    {
        try
        {
            return MethodResolver.resolveMethod(clazz, methodName, paramTypes, true);
        }
        catch (EngineNoSuchMethodException e)
        {
            throw convert(clazz, methodName, paramTypes, e, true);
        }
    }

    private EngineImportException convert(Class clazz, String methodName, Class[] paramTypes, EngineNoSuchMethodException e, boolean isInstance)
    {
        String expected = JavaClassHelper.getParameterAsString(paramTypes);
        String message = "Could not find ";
        if (!isInstance) {
            message += "static ";
        }
        else {
            message += "instance ";
        }

        if (paramTypes.length > 0)
        {
            message += "method named '" + methodName + "' in class '" + JavaClassHelper.getClassNameFullyQualPretty(clazz) + "' with matching parameter number and expected parameter type(s) '" + expected + "'";
        }
        else
        {
            message += "method named '" + methodName + "' in class '" + JavaClassHelper.getClassNameFullyQualPretty(clazz) + "' taking no parameters";
        }

        if (e.getNearestMissMethod() != null)
        {
            message += (" (nearest match found was '" + e.getNearestMissMethod().getName() + "' taking type(s) '" + JavaClassHelper.getParameterAsString(e.getNearestMissMethod().getParameterTypes()) + "')");
        }
        return new EngineImportException(message, e);
    }

    public ExprNode resolveAggExtendedBuiltin(String name, boolean isDistinct) {
        if (!allowExtendedAggregationFunc) {
            return null;
        }
        if (name.toLowerCase().equals("firstever"))
        {
            return new ExprFirstEverNode(isDistinct);
        }
        if (name.toLowerCase().equals("lastever"))
        {
            return new ExprLastEverNode(isDistinct);
        }
        if (name.toLowerCase().equals("rate"))
        {
            return new ExprRateAggNode(isDistinct);
        }
        if (name.toLowerCase().equals("nth"))
        {
            return new ExprNthAggNode(isDistinct);
        }
        if (name.toLowerCase().equals("leaving"))
        {
            return new ExprLeavingAggNode(isDistinct);
        }
        return null;
    }

    /**
     * For testing, returns imports.
     * @return returns auto-import list as array
     */
    protected String[] getImports()
	{
		return imports.toArray(new String[imports.size()]);
	}

    private static boolean isFunctionName(String functionName)
    {
        String classNameRegEx = "\\w+";
        return functionName.matches(classNameRegEx);
    }

	private static boolean isClassName(String importName)
	{
		String classNameRegEx = "(\\w+\\.)*\\w+(\\$\\w+)?";
		return importName.matches(classNameRegEx);
	}

	private static boolean isPackageName(String importName)
	{
		String classNameRegEx = "(\\w+\\.)+\\*";
		return importName.matches(classNameRegEx);
	}

	// Strip off the final ".*"
	private static String getPackageName(String importName)
	{
		return importName.substring(0, importName.length() - 2);
	}
}
