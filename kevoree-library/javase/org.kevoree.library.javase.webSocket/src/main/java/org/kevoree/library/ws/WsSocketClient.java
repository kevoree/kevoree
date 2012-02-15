package org.kevoree.library.ws;

import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.WebSocketClient;

import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/02/12
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
public class WsSocketClient extends WebSocketClient {

    public WsSocketClient(URI serverURI) {
        super(serverURI);
    }

    public void onMessage(String message) {
        System.out.println("rec=" + message);
    }

    public void onOpen(Handshakedata handshake) {
        try {
            send("hello server");
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void onClose(int code, String reason, boolean remote) {

    }

    public void onError(Exception ex) {ex.printStackTrace();
    }


}
