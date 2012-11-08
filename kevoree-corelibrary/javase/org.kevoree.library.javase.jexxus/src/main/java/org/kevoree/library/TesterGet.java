package org.kevoree.library;

import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.ServerConnection;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Exchanger;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 07/11/12
 * Time: 18:01
 */
public class TesterGet {

    public static void main(String[] args) throws IOException, InterruptedException {

        final Exchanger<ContainerRoot> exchanger = new Exchanger<ContainerRoot>();

        final ClientConnection[] conns = new ClientConnection[1];
        conns[0] = new ClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
                System.out.println("ConnectionBroken");
            }

            @Override
            public void receive(byte[] data, Connection from) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                final ContainerRoot root = KevoreeXmiHelper.loadCompressedStream(inputStream);
                try {
                    exchanger.exchange(root);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(KevoreeXmiHelper.saveToString(root, true));
                conns[0].close();
            }

            @Override
            public void clientConnected(ServerConnection conn) {
                System.out.println("Connected");
            }

        }, "localhost", 8000, true);
        conns[0].connect();
        byte[] data = new byte[1];
        data[0] = 0;
        conns[0].send(data, Delivery.RELIABLE);
        ContainerRoot rec = exchanger.exchange(null);
        System.out.println(KevoreeXmiHelper.saveToString(rec, true));
    }

}
