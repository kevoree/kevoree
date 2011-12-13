package org.kevoree.library.jmdnsrest;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 06/12/11
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.ContainerRoot;
import org.kevoree.NetworkProperty;
import org.kevoree.NodeLink;
import org.kevoree.NodeNetwork;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.impl.ContainerRootImpl;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

public class testJmDNS {



    public static void main (String[] args){

        /*   String REMOTE_TYPE = "_kevoree-remote._tcp.local." ;
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
        */


        ArrayList<String> ips = new ArrayList<String>();
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaceN = (NetworkInterface)interfaces.nextElement();
                Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
                while (ienum.hasMoreElements()) {
                    InetAddress ia = ienum.nextElement();
                    String adress = ia.getHostAddress().toString();
                    ips.add(adress);

                }
            }
        }
        catch(Exception e){

        }
        ContainerRoot model = new ContainerRootImpl();

        KevoreePlatformHelper.updateNodeLinkProp(model, "node0", "node0", org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(), "192.168.1.1", "LAN", 100);
        KevoreePlatformHelper.updateNodeLinkProp(model, "node0", "node0", org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(), "192.168.1.2", "LAN2", 100);

        for( NodeNetwork node : model.getNodeNetworksForJ())
        {

            for( NodeLink lins : node.getLinkForJ())  {
                for( NetworkProperty prop : lins.getNetworkPropertiesForJ())  {
                    System.out.println(prop.getValue()+" "+prop.getName()+" "+prop.getLastCheck());
                }
            }

        }
    }
    }