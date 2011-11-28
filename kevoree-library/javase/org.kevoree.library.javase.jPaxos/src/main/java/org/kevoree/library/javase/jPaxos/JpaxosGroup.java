package org.kevoree.library.javase.jPaxos;


import lsr.common.Configuration;
import lsr.common.PID;
import lsr.paxos.ReplicationException;
import lsr.paxos.client.Client;
import lsr.paxos.replica.Replica;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 25/11/11
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */

@DictionaryType({
        @DictionaryAttribute(name = "localId", defaultValue = "0", optional = false , fragmentDependant = true),
        @DictionaryAttribute(name = "replicaPort", defaultValue = "2021", optional = true , fragmentDependant = true),
        @DictionaryAttribute(name = "clientPort", defaultValue = "3001", optional = true , fragmentDependant = true)
})
@GroupType
@Library(name="JavaSE")
public class JpaxosGroup extends AbstractGroupType implements  Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int localId;
    private int localReplicaPort;
    private int localClientPort;
    Thread handler=null;
    private List<PID> processess = new ArrayList<PID> ();
    private Configuration _conf;
    private Replica _replica;

    @Start
    public void startJpaxosRestGroup() throws IOException {
        updateDico();
        handler =new Thread(this);
        handler.start();
    }


    public String getAddressModel(String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public int parseNumberModel(String nodeName,String namePort) throws IOException {
        try
        {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentGroup(this.getModelService().getLastModel(), this.getName(), namePort, nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }


    public void updateDico() throws NumberFormatException {
        try
        {
            localId=  Integer.parseInt(this.getDictionary().get("localId").toString());
            localReplicaPort=  Integer.parseInt(this.getDictionary().get("replicaPort").toString());
            localClientPort=  Integer.parseInt(this.getDictionary().get("clientPort").toString());
        } catch (Exception e)
        {
            throw new NumberFormatException("updateDico"+e);
        }
    }

    @Stop
    public void stopJpaxosRestGroup()
    {
        _replica.forceExit();
    }

    public ContainerRoot update(List<PID> processess,ContainerRoot _model) throws IOException, ReplicationException {
        Client client = null;
        client = new Client(new Configuration(processess));
        client.connect();
        KevoreeJpaxosCommand command = new KevoreeJpaxosCommand(_model);
        byte[] request = command.toByteArray();
        /** Executing the request **/
        byte[] response = client.execute(request);
        /** Deserialising answer **/
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));
        ContainerRoot model = KevoreeXmiHelper.loadStream(in);
        return model;
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

    @Override
    public void triggerModelUpdate() {

        try
        {
            ContainerRoot model=   update(processess, getModelService().getLastModel());

        } catch (Exception e) {
            logger.error("triggerModelUpdate "+e);
        }

    }

    @Override
    public void push(ContainerRoot containerRoot, String s) {

    }

    @Override
    public ContainerRoot pull(String s) {
        return null;
    }

    public void updateReplica() throws IOException {
        PID pid;
        if(_replica !=null)
            processess.clear();

        for(String _node : getAllNodes()){
            String hostname = getAddressModel(_node);
            int id =parseNumberModel(_node,"localId");
            int replicaport = parseNumberModel(_node,"replicaPort");
            int clientport  = parseNumberModel(_node,"clientPort");
            pid = new PID(id, hostname,replicaport,clientport);
            processess.add(pid);
            logger.debug(pid.toString());
        }
        _conf = new Configuration(processess);
        _replica= new Replica(_conf, localId, new KevoreeJPaxosService());
        _replica.start();
    }

    @Override
    public void run()
    {
        try {
            updateReplica();
        } catch (Exception e) {
            logger.error("Starting Group "+e);
        }
    }
}
