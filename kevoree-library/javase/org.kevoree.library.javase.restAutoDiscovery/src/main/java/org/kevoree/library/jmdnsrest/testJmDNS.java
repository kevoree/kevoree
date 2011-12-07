package org.kevoree.library.jmdnsrest;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 06/12/11
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

public class testJmDNS {



    public static void main (String[] args){

        String REMOTE_TYPE = "_kevoree-remote._tcp.local." ;
        try {
            JmDNS jmdns =  JmDNS.create("KevoreeEditor");

            System.out.println("Ecoute sur "+jmdns.getInterface());

            for(ServiceInfo ser : jmdns.list(REMOTE_TYPE)){
                System.out.print(ser.getKey());
                System.out.println(ser.getApplication() +" "+ser.getAddress()+" "+ser.getDomain()+" "+ser.getPort());
                System.out.println("text = <"+new String(ser.getTextBytes())+">");

            }

            System.out.println("Fin");

            jmdns.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}