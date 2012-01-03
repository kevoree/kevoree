package test2;

import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.test.MapServiceCommand;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.ContainerRootImpl;
import org.kevoree.library.javase.jPaxos.KevoreeJpaxosCommand;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 19/12/11
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public class client {

    public static void main(String[] args) {
        try {
            /** Creating the Client object **/
            Client client = new Client();
            client.connect();



/** Prepairing request **/
            MapServiceCommand command = new MapServiceCommand(new Long(1),new Long(10));
            byte[] request = command.toByteArray();

/** Executing the request **/
            byte[] response = client.execute(request);

/** Deserialising answer **/
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response));

            System.out.println(in.readLong());


        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
