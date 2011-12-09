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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 18:27
 */


@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true , fragmentDependant = true),
        @DictionaryAttribute(name = "timer", defaultValue = "5000", optional = true, fragmentDependant = true)   // gap between request scan of the network
})
@GroupType
@Library(name="JavaSE")
public class JmDNSRestGroup extends RestGroup implements  Runnable{

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JmDnsComponent jmdns = null ;
    private  Thread thread = null;
    private  boolean  alive;
    private int timer=2000;
    @Start
    public void startJmdnsGroup() {
        super.startRestGroup();
        jmdns = new JmDnsComponent(this.getNodeName(),this.getName() ,Integer.parseInt(this.getDictionary().get("port").toString()),this.getModelService(),"JmDNSRestGroup");
        thread = new Thread(this);
        updateDico();
        alive = true;
        thread.start();
    }

    @Stop
    public void stopJmdnsGroup() {

        alive = false;
        thread.interrupt();
        jmdns.close();
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
            logger.error("Parse timer refresh listener"+e);
        }
    }

    @Override
    public void run() {
        while(alive)
        {
            jmdns.requestUpdateList();
            try {
                Thread.sleep(timer);
            } catch (InterruptedException e) {
                logger.debug("requestUpdateList "+e);
            }
        }
    }
}
