package org.kevoree.library.ws;

import net.tootallnate.websocket.WebSocket;

import java.net.URI;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/02/12
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class Tester  {

    public static void main(String[] args){

        try {
         //   WebSocket.DEBUG = true;
           /// WsServer ws = new WsServer();
          //  ws.start();
            
            System.out.println("Server Started !");

            WsSocketClient client = new WsSocketClient(new URI("ws://localhost:9090"));
            client.connect();



        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
