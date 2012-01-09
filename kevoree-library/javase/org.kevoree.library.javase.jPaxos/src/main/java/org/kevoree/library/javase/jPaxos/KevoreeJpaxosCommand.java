package org.kevoree.library.javase.jPaxos;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 25/11/11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeJpaxosCommand implements Serializable{

    private static final long serialVersionUID = 1L;
    private final ContainerRoot model;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public KevoreeJpaxosCommand(ContainerRoot _model)
    {
        this.model =   _model;
    }


    public ContainerRoot getLastModel(){
        return  model;
    }

    public KevoreeJpaxosCommand(byte[] bytes) throws IOException
    {
        DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(bytes));
        model =   KevoreeXmiHelper.loadStream(dataInput);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        KevoreeXmiHelper.saveStream(outStream, model);
        outStream.flush();
        return  outStream.toByteArray();
    }

}
