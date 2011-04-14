package com.espertech.esper.event.bean;

import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.JavaClassHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Array;

/**
 * Getter for an iterable property backed by a field, identified by a given index, using vanilla reflection.
 */
public class IterableFieldPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter
{
    private final Field field;
    private final int index;

    /**
     * Constructor.
     * @param field is the field to use to retrieve a value from the object
     * @param index is tge index within the array to get the property from
     * @param eventAdapterService factory for event beans and event types
     */
    public IterableFieldPropertyGetter(Field field, int index, EventAdapterService eventAdapterService)
    {
        super(eventAdapterService, JavaClassHelper.getGenericFieldType(field, false), null);
        this.index = index;
        this.field = field;

        if (index < 0)
        {
            throw new IllegalArgumentException("Invalid negative index value");
        }
    }

    public Object getBeanProp(Object object) throws PropertyAccessException
    {
        try
        {
            Object value = field.get(object);
            return IterableFastPropertyGetter.getIterable(value, index);
        }
        catch (ClassCastException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
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
        return "IterableFieldPropertyGetter " +
                " field=" + field.toString() +
                " index=" + index;
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
