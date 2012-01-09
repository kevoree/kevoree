package org.kevoree.library.javase.jPaxos;

import lsr.service.AbstractService;
import lsr.service.SimplifiedService;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 25/11/11
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeJPaxosService extends SimplifiedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ContainerRoot currentContainerRoot;
    private  KevoreeModelHandlerService _handler;
    private UUID last;
    public KevoreeJPaxosService(KevoreeModelHandlerService handler){
        this._handler = handler;
    }
    /** Processes client request and returns the reply for client **/
    @Override
    public byte[] execute(byte[] bytes) {
        logger.debug("execute ");
        try
        {
            // Deserialise the client command
            KevoreeJpaxosCommand newmodel;
            newmodel = new KevoreeJpaxosCommand(bytes);
            ContainerRoot oldModel = currentContainerRoot;
            currentContainerRoot =newmodel.getLastModel();

            // return the oldModel
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(outStream, currentContainerRoot);
            outStream.flush();
            return   outStream.toByteArray();

        } catch (IOException e)
        {
            logger.error("execute "+e);
        }
        return null;
    }

    @Override
    protected byte[] makeSnapshot() {
        logger.debug("makeSnapshot ");
        // In order to make the snapshot, we just serialise the model
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
    public void updateToSnapshot(byte[] bytes) {
        logger.debug("updateToSnapshot ");

        // For map service the "recovery" is just recreation of underlaying model
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(stream);
            currentContainerRoot = (ContainerRoot) objectInputStream.readObject();

        } catch (Exception e) {
            throw new RuntimeException("Snapshot read error");
        }
    }
}
