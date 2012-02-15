package org.kevoree.library.ws;

import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/02/12
 * Time: 22:27
 */

@Library(name = "JavaSE")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "port", optional = false, fragmentDependant = true)
}
)
public class WsChannel extends AbstractChannelFragment {

    private WsServer wsServer = null;
    private Logger logger = LoggerFactory.getLogger(WsChannel.class);


    private Map<String, WsSocketClient> csClients = null;

    @Start
    public void startWsChannel() throws Exception {
        csClients = new HashMap<String, WsSocketClient>();
        int port = parsePortNumber(getNodeName());
        logger.debug("Start WS on port " + port);
        wsServer = new WsServer(new InetSocketAddress("0.0.0.0", port), this);
        wsServer.start();
    }

    @Stop
    public void stopWsChannel() throws Exception {
        for (WsSocketClient client : csClients.values()) {
            client.close();
        }
        wsServer.stop();
        wsServer = null;
    }

    @Update
    public void updateWsChannel() throws Exception {
        stopWsChannel();
        startWsChannel();
    }

    @Override
    public Object dispatch(Message message) {

        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, message);
        }

        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!message.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, message);
            }
        }
        /*
        try {
            wsServer.sendToAll(message.getContent().toString());
        } catch (Exception e){
            e.printStackTrace();
        }*/
        

        return null;
    }

    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, final String remoteChannelName) {
        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message message) {
                try {

                    WsSocketClient client = null;
                    if(!csClients.containsKey(remoteNodeName)){
                        WsSocketClient newClient = new WsSocketClient(new URI("ws://"+getAddress(remoteNodeName)+":"+parsePortNumber(remoteNodeName)));
                        csClients.put(remoteNodeName,newClient);
                        newClient.connect();
                        client = newClient;
                    } else {
                        client = csClients.get(remoteNodeName);
                    }
                    client.sendMessage(message);
                } catch (Exception e) {
                    logger.error("Error while sending message to " + remoteNodeName + "-" + remoteChannelName);
                }
                return null;
            }
        };
    }


    public String getAddress(String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public int parsePortNumber(String nodeName) throws IOException {
        try {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper
                    .getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
                            nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }
}
