/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.NameNode;

import org.kevoree.annotation.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
public class HadoopNameNode extends HadoopComponent {

    private static final Logger LOG = LoggerFactory.getLogger(HadoopNameNode.class.getName());
    private NameNode nameNode;

    public HadoopNameNode() {
        super();
    }

    @Start
    public void start() throws RemoteException, IOException,
            InterruptedException {
        
        LOG.info("Entering: START()");

        Configuration configuration = this.getConfiguration();
        // Local dirs

        String dfsDir = configuration.get("hadoop.tmp.dir");
        LOG.debug("DFS dir: " + dfsDir);
        new File(dfsDir).mkdirs();

        //if (!new File("/tmp/hadoop/dfs/name/image/fsimage").exists()) {
            NameNode.format(configuration);
        //}

            
   
            
        // Set NameNode address
        InetAddress i = InetAddress.getLocalHost();
        String hostName = i.getHostName();


        configuration.set("hadoop.namenode", hostName);
        configuration.set("dfs.namenode.http-address", hostName);

        configuration.set("dfs.info.bindAddress", i.getHostName());
        //configuration.set("dfs.info.port", configuration.get("hadoop.namenode.port"));


        String nnhost = "hdfs://" + hostName + ":" + configuration.get("hadoop.namenode.port");
        configuration.set("fs.default.name", nnhost);
        
        configuration.set("dfs.http.address", "http://" + hostName + ":" + configuration.get("dfs.info.port"));

        LOG.info("Starting NameNode!");

        HadoopConfiguration cfg = new HadoopConfiguration(configuration);
        cfg.writeHdfsSite();
        cfg.writeCoreSite();

        new Thread() {
            @Override
            public void run() {
                runNameNode();
            }
        }.start();
    }

    public void runNameNode() {
        try {
            nameNode = new NameNode(getConfiguration());
            nameNode.join();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Update
    public void update() {
        
    }
    
    @Stop
    public void stop() throws IOException {
        if (nameNode != null) {
            nameNode.stop();
        }
    }

    public static void main(String[] args) {

        try {
            HadoopNameNode node = new HadoopNameNode();
            node.start();
            //Thread.sleep(10000);
            //node.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}