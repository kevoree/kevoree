package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.util.JavaClassHelper;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Getter for a list property identified by a given index, using the CGLIB fast method.
 */
public class ListFastPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final FastMethod fastMethod;
    private final int index;

    /**
     * Constructor.
     * @param method the underlying method
     * @param fastMethod is the method to use to retrieve a value from the object
     * @param index is tge index within the array to get the property from
     * @param eventAdapterService factory for event beans and event types
     */
    public ListFastPropertyGetter(Method method, FastMethod fastMethod, int index, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, JavaClassHelper.getGenericReturnType(method, false), null);
        this.index = index;
        this.fastMethod = fastMethod;

        if (index < 0)
        {
            throw new IllegalArgumentException("Invalid negative index value");
        }
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            Object value = fastMethod.invoke(object, null);
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
        return "ListFastPropertyGetter " +
                " fastMethod=" + fastMethod.toString() +
                " index=" + index;
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
