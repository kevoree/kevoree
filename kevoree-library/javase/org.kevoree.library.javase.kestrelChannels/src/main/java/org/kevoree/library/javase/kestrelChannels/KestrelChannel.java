package org.kevoree.library.javase.kestrelChannels;

import org.kevoree.ContainerNode;
import org.kevoree.Port;
import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.framework.*;
import org.kevoree.framework.aspects.PortAspect;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Semaphore;


/**
 * Created by IntelliJ IDEA.
 * User: jedartois@gmail.com
 * Date: 29/11/11
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "JavaSE")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "portKestrel", defaultValue = "22133", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "queuePath", defaultValue = "/tmp/kestrel", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "queueLog", defaultValue = "/tmp/kestrel.log", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "queueName", defaultValue = "kevoree", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "PersistentQueue", defaultValue = "false", optional = false, vals = {"true", "false"}, fragmentDependant = true)
}
)
public class KestrelChannel  extends AbstractChannelFragment  implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private  static KestrelServer server;
    private  static int counter;

    private boolean alive = false;
    private  int portKestrel;
    private String queuePath;
    private  String queueName;
    private  String queueLog;
    final Semaphore sem = new java.util.concurrent.Semaphore(1);
    private Map<String, KestrelClient> clientsKestrel = new HashMap<String, KestrelClient>();
    private Thread clientKestrel;
    private Boolean   PersistentQueue;

    @Start
    public void startChannel()
    {
        updateDico();
        alive = true;
        clientKestrel  = new Thread(this);
        clientKestrel.start();
    }


    @Stop
    public void stopChannel() {
        counter--;
        logger.debug("Stopping channel  "+counter);
        if(server !=null && counter <= 0 )
            server.stopServer();
        alive = false;
    }

    public void updateDico() throws NumberFormatException {
        try
        {
            portKestrel=  Integer.parseInt(this.getDictionary().get("portKestrel").toString());
            queuePath=  getDictionary().get("queuePath").toString();
            queueName=  getDictionary().get("queueName").toString();
            queueLog=  getDictionary().get("queueLog").toString();
            PersistentQueue = Boolean.parseBoolean(getDictionary().get("PersistentQueue").toString());
        } catch (Exception e)
        {
            throw new NumberFormatException("updateDico"+e);
        }
    }
    @Update
    public void updateChannel() {
        updateDico();
        if(server !=null)
            server.reloadServer();

    }
    @Override
    public Object dispatch(Message message) {
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, message);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!message.getPassedNodes().contains(cf.getNodeName()) ) {
                forward(cf, message);
                break;
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
                    sem.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
                try
                {
                    if(server != null)
                    {
                        logger.debug("enqueue message to " + msg.getDestNodeName()+" port kestrel "+portKestrel);
                        // adding the current node  to passedNodes
                        if (!msg.getPassedNodes().contains(getNodeName())) {
                            msg.getPassedNodes().add(getNodeName());
                        }
                        KestrelClient client = getOrCreateKestrel("localhost",portKestrel);
                        client.enqueue(queueName, msg);
                    }

                } catch (Exception e)
                {
                    delete_link("localhost",portKestrel);
                    logger.error("KestrelClient "+e.toString());
                }
                finally
                {
                    msg = null;
                    sem.release();
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
            return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "portKestrel",nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }


    public String getQueueName(String nodeName) throws IOException {
        return KevoreeFragmentPropertyHelper.getPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "queueName",nodeName);
    }

    public void run()
    {
        // Running the server Kestrel
        try
        {
            if(server == null && KevoreeUtil.isRequired(getModelService().getLastModel(),getName(),getNodeName()))
            {
                counter = 0;
                logger.debug("The server Kestrel is starting "+counter);
                server =  new KestrelServer("localhost",portKestrel,queuePath,queueLog,PersistentQueue);
            }else
            {
                counter++;
            }
        } catch (Exception e)
        {
            logger.error("startChannel "+e.toString());
        }

        // running KestrelClient if provided ports
        if(KevoreeUtil.isProvided(getModelService().getLastModel(),getName(),getNodeName()))
        {

            List<String> nodesKestrelServer = new ArrayList<String>();
            // looking for kestrel Servers
            for(ContainerNode node : getModelService().getLastModel().getNodesForJ())
            {
                if(!node.getName().equals(getNodeName())&& KevoreeUtil.isRequired(getModelService().getLastModel(),getName(),node.getName()))
                {
                    nodesKestrelServer.add(node.getName());
                    logger.debug(" add kestrel server"+node.getName());
                }

            }
            logger.debug("The client Kestrel is starting ");
            while (alive)
            {
                String host="localhost";
                int port=0;
                try
                {
                    for(String nodeName : nodesKestrelServer)
                    {
                        String queue;
                        host = getAddress(nodeName);
                        port = parsePortNumber(nodeName);
                        queue = getQueueName(nodeName);
                        KestrelClient client =getOrCreateKestrel(host,port);
                        Message msg =    client.dequeue(queue);
                        logger.debug("dequeue msg from "+host+" "+port+" "+queue);
                        msg.setDestNodeName(getNodeName());
                        remoteDispatch(msg);
                    }
                } catch (EOFException e)
                {
                    logger.debug("no message to dequeue");

                } catch (Exception e)
                {
                    delete_link(host,port);
                    logger.debug(""+e);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {

                    }
                }
            }
        }
    }

    public void delete_link(String host,Integer port){
        logger.debug("remove link "+host+" "+port);
        try {

            clientsKestrel.get(host+port).disconnect();
            clientsKestrel.remove(host+port);
        } catch (Exception e)
        {
            //ignore
        }

    }
    public KestrelClient getOrCreateKestrel(String host,Integer port) throws IOException {
        KestrelClient client_consumer = null;

        if (clientsKestrel.containsKey(host+port)) {
            //  logger.debug("the link exist");
            client_consumer = clientsKestrel.get(host+port);
        } else {
            client_consumer = new KestrelClient(host,port);
            client_consumer.connect();

            clientsKestrel.put(host+port, client_consumer);
        }
        return client_consumer;
    }



}
