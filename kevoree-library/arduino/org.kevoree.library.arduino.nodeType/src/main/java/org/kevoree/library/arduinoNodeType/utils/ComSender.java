/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType.utils;

import gnu.io.*;
import org.kevoree.extra.osgi.rxtx.KevoreeSharedCom;
//import org.kevoree.library.arduinoNodeType.com.TwoWayActors;

import java.io.IOException;
//import java.util.HashMap;
import java.util.Random;

/**
 * @author ffouquet
 */
public class ComSender {

   // private static HashMap<String, TwoWayActors> map = new HashMap<String, TwoWayActors>();

    public static Boolean send(String msg, String portName) throws NoSuchPortException, PortInUseException, IOException, InterruptedException {
           /*
        if (!map.containsKey(portName)) {
            map.put(portName, new TwoWayActors(portName));
            System.out.println("Add " + portName);
        }

        TwoWayActors com = map.get(portName);
         */

        int token = new Random().nextInt(10);

        String msgToSend = "$"+token+msg;


        boolean result = KevoreeSharedCom.sendSynch(portName,msgToSend, "ack" + token, 1000);

        //boolean result = com.sendAndWait(msgToSend, "ack" + token, 3000);


        System.out.println("Send => " + msgToSend);
        System.out.println("Result => " + result);


        return true;
    }
        /*
    public static void closeAllPreviousPort() {


        for (String key : map.keySet()) {
            TwoWayActors com = map.get(key);
            if (com != null) {
                 com.killConnection();
            }
        }
        map.clear();
    }   */
}
