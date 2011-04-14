package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.EventBeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Writer for a set of event properties to a bean event.
 */
public class BeanEventBeanWriter implements EventBeanWriter
{
    private static final Log log = LogFactory.getLog(BeanEventBeanWriter.class);

    private final BeanEventPropertyWriter[] writers;

    /**
     * Writes to use.
     * @param writers writers
     */
    public BeanEventBeanWriter(BeanEventPropertyWriter[] writers)
    {
        this.writers = writers;
    }

    public void write(Object[] values, EventBean event)
    {
        for (int i = 0; i < values.length; i++)
        {
            writers[i].write(values[i], event);
        }
    }
}
