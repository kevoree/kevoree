package org.kevoree.library.javase;

import jexxus.client.ClientConnection;
import jexxus.client.UniClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.ServerConnection;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage;
import org.kevoree.library.javase.basicGossiper.GossiperProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/11/12
 * Time: 15:46
 */
public class NetworkSender {

    private GossiperProcess process = null;

    public NetworkSender(GossiperProcess p) {
        process = p;
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void sendMessageUnreliable(KevoreeMessage.Message m, InetSocketAddress addr) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(2);
            m.writeTo(output);
            byte[] message = output.toByteArray();
            output.close();
            DatagramPacket packet = new DatagramPacket(message, message.length, addr);
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
        } catch (Exception e) {
            logger.debug("", e);
        }
    }


    public void sendMessage(KevoreeMessage.Message m, InetSocketAddress addr) {
        UniClientConnection conn = null;
        try {
            conn = new UniClientConnection(new ConnectionListener() {

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
            conn.connect();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(2);
            m.writeTo(output);
            conn.send(output.toByteArray(), Delivery.RELIABLE);
            output.close();
        } catch (Exception e) {
            logger.debug("", e);
        } finally {
            if (conn != null && conn.isConnected()) {
                try {
                    conn.close();
                } catch (Exception e){

                }
            }
        }
    }

}
