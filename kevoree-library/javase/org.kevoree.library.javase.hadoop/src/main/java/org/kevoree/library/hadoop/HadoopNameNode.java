/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

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

        Configuration configuration = this.getConfiguration();

        // Set NameNode address
        InetAddress i = InetAddress.getLocalHost();
        configuration.set("hadoop.namenode", i.getHostName());

        LOG.info("Starting NameNode!");
        
        
       new Thread(new Runnable() {

            public void run() {
                try {
                    nameNode = new NameNode(getConfiguration());
                    
                }
                catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }).start();
        
    }

    
    @Stop
    public void stop() throws IOException {
        nameNode.stop();
    }
}