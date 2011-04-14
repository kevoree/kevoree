package com.espertech.esper.event;

import com.espertech.esper.client.EventBean;

/**
 * Writer for a set of wrapper event object values.
 */
public class WrapperEventBeanPropertyWriter implements EventBeanWriter
{
    private final EventPropertyWriter[] writerArr;

    /**
     * Ctor.
     * @param writerArr writers are writing properties.
     */
    public WrapperEventBeanPropertyWriter(EventPropertyWriter[] writerArr)
    {
        this.writerArr = writerArr;
    }

    public void write(Object[] values, EventBean event)
    {
        for (int i = 0; i < values.length; i++)
        {
            writerArr[i].write(values[i], event);
        }
    }
}
