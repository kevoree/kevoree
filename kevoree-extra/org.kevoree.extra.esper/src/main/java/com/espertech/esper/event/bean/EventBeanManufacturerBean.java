package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.EngineImportException;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventBeanManufactureException;
import com.espertech.esper.event.EventBeanManufacturer;
import com.espertech.esper.event.WriteablePropertyDescriptor;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Factory for event beans created and populate anew from a set of values.
 */
public class EventBeanManufacturerBean implements EventBeanManufacturer
{
    private static Log log = LogFactory.getLog(EventBeanManufacturerBean.class);

    private final FastClass fastClass;
    private final BeanEventType beanEventType;
    private final EventAdapterService service;
    private final FastMethod[] writeMethods;
    private final FastMethod factoryMethod;
    private final boolean hasPrimitiveTypes;
    private final boolean[] primitiveType;

    /**
     * Ctor.
     * @param beanEventType target type
     * @param service factory for events
     * @param properties written properties
     * @param methodResolutionService for resolving write methods
     * @throws EventBeanManufactureException if the write method lookup fail
     */
    public EventBeanManufacturerBean(BeanEventType beanEventType,
                                     EventAdapterService service,
                                     WriteablePropertyDescriptor[] properties,
                                     MethodResolutionService methodResolutionService
                                     )
            throws EventBeanManufactureException
    {
        this.fastClass = beanEventType.getFastClass();
        this.beanEventType = beanEventType;
        this.service = service;

        // see if we use a factory method
        if (beanEventType.getFactoryMethodName() != null)
        {
            factoryMethod = resolveFactoryMethod(fastClass, beanEventType.getFactoryMethodName(), methodResolutionService);
        }
        else
        {
            factoryMethod = null;
            try
            {
                fastClass.newInstance();
            }
            catch (InvocationTargetException e)
            {
                String message = "Failed to instantiate class '" + fastClass.getJavaClass().getName() + "', define a factory method if the class has no default constructor: " + e.getTargetException().getMessage();
                log.info(message, e);
                throw new EventBeanManufactureException(message, e.getTargetException());
            }
            catch (IllegalArgumentException e)
            {
                String message = "Failed to instantiate class '" + fastClass.getJavaClass().getName() + "', define a factory method if the class has no default constructor";
                log.info(message, e);
                throw new EventBeanManufactureException(message, e);
            }
        }

        writeMethods = new FastMethod[properties.length];
        boolean primitiveTypeCheck = false;
        primitiveType = new boolean[properties.length];
        for (int i = 0; i < properties.length; i++)
        {
            writeMethods[i] = fastClass.getMethod(properties[i].getWriteMethod());
            primitiveType[i] = properties[i].getType().isPrimitive();
            primitiveTypeCheck |= primitiveType[i];
        }
        hasPrimitiveTypes = primitiveTypeCheck;
    }

    public EventBean make(Object[] propertyValues)
    {
        Object out;
        if (factoryMethod == null)
        {
            try
            {
                out = fastClass.newInstance();
            }
            catch (InvocationTargetException e)
            {
                String message = "Unexpected exception encountered invoking newInstance on class '" + fastClass.getJavaClass().getName() + "': " + e.getTargetException().getMessage();
                log.error(message, e);
                return null;
            }
        }
        else
        {
            try
            {
                out = factoryMethod.invoke(null, null);
            }
            catch (InvocationTargetException e)
            {
                String message = "Unexpected exception encountered invoking factory method '" + factoryMethod.getName() + "' on class '" + factoryMethod.getJavaMethod().getDeclaringClass().getName() + "': " + e.getTargetException().getMessage();
                log.error(message, e);
                return null;
            }
        }

        if (!hasPrimitiveTypes) {
            Object[] params = new Object[1];
            for (int i = 0; i < writeMethods.length; i++)
            {
                params[0] = propertyValues[i];
                try
                {
                    writeMethods[i].invoke(out, params);
                }
                catch (InvocationTargetException e)
                {
                    String message = "Unexpected exception encountered invoking setter-method '" + writeMethods[i] + "' on class '" +
                            fastClass.getJavaClass().getName() + "' : " + e.getTargetException().getMessage();
                    log.error(message, e);
                }
            }
        }
        else
        {
            Object[] params = new Object[1];
            for (int i = 0; i < writeMethods.length; i++)
            {
                if (primitiveType[i]) {
                    if (propertyValues[i] == null) {
                        continue;
                    }
                }
                params[0] = propertyValues[i];
                try
                {
                    writeMethods[i].invoke(out, params);
                }
                catch (InvocationTargetException e)
                {
                    String message = "Unexpected exception encountered invoking setter-method '" + writeMethods[i] + "' on class '" +
                            fastClass.getJavaClass().getName() + "' : " + e.getTargetException().getMessage();
                    log.error(message, e);
                }
            }
        }


        return service.adapterForTypedBean(out, beanEventType);
    }

    private static FastMethod resolveFactoryMethod(FastClass fastClass, String factoryMethodName, MethodResolutionService methodResolutionService)
            throws EventBeanManufactureException
    {
        int lastDotIndex = factoryMethodName.lastIndexOf('.');
        if (lastDotIndex == -1)
        {
            try
            {
                Method method = methodResolutionService.resolveMethod(fastClass.getJavaClass(), factoryMethodName, new Class[0]);
                return fastClass.getMethod(method);
            }
            catch (EngineImportException e)
            {
                String message = "Failed to resolve configured factory method '" + factoryMethodName +
                        "' expected to exist for class '" + fastClass.getName() + "'";
                log.info(message, e);
                throw new EventBeanManufactureException(message, e);
            }
        }

        String className = factoryMethodName.substring(0, lastDotIndex);
        String methodName = factoryMethodName.substring(lastDotIndex + 1);
        try
        {
            Method method = methodResolutionService.resolveMethod(className, methodName, new Class[0]);
            FastClass fastClassFactory = FastClass.create(method.getDeclaringClass());
            return fastClassFactory.getMethod(method);
        }
        catch (EngineImportException e)
        {
            String message = "Failed to resolve configured factory method '" + methodName + "' expected to exist for class '" + className + "'";
            log.info(message, e);
            throw new EventBeanManufactureException(message, e);
        }
    }
}
