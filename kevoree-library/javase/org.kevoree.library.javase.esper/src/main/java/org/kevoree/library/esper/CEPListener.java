package org.kevoree.library.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CEPListener implements UpdateListener {
	private Logger logger = LoggerFactory.getLogger(CEPListener.class);

    public void update(EventBean[] newData, EventBean[] oldData) {
        logger.info("Event received: " + newData[0].getUnderlying());
    }
}