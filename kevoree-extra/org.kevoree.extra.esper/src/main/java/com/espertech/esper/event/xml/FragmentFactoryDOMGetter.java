package com.espertech.esper.event.xml;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.EventAdapterService;
import org.w3c.dom.Node;

/**
 * Factory for fragments for DOM getters.
 */
public class FragmentFactoryDOMGetter implements FragmentFactory
{
    private final EventAdapterService eventAdapterService;
    private final BaseXMLEventType xmlEventType;
    private final String propertyExpression;

    private volatile EventType fragmentType;

    /**
     * Ctor.
     * @param eventAdapterService for event type lookup
     * @param xmlEventType the originating type
     * @param propertyExpression property expression
     */
    public FragmentFactoryDOMGetter(EventAdapterService eventAdapterService, BaseXMLEventType xmlEventType, String propertyExpression)
    {
        this.eventAdapterService = eventAdapterService;
        this.xmlEventType = xmlEventType;
        this.propertyExpression = propertyExpression;
    }

    public EventBean getEvent(Node result)
    {
        if (fragmentType == null)
        {
            FragmentEventType type = xmlEventType.getFragmentType(propertyExpression);
            if (type == null)
            {
                return null;
            }
            fragmentType = type.getFragmentType();
        }

        return eventAdapterService.adapterForTypedDOM(result, fragmentType);
    }    
}
