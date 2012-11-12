package org.kevoree.library.javase;

import jexxus.client.ClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.ServerConnection;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/11/12
 * Time: 15:46
 */
public class NetworkSender {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void sendMessage(KevoreeMessage.Message m , InetSocketAddress addr){

        final ClientConnection[] conns = new ClientConnection[1];
        conns[0] = new ClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
            }
            @Override
            public void receive(byte[] data, Connection from) {
            }
            @Override
            public void clientConnected(ServerConnection conn) {
            }
        }, addr.getAddress().getHostAddress(), addr.getPort(), true);
        try {
            conns[0].connect();
        } catch (IOException e) {
           logger.error("",e);
        }
        logger.debug("Try to connect to "+addr.getAddress().getHostAddress()+":"+addr.getPort());
        conns[0].send(m.toByteArray(), Delivery.RELIABLE);
    }

}
