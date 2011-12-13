package org.kevoree.library.jmdnsrest;

import org.kevoree.ContainerRoot;
import org.kevoree.DictionaryValue;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.kevoree.library.rest.RestGroup;
import org.kevoree.library.rest.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 18:27
 */


@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = false , fragmentDependant = true),
        @DictionaryAttribute(name = "timer", defaultValue = "5000", optional = false, fragmentDependant = true)   // gap between request scan of the network
})
@GroupType
@Library(name="JavaSE")
public class JmDNSRestGroup extends RestGroup implements  Runnable{

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ArrayList<JmDnsComponent> jmdns = null ;
    private  Thread thread = null;
    private  boolean  alive;
    private int timer;

    @Start
    public void startJmdnsGroup() {
        super.startRestGroup();
        jmdns = new ArrayList<JmDnsComponent>();
        thread = new Thread(this);
        updateDico();
        alive = true;
        thread.start();
    }

    @Stop
    public void stopJmdnsGroup() {
        alive = false;
        thread.interrupt();
        for(JmDnsComponent _jmdns : jmdns){
            _jmdns.close();
        }
        jmdns = null;
        super.stopRestGroup();
    }
    @Update
    public void updateJmDNSGroup(){
        updateDico();
    }

    public void updateDico(){
        try{
            timer = Integer.parseInt(getDictionary().get("timer").toString());
        } catch (Exception e){
            timer = 9000;
        }
    }


    public ArrayList<InetAddress> getIps(){
        ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface interfaceN = (NetworkInterface)interfaces.nextElement();
                Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
                while (ienum.hasMoreElements()) {
                    InetAddress ia = ienum.nextElement();
                    if(!ia.getHostAddress().toString().startsWith("127")){
                        ips.add(ia);
                    }

                }
            }
        }
        catch(Exception e){
            logger.error("pas de carte reseau " + e);
        }

        return ips;
    }

    @Override
    public void run() {
        ArrayList<InetAddress> ips =    getIps();
        ContainerRoot model = getModelService().getLastModel();
        for(InetAddress ip : ips)
        {
            KevoreePlatformHelper.updateNodeLinkProp(model,this.getNodeName(),this.getNodeName(),org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(),ip.getHostName(),"LAN", 100);
            KevoreePlatformHelper.updateNodeLinkProp(model,this.getNodeName(),this.getNodeName(), org.kevoree.framework.Constants.KEVOREE_MODEL_PORT(),this.getDictionary().get("port").toString(), "LAN", 100);
        }
        getModelService().updateModel(model);

        for(InetAddress ip : ips)
        {
            jmdns.add(new JmDnsComponent(this.getNodeName(),this.getName() ,Integer.parseInt(this.getDictionary().get("port").toString()),this.getModelService(),"JmDNSRestGroup",ip));
        }
        while(alive)
        {
            for(JmDnsComponent _jmdns : jmdns){
                _jmdns.requestUpdateList(timer);
            }
            try {
                Thread.sleep(timer);
            } catch (InterruptedException e) {
                logger.debug("requestUpdateList "+e);
            }
        }
    }
}
