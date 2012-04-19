package org.kevoree.library.voldemortChannels;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 18/04/11
 * Time: 08:58
 */

import org.apache.commons.io.FileUtils;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.extra.voldemort.KUtils;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import voldemort.client.StoreClient;
import voldemort.cluster.Node;
import voldemort.server.VoldemortConfig;
import voldemort.utils.Props;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "id", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "clusterName", defaultValue = "kevoreeCluster", optional = false),
        @DictionaryAttribute(name = "httpPort", defaultValue = "", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "socketPort", defaultValue = "", optional = false , fragmentDependant = true),
        @DictionaryAttribute(name = "adminPort", defaultValue = "", optional = false, fragmentDependant = true),
        @DictionaryAttribute(name = "partitions", defaultValue = "", optional = false, fragmentDependant = true)
})

public class VoldemortChannels extends AbstractChannelFragment implements Runnable {

    private Logger logger = LoggerFactory.getLogger(VoldemortChannels.class);
    private ChannelClassResolver resolver = new ChannelClassResolver(this);
    private  List<Node> nodes = new ArrayList<Node>();
    private KServer currentNODE=null;
    private Thread handler=null;
    final Semaphore sem = new java.util.concurrent.Semaphore(1);
    private  Boolean alive=true;
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

    public String getAddress(String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }


    private VoldemortConfig createServerConfig(int nodeId) throws IOException
    {
        Props props = new Props();
        props.put("node.id", nodeId);
        props.put("voldemort.home", KUtils.createTempDir() + "/node-" + nodeId);

        props.put("bdb.cache.size", 1 * 1024 * 1024);
        props.put("jmx.enable", "false");
        VoldemortConfig config = new VoldemortConfig(props);


        // clean and reinit metadata dir.
        File tempDir = new File(config.getMetadataDirectory());
        tempDir.mkdirs();

        File tempDir2 = new File(config.getDataDirectory());
        tempDir2.mkdirs();

        //    FileUtils.copyFileToDirectory(new File(this.getClass().getClassLoader().getResource("config/one-node-cluster.xml").getPath()), tempDir);
        FileUtils.copyInputStreamToFile(this.getClass().getClassLoader().getResourceAsStream("config/stores.xml"),new File(tempDir.getPath()+"/stores.xml"));
        return config;
    }

    public String getAddressModel(String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }


    public List<String> getAllNodes () {
        ContainerRoot model = this.getModelService().getLastModel();
        for (Object o : model.getGroupsForJ()) {
            Group g = (Group) o;
            List<String> peers = new ArrayList<String>(g.getSubNodes().size());
            for (ContainerNode node : g.getSubNodesForJ()) {
                peers.add(node.getName());
            }
            return peers;
        }
        return new ArrayList<String>();
    }


    public int getport(String nodeName,String port) throws IOException {
        try {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentChannel(getModelService().getLastModel(), getName(), port, nodeName);
        } catch (NumberFormatException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    @Start
    public void startChannel() {
        handler = new Thread(this);
        handler.start();
    }

    @Stop
    public void stopChannel() {
        alive = false;
        currentNODE.stop();
        handler.interrupt();
    }

    @Update
    public void updateChannel() {
        currentNODE.stop();

        currentNODE.start();
    }


    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message msg) {
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
                try {
                    KClient t = new KClient(nodes);
                    StoreClient store = t.getStore("kevoree");
                    store.put(remoteNodeName,msg);

                } catch (Exception e) {

                    logger.error(e.toString());
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





    @Override
    public void run() {

        try
        {
            List<String> listNodes =   getAllNodes();
            logger.error("Number of node "+listNodes.size());

            for(String _node : listNodes)
            {
                String hostname = getAddressModel(_node);
                int id =getport(_node, "id");
                int httpPort = getport(_node, "httpPort");
                int socketPort  = getport(_node, "socketPort");
                int adminPort  = getport(_node, "adminPort");
                String partition =   KevoreeFragmentPropertyHelper.getPropertyFromFragmentChannel(getModelService().getLastModel(), getName(), "partitions", _node);
                List<Integer> partitions =  new ArrayList<Integer>();
                StringTokenizer st = new StringTokenizer(partition, ";");
                while (st.hasMoreTokens())
                {
                    partitions.add(Integer.parseInt(st.nextToken()));
                }
                logger.error("Node "+id+" httpPort="+httpPort+" "+" socketPort="+socketPort+" adminPort="+adminPort+" partitions="+partitions);
                Node node0 = new Node(id,hostname,httpPort,socketPort,adminPort,partitions);
                nodes.add(node0);
            }


        } catch (IOException e) {
            logger.error("The cluster can't be configure "+e);
        }

        try {

            if(!nodes.isEmpty())
            {
                String clustername = this.getDictionary().get("clusterName").toString();
                int id=  Integer.parseInt(this.getDictionary().get("id").toString());
                currentNODE = new KServer(createServerConfig(id),clustername,nodes);
                currentNODE.start();
            }

        } catch (Exception e)
        {
            logger.error("Running cluster "+e);
        }

        KClient t = new KClient(nodes);



        while(alive)
        {

            StoreClient store = t.getStore("kevoree");


            store.
            if(store.get(getNodeName()) != null)
            {
                Message c = (Message) store.get(this.getNodeName()).getValue();
                remoteDispatch(c);
            }



        }

    }

}
