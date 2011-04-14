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
import com.espertech.esper.client.ConfigurationDataCache;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.epl.db.DataCache;
import com.espertech.esper.epl.db.DataCacheFactory;
import com.espertech.esper.epl.db.PollExecStrategy;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.spec.MethodStreamSpec;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.schedule.ScheduleBucket;
import com.espertech.esper.schedule.SchedulingService;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.HistoricalEventViewable;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for method-invocation data provider streams.
 */
public class MethodPollingViewableFactory
{
    private static final Log log = LogFactory.getLog(MethodPollingViewableFactory.class);

    /**
     * Creates a method-invocation polling view for use as a stream that calls a method, or pulls results from cache.
     * @param streamNumber the stream number
     * @param methodStreamSpec defines the class and method to call
     * @param eventAdapterService for creating event types and events
     * @param epStatementHandle for time-based callbacks
     * @param methodResolutionService for resolving classes and imports
     * @param engineImportService for resolving configurations
     * @param schedulingService for scheduling callbacks in expiry-time based caches
     * @param scheduleBucket for schedules within the statement
     * @param exprEvaluatorContext expression evaluation context
     * @return pollable view
     * @throws ExprValidationException if the expressions cannot be validated or the method descriptor
     * has incorrect class and method names, or parameter number and types don't match
     */
    public static HistoricalEventViewable createPollMethodView(int streamNumber,
                                                               MethodStreamSpec methodStreamSpec,
                                                               EventAdapterService eventAdapterService,
                                                               EPStatementHandle epStatementHandle,
                                                               MethodResolutionService methodResolutionService,
                                                               EngineImportService engineImportService,
                                                               SchedulingService schedulingService,
                                                               ScheduleBucket scheduleBucket,
                                                               ExprEvaluatorContext exprEvaluatorContext)
            throws ExprValidationException
    {
        // Try to resolve the method
        FastMethod staticMethod;
        Class declaringClass;
        try
		{
			Method method = methodResolutionService.resolveMethod(methodStreamSpec.getClassName(), methodStreamSpec.getMethodName());
            declaringClass = method.getDeclaringClass();
            FastClass declaringFastClass = FastClass.create(Thread.currentThread().getContextClassLoader(), method.getDeclaringClass());
			staticMethod = declaringFastClass.getMethod(method);
		}
		catch(Exception e)
		{
			throw new ExprValidationException(e.getMessage());
		}

        // Determine object type returned by method
        Class beanClass = staticMethod.getReturnType();
        if ((beanClass == void.class) || (beanClass == Void.class) || (JavaClassHelper.isJavaBuiltinDataType(beanClass)))
        {
            throw new ExprValidationException("Invalid return type for static method '" + staticMethod.getName() + "' of class '" + methodStreamSpec.getClassName() + "', expecting a Java class");
        }
        if (staticMethod.getReturnType().isArray())
        {
            beanClass = staticMethod.getReturnType().getComponentType();
        }

        // If the method returns a Map, look up the map type
        Map<String, Object> mapType = null;
        String mapTypeName = null;
        if ( (JavaClassHelper.isImplementsInterface(staticMethod.getReturnType(), Map.class)) ||
             (staticMethod.getReturnType().isArray() && JavaClassHelper.isImplementsInterface(staticMethod.getReturnType().getComponentType(), Map.class)) )
        {
            Method typeGetterMethod = null;
            String getterMethodName = methodStreamSpec.getMethodName() + "Metadata";
            try
            {
                typeGetterMethod = methodResolutionService.resolveMethod(methodStreamSpec.getClassName(), getterMethodName, new Class[0]);
            }
            catch(Exception e)
            {
                log.warn("Could not find getter method for Map-typed method invocation, expected a method by name '" + getterMethodName + "' accepting no parameters");
            }
            if ((typeGetterMethod != null) && (JavaClassHelper.isImplementsInterface(typeGetterMethod.getReturnType(), Map.class)))
            {
                Object resultType = null;
                try
                {
                    resultType = typeGetterMethod.invoke(null);
                }
                catch (Exception e)
                {
                    log.warn("Error invoking getter method for Map-typed method invocation, for method by name '" + getterMethodName + "' accepting no parameters");
                }

                if ((resultType != null) && (resultType instanceof Map))
                {
                    mapTypeName = methodStreamSpec.getClassName() + "." + typeGetterMethod.getName();
                    mapType = (Map<String, Object>) resultType;
                }
            }
        }

        // Determine event type from class and method name
        EventType eventType;
        if (mapType != null)
        {
            eventType = eventAdapterService.addNestableMapType(mapTypeName, mapType, null, false, true, true, false, false);
        }
        else
        {
            eventType = eventAdapterService.addBeanType(beanClass.getName(), beanClass, false, true, true);
        }

        // Construct polling strategy as a method invocation
        ConfigurationMethodRef configCache = engineImportService.getConfigurationMethodRef(declaringClass.getName());
        if (configCache == null)
        {
            configCache = engineImportService.getConfigurationMethodRef(declaringClass.getSimpleName());
        }
        ConfigurationDataCache dataCacheDesc = (configCache != null) ? configCache.getDataCacheDesc() : null;
        DataCache dataCache = DataCacheFactory.getDataCache(dataCacheDesc, epStatementHandle, schedulingService, scheduleBucket);
        PollExecStrategy methodPollStrategy = new MethodPollingExecStrategy(eventAdapterService, staticMethod, mapTypeName != null, eventType);

        return new MethodPollingViewable(methodStreamSpec, streamNumber, methodStreamSpec.getExpressions(), methodPollStrategy, dataCache, eventType, exprEvaluatorContext);
    }
}
