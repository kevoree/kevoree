package org.kevoree.library.javase.restJpaxos;


import lsr.common.Configuration;
import lsr.common.PID;
import lsr.paxos.client.Client;
import lsr.paxos.replica.Replica;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.library.javase.restJpaxos.jpaxos.KevoreeJPaxosService;
import org.kevoree.library.javase.restJpaxos.jpaxos.KevoreeJpaxosCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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
public class JpaxosRestGroup extends AbstractGroupType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int localId;
    private int localReplicaPort;
    private int localClientPort;
    private  PID localpid;
    private List<PID> processess = new ArrayList<PID> ();
    private Configuration _conf;
    private Replica _replica;

    @Start
    public void startJpaxosRestGroup() {

        try {
            updateDico();

            localpid = new PID(localId,"localhost",localReplicaPort,localClientPort);

            processess.add(localpid);
            // TODO load PID from others nodes

            _conf = new Configuration(processess);

            _replica= new Replica(_conf, localId, new KevoreeJPaxosService());
            _replica.start();
        } catch (IOException e)
        {
            logger.error("StartJpaxosRestGroup : "+e.toString());

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

    @Override
    public void triggerModelUpdate() {
        Client client = null;
        try
        {

            // TODO Pre-updateSynchro

            client = new Client(new Configuration(processess));
            client.connect();

            KevoreeJpaxosCommand  command = new KevoreeJpaxosCommand(getModelService().getLastModel());

            byte[] request = command.toByteArray();

            /** Executing the request **/
            byte[] response = client.execute(request);

            /** Deserialising answer **/
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));

            // TODO in.readmodel   regarder de plus près KevoreeXmiHelper

        } catch (Exception e) {
            logger.debug("triggerModelUpdate "+e.toString());
        }

    }

    @Override
    public void push(ContainerRoot containerRoot, String s) {

    }

    @Override
    public ContainerRoot pull(String s) {
        return null;
    }
}
