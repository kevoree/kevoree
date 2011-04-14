package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.util.JavaClassHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Getter for a list property identified by a given index, using vanilla reflection.
 */
public class ListMethodPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final Method method;
    private final int index;

    /**
     * Constructor.
     * @param method is the method to use to retrieve a value from the object
     * @param index is tge index within the array to get the property from
     * @param eventAdapterService factory for event beans and event types
     */
    public ListMethodPropertyGetter(Method method, int index, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, JavaClassHelper.getGenericReturnType(method, false), null);
        this.index = index;
        this.method = method;

        if (index < 0)
        {
            throw new IllegalArgumentException("Invalid negative index value");
        }
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            Object value = method.invoke(object, (Object[]) null);
            if (!(value instanceof List))
            {
                return null;
            }
            List valueList = (List) value;
            if (valueList.size() <= index)
            {
                return null;
            }
            return valueList.get(index);
        }
        catch (ClassCastException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
        }
        catch (InvocationTargetException e)
        {
            throw new PropertyAccessException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new PropertyAccessException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new PropertyAccessException(e);
        }
    }

    public boolean isBeanExistsProperty(Object object)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public final Object get(EventBean obj) throws PropertyAccessException
    {
        Object underlying = obj.getUnderlying();
        return getBeanProp(underlying);
    }

    public String toString()
    {
        return "ListMethodPropertyGetter " +
                " method=" + method.toString() +
                " index=" + index;
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
