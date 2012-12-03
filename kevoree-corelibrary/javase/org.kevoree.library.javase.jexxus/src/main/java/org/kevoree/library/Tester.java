package org.kevoree.library;

import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.framework.KevoreeXmiHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 07/11/12
 * Time: 17:25
 */
public class Tester {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        Server server = new Server(new ConnectionListener(){

            @Override
            public void connectionBroken(Connection broken, boolean forced) {
                System.out.println("ConnectionBroken");
            }

            @Override
            public void receive(byte[] data, Connection from) {

                //if(!from.isConnected()){
                    System.out.println("recData="+new String(data)+"@"+from.getIP());
                //}

                //ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                //ContainerRoot root = KevoreeXmiHelper.loadCompressedStream(inputStream);
                //System.out.println(root);
            }

            @Override
            public void clientConnected(ServerConnection conn) {
                System.out.println("Connected");
            }

        }, 15652,15652,false);
        server.startServer();
        /*
        ClientConnection conn = new ClientConnection(new ConnectionListener(){

            @Override
            public void connectionBroken(Connection broken, boolean forced) {
                System.out.println("ConnectionBroken");
            }

            @Override
            public void receive(byte[] data, Connection from) {
                System.out.println("recData="+new String(data));
            }

            @Override
            public void clientConnected(ServerConnection conn) {
                System.out.println("Connected");
            }


        }, "localhost", 15652,true);
       */
        //conn.connect();
        //send with the TCP Protocol
              /*
        ContainerRoot root = KevoreeFactory.createContainerRoot();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        KevoreeXmiHelper.saveCompressedStream(output,root);

        conn.send(output.toByteArray(), Delivery.RELIABLE);

        conn.send("HelloUDP".getBytes(),Delivery.UNRELIABLE);
         */

    }

}
