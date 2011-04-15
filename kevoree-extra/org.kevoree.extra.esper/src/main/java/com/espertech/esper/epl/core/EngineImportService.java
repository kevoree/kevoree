/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.agg.AggregationSupport;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.client.ConfigurationMethodRef;

import java.lang.reflect.Method;

/**
 * Service for engine-level resolution of static methods and aggregation methods.
 */
public interface EngineImportService
{
    /**
     * Returns the method invocation caches for the from-clause for a class.
     * @param className the class name providing the method
     * @return cache configs
     */
    public ConfigurationMethodRef getConfigurationMethodRef(String className);

    /**
     * Add an import, such as "com.mypackage.*" or "com.mypackage.MyClass".
     * @param importName is the import to add
     * @throws EngineImportException if the information or format is invalid
     */
    public void addImport(String importName) throws EngineImportException;

    /**
     * Add an aggregation function.
     * @param functionName is the name of the function to make known.
     * @param aggregationClass is the class that provides the aggregator
     * @throws EngineImportException throw if format or information is invalid
     */
    public void addAggregation(String functionName, String aggregationClass) throws EngineImportException;

    /**
     * Add an single-row function.
     * @param functionName is the name of the function to make known.
     * @param singleRowFuncClass is the class that provides the single row function
     * @param methodName is the name of the public static method provided by the class that provides the single row function
     * @throws EngineImportException throw if format or information is invalid
     */
    public void addSingleRow(String functionName, String singleRowFuncClass, String methodName) throws EngineImportException;

    /**
     * Used at statement compile-time to try and resolve a given function name into an
     * aggregation method. Matches function name case-neutral.
     * @param functionName is the function name
     * @return aggregation provider
     * @throws EngineImportUndefinedException if the function is not a configured aggregation function
     * @throws EngineImportException if the aggregation providing class could not be loaded or doesn't match
     */
    public AggregationSupport resolveAggregation(String functionName) throws EngineImportUndefinedException, EngineImportException;

    /**
     * Used at statement compile-time to try and resolve a given function name into an
     * single-row function. Matches function name case-neutral.
     * @param functionName is the function name
     * @throws EngineImportUndefinedException if the function is not a configured single-row function
     * @throws EngineImportException if the function providing class could not be loaded or doesn't match
     */
    public Pair<Class, String> resolveSingleRow(String functionName) throws EngineImportUndefinedException, EngineImportException;

    /**
     * Resolves a given class, method and list of parameter types to a static method.
     * @param className is the class name to use
     * @param methodName is the method name
     * @param paramTypes is parameter types match expression sub-nodes
     * @return method this resolves to
     * @throws EngineImportException if the method cannot be resolved to a visible static method
     */
    public Method resolveMethod(String className, String methodName, Class[] paramTypes) throws EngineImportException;

    /**
     * Resolves a given class name, either fully qualified and simple and imported to a class.
     * @param className is the class name to use
     * @return class this resolves to
     * @throws EngineImportException if there was an error resolving the class
     */
    public Class resolveClass(String className) throws EngineImportException;

    /**
     * Resolves a given class and method name to a static method, expecting the method to exist
     * exactly once and not be overloaded, with any parameters.
     * @param className is the class name to use
     * @param methodName is the method name
     * @return method this resolves to
     * @throws EngineImportException if the method cannot be resolved to a visible static method, or
     * if the method is overloaded
     */
    public Method resolveMethod(String className, String methodName) throws EngineImportException;

    /**
     * Resolves a given method name and list of parameter types to an instance or static method exposed by the given class.
     * @param clazz is the class to look for a fitting method
     * @param methodName is the method name
     * @param paramTypes is parameter types match expression sub-nodes
     * @return method this resolves to
     * @throws EngineImportException if the method cannot be resolved to a visible static or instance method
     */
    public Method resolveMethod(Class clazz, String methodName, Class[] paramTypes) throws EngineImportException;

    /**
     * Resolve an extended (non-SQL std) builtin aggregation.
     * @param name of func
     * @param isDistinct indicator
     * @return aggregation func node
     */
    public ExprNode resolveAggExtendedBuiltin(String name, boolean isDistinct);
}
