/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.kevoree.framework.AbstractComponentType;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 *
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
@Requires({
    @RequiredPort(name = "records", type = PortType.MESSAGE),
    @RequiredPort(name = "calibrations", type = PortType.MESSAGE)
})
@Provides({
    @ProvidedPort(name = "log", type = PortType.MESSAGE)
})
public class HadoopNameNode extends AbstractComponentType {

    private static final Logger LOG = Logger.getLogger(HadoopNameNode.class.getName());
    private Thread nameNodeThread;
    private NameNode nameNode;
    private Configuration configuration;

    public HadoopNameNode(Configuration conf) {
		this.configuration = conf;
	}
    
    
    public void start() throws RemoteException, IOException,
            InterruptedException {

        LOG.info("Starting NameNode!");
	nameNode = new NameNode(configuration);			

    }

    public void stop() throws IOException {
        nameNode.stop();
    }

}
