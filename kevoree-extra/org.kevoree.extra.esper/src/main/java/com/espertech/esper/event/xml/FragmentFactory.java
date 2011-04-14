package com.espertech.esper.event.xml;

import com.espertech.esper.client.EventBean;
import org.w3c.dom.Node;

/**
 * Factory for event fragments for use with DOM getters.
 */
public interface FragmentFactory
{
    /**
     * Returns a fragment for the node.
     * @param result node to fragment
     * @return fragment
     */
    public EventBean getEvent(Node result);
}
