package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.util.JavaClassHelper;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Getter for a iterable property identified by a given index, using the CGLIB fast method.
 */
public class IterableFastPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
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
    public IterableFastPropertyGetter(Method method, FastMethod fastMethod, int index, EventAdapterService eventAdapterService)
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
            return getIterable(value, index);
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

    /**
     * Returns the iterable at a certain index, or null.
     * @param value the iterable
     * @param index index
     * @return value at index
     */
    protected static Object getIterable(Object value, int index)
    {
        if (!(value instanceof Iterable))
        {
            return null;
        }

        Iterator it = ((Iterable) value).iterator();

        if (index == 0)
        {
            if (it.hasNext())
            {
                return it.next();
            }
            return null;
        }

        int count = 0;
        while(true)
        {
            if (!it.hasNext())
            {
                return null;
            }
            if (count < index)
            {
                it.next();
            }
            else
            {
                return it.next();
            }
            count++;
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
