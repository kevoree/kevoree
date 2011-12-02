package org.kevoree.library.javase.kestrelChannels;


import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 29/11/11
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "JavaSE")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "portServerKestrel", defaultValue = "22133", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "serverhosted", defaultValue = "true", optional = false, vals = {"true", "false"}, fragmentDependant = true),
        @DictionaryAttribute(name = "queuePath", defaultValue = "/var/spool/kestrel", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "queueLog", defaultValue = "/var/log/kestrel/kestrel.log", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "queueName", defaultValue = "kevoree", optional = true, fragmentDependant = true)
}
)
public class KestrelChannel  extends AbstractChannelFragment  implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private  KestrelServer server=null;
    private boolean alive = false;
    private  int portServerKestrel;
    private String queuePath;
    private  String queueName;
    private  String queueLog;
    private  Boolean serverhosted;
    private String clientKestrelHost;
    private int clientKestrelPort;
    private boolean serverfound = false;

    @Start
    public void startChannel()
    {
        updateDico();
        try
        {
            if(serverhosted)
            {
                server =  new KestrelServer("localhost",portServerKestrel,queuePath,queueLog);
            }
        } catch (Exception e)
        {
            logger.error("startChannel "+e.toString());
        }

        alive = true;
    }


    @Stop
    public void stopChannel() {
        if(server !=null )
            server.stopServer();
        alive = false;
    }


    public void updateDico() throws NumberFormatException {
        try
        {

            portServerKestrel=  Integer.parseInt(this.getDictionary().get("portServerKestrel").toString());
            queuePath=  getDictionary().get("clientPort").toString();
            queueName=  getDictionary().get("clientPort").toString();
            queueLog=  getDictionary().get("queueLog").toString();
            serverhosted = Boolean.parseBoolean(getDictionary().get("serverhosted").toString());
        } catch (Exception e)
        {
            throw new NumberFormatException("updateDico"+e);
        }
    }
    @Update
    public void updateChannel() {
        updateDico();
        if(server !=null )
            server.reloadServer();

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
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {

            public Object sendMessageToRemote(Message msg) {

                try
                {
                    logger.debug("Sending message to " + remoteNodeName);

                    if(findServerKestrel())
                    {
                        try
                        {
                            KestrelClient client = new KestrelClient(clientKestrelHost,clientKestrelPort);
                            client.enqueue(remoteNodeName, msg);
                            client.disconnect();
                        } catch (Exception e) {
                            logger.error("KestrelClient "+e.toString());
                        }
                    }
                    else
                    {
                        logger.error("no server Kestrel found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    public List<String> getAllNodes () {
        ContainerRoot model = this.getModelService().getLastModel();
        for (Object o : model.getGroupsForJ()) {
            Group g = (Group) o;
            if (g.getName().equals(this.getName())) {
                List<String> peers = new ArrayList<String>(g.getSubNodes().size());
                for (ContainerNode node : g.getSubNodesForJ()) {
                    peers.add(node.getName());
                }
                return peers;
            }
        }
        return new ArrayList<String>();
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
            return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "portKestrel",nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }


    public boolean findServerKestrel()
    {
        if(!serverfound)
        {
            for(String node : getAllNodes())
            {
                String KestrelHosted = KevoreeFragmentPropertyHelper.getPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "server", node);
                if(Boolean.parseBoolean(KestrelHosted))
                {
                    try
                    {
                        clientKestrelHost =getAddress(node);
                        clientKestrelPort = parsePortNumber(node);
                        serverfound = true;
                    } catch (IOException e) {
                        logger.debug("findServerKestrel "+e.toString());
                    }
                    break;
                }
            }
        }
        return serverfound;
    }


    public void run() {

        if(findServerKestrel())
        {
            KestrelClient client = new KestrelClient(clientKestrelHost,clientKestrelPort);
            while (alive)
            {
                Message msg =    client.dequeue(getNodeName());
                logger.debug(" Receive message "+msg.getDestNodeName());
                remoteDispatch(msg);
            }
            client.disconnect();
        }else {
            logger.error("no server Kestrel found");
        }


    }
}
