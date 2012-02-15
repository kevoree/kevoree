package org.kevoree.library.ws;

import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;
import org.kevoree.framework.message.Message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/02/12
 * Time: 21:37
 */
public class WsServer extends WebSocketServer {

    WsChannel rootChannel = null;

    public WsServer() throws UnknownHostException {
        super(new InetSocketAddress( "0.0.0.0",9090));
    }

    public WsServer(InetSocketAddress address,WsChannel root) {
        super(address);
        rootChannel = root;
    }

    @Override
    public void onClientOpen(WebSocket conn, Handshakedata handshake) {

    }

    @Override
    public void onClientClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onClientMessage(WebSocket conn, String message) {
        Message msg = new Message();
        msg.setContent(message);
        if(rootChannel != null){
            rootChannel.remoteDispatch(msg);
        } else {
            System.out.println("Lost msg "+message);
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}
