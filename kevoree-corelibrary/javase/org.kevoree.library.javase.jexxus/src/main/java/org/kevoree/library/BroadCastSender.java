package org.kevoree.library;

import java.net.*;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/12
 * Time: 14:43
 */
public class BroadCastSender {

    public static void send(int port, byte[] sendData) {
        DatagramSocket c = null;
        try {
            c = new DatagramSocket();
            c.setBroadcast(true);
            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
                c.send(sendPacket);
               // System.out.println(">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            } catch (Exception e) {
            }
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) {
                    }
                   // System.out.println(">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception ex) {

            }
        }
    }

}
