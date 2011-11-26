package org.kevoree.library.javase.restJpaxos.jpaxos;

import org.kevoree.ContainerRoot;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
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
    private ContainerRoot model;


    public KevoreeJpaxosCommand(ContainerRoot _model)
    {
        this.model =   _model;
    }


    public ContainerRoot getLastModel(){
        return  model;
    }


    public KevoreeJpaxosCommand(byte[] bytes) throws IOException {
        DataInputStream dataInput = new DataInputStream(new ByteArrayInputStream(bytes));

        //TODO  regarder de plus près KevoreeXmiHelper
    }

    public byte[] toByteArray() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(100);

        //TODO    regarder de plus près KevoreeXmiHelper

        return  buffer.array();
    }

}
