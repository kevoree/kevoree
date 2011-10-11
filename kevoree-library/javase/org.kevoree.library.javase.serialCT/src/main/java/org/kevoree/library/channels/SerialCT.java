package org.kevoree.library.channels;

import org.kevoree.ContainerNode;
import org.kevoree.DictionaryValue;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.extra.osgi.rxtx.KevoreeSharedCom;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@ChannelTypeFragment
public class SerialCT extends AbstractChannelFragment {
	private static final Logger logger = LoggerFactory.getLogger(SerialCT.class);

    KContentListener cl = new KContentListener(this);
    protected ServiceReference sr;
    protected KevoreeModelHandlerService modelHandlerService = null;

    protected HashMap<String, String> nodePortCache = new HashMap<String, String>();

    protected String getPortFromNode(String remoteNodeName) {
        if (!nodePortCache.containsKey(remoteNodeName)) {
            for (ContainerNode node : modelHandlerService.getLastModel().getNodesForJ()) {
                if (node.getName().equals(remoteNodeName)) {
                    for (DictionaryValue dv : node.getDictionary().get().getValuesForJ()) {
                        if (dv.getAttribute().getName().equals("boardPortName")) {
                            nodePortCache.put(remoteNodeName, dv.getValue());
                        }
                    }
                }
            }
        }
        return nodePortCache.get(remoteNodeName);
    }

    @Start
    public void startRxTxChannel() {
        Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
        modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);
        new Thread() {
            @Override
            public void run() {
                for (KevoreeChannelFragment cf : getOtherFragments()) {
                    if (getPortFromNode(cf.getNodeName()) != null) {
                        KevoreeSharedCom.addObserver(getPortFromNode(cf.getNodeName()), cl);
                    } else {
                        logger.error("Com Port Not Found ");
                    }
                }
            }
        }.start();
    }

    @Update
    public void updateRxTxChannel() {
        stopRxTxChannel();
        startRxTxChannel();
    }

    @Stop
    public void stopRxTxChannel() {
        for (String port : nodePortCache.values()) {
            KevoreeSharedCom.removeObserver(port, cl);
        }
        nodePortCache.clear();
    }

    @Override
    public Object dispatch(Message msg) {

        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (msg.getPassedNodes().isEmpty()) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, final String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                String messageTosSend = "#"+getName() + /*":" + getNodeName() +*/ "[" + message.getContent().toString() + "]";

                logger.debug("Send Message");
                logger.debug(getPortFromNode(remoteNodeName));
                logger.debug(messageTosSend);

                KevoreeSharedCom.send(getPortFromNode(remoteNodeName), messageTosSend);
                return null;
            }
        };
    }


}
