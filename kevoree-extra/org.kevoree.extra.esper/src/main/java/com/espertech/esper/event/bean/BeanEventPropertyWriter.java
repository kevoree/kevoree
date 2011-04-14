package com.espertech.esper.event.bean;

import com.espertech.esper.event.EventPropertyWriter;
import com.espertech.esper.client.EventBean;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Writer for a property to an event.
 */
public class BeanEventPropertyWriter implements EventPropertyWriter
{
    private static final Log log = LogFactory.getLog(BeanEventPropertyWriter.class);

    private final Class clazz;
    private final FastMethod writerMethod;

    /**
     * Ctor.
     * @param clazz to write to
     * @param writerMethod write method 
     */
    public BeanEventPropertyWriter(Class clazz, FastMethod writerMethod)
    {
        this.clazz = clazz;
        this.writerMethod = writerMethod;
    }

    public void write(Object value, EventBean target)
    {
        try
        {
            writerMethod.invoke(target.getUnderlying(), new Object[] {value});
        }
        catch (InvocationTargetException e)
        {
            String message = "Unexpected exception encountered invoking setter-method '" + writerMethod.getJavaMethod() + "' on class '" +
                    clazz.getName() + "' : " + e.getTargetException().getMessage();
            log.error(message, e);
        }
    }
}
