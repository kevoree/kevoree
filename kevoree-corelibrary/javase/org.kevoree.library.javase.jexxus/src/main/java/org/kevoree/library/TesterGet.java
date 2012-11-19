package org.kevoree.library;

import jexxus.client.ClientConnection;
import jexxus.client.UniClientConnection;
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
public class TesterGet implements Runnable {

    int i = 0;

    public TesterGet setI(int _i){
       i = _i;
        return this;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        for (int i = 0; i < 1; i++) {
             new Thread(new TesterGet().setI(i)){}.start();
        }
    }

    @Override
    public void run() {
        try {
            final Exchanger<ContainerRoot> exchanger = new Exchanger<ContainerRoot>();
            final UniClientConnection[] conns = new UniClientConnection[1];
            conns[0] = new UniClientConnection(new ConnectionListener() {
                @Override
                public void connectionBroken(Connection broken, boolean forced) {
                    System.out.println("ConnectionBroken");
                }

                @Override
                public void receive(byte[] data, Connection from) {
                }

                @Override
                public void clientConnected(ServerConnection conn) {
                    System.out.println("Connected");
                }

            }, "192.168.1.121", 8000, false);
            conns[0].connect(3000);
            byte[] data = new byte[10];
            data = ("HelloFrom="+i).getBytes();
            conns[0].send(data, Delivery.RELIABLE);
            //ContainerRoot rec = exchanger.exchange(null);
           // System.out.println(KevoreeXmiHelper.saveToString(rec, true));
            conns[0].close();

        }catch(Exception e){
            e.printStackTrace();
        }    }
}
