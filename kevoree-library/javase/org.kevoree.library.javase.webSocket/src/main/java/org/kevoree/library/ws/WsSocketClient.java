package org.kevoree.library.ws;

import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.WebSocketClient;
import org.kevoree.framework.message.Message;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/02/12
 * Time: 21:52
 */
public class WsSocketClient extends WebSocketClient {

    private List<Message> msgs = new ArrayList<Message>();
    private Boolean connected = false;

    public void sendMessage(Message msg) throws Exception{
       if(connected){
           System.out.println("Direct send");
           send(msg.getContent().toString());
       } else {
           System.out.println("Can send :-) endque");
           msgs.add(msg);
       }
    }
    
    public WsSocketClient(URI serverURI) {
        super(serverURI);
    }

    public void onMessage(String message) {
        System.out.println("rec=" + message);
    }

    public void onOpen(Handshakedata handshake) {
        connected = true;
        for(Message msg : msgs){
            try {
                sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        msgs.clear();
    }

    public void onClose(int code, String reason, boolean remote) {
        //TODO REJEU ;-)
        connected = false;
    }

    public void onError(Exception ex) {
        ex.printStackTrace();
    }


}
