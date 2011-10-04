/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.NameNode;

import org.kevoree.annotation.*;

/**
 *
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
public class HadoopNameNode extends HadoopComponent {

    private static final Logger LOG = Logger.getLogger(HadoopNameNode.class.getName());
    private NameNode nameNode;

    public HadoopNameNode() {
        super();
    }

    public void start() throws RemoteException, IOException,
            InterruptedException {

        Configuration configuration = this.getConfiguration();

        // Set NameNode address
        InetAddress i = InetAddress.getLocalHost();
        configuration.set("hadoop.namenode", i.getHostName());

        LOG.info("Starting NameNode!");
        nameNode = new NameNode(configuration);
    }

    public void stop() throws IOException {
        nameNode.stop();
    }
}