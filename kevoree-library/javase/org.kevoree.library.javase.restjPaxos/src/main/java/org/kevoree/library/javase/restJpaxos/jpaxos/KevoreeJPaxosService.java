package org.kevoree.library.javase.restJpaxos.jpaxos;

import lsr.paxos.test.MapServiceCommand;
import lsr.service.SimplifiedService;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeContainer;

import java.io.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 25/11/11
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeJPaxosService extends SimplifiedService {

    private ContainerRoot currentContainerRoot;

    @Override
    protected byte[] execute(byte[] bytes) {
           try
           {
            // Deserialise the client command
            KevoreeJpaxosCommand command;

            command = new KevoreeJpaxosCommand(bytes);

            ContainerRoot oldModel = currentContainerRoot;

             currentContainerRoot =command.getLastModel();

            // TODO serialize  ContainerRoot



           // TODO return serialized oldModel
            return null;

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }
    /** Makes snapshot used for recovery and replicas that have very old state **/
    @Override
    protected byte[] makeSnapshot() {
        // In order to make the snapshot, we just serialise the map
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
            objectOutputStream.writeObject(currentContainerRoot);
        } catch (IOException e) {
            throw new RuntimeException("Snapshot creation error");
        }
        return stream.toByteArray();
    }

    /** Brings the system up-to-date from a snapshot **/
    @Override
    protected void updateToSnapshot(byte[] snapshot) {
        // For map service the "recovery" is just recreation of underlaying map
        ByteArrayInputStream stream = new ByteArrayInputStream(snapshot);
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(stream);
            currentContainerRoot = (ContainerRoot) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Snapshot read error");
       }
    }
}
