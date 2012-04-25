package org.kevoree.library.rest;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/11/11
 * Time: 12:00
 * To change this template use File | Settings | File Templates.
 */

//@Library(name = "JavaSE")
//@ChannelTypeFragment
public class RestChannel extends AbstractChannelFragment {

    private static final Logger logger = LoggerFactory.getLogger(RestChannel.class);

    @Override
    public Object dispatch(Message msg) {
        if (getBindedPorts().isEmpty() && getOtherFragments().isEmpty()) {
            logger.debug("No consumer, msg lost=" + msg.getContent());
        }
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!msg.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
