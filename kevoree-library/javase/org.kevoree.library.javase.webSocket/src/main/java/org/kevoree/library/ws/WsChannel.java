package org.kevoree.library.ws;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;
import java.net.InetSocketAddress;

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

    @Start
    public void startWsChannel() throws Exception {
        int port = parsePortNumber(getNodeName());
        logger.debug("Start WS on port "+port);
        wsServer = new WsServer(new InetSocketAddress("0.0.0.0", port),this);
        wsServer.start();
    }

    @Stop
    public void stopWsChannel() throws Exception {
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
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String s, String s1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
