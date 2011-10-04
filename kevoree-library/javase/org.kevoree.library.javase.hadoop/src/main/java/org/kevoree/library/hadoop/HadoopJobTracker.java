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
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobTracker;

import org.kevoree.annotation.*;

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
public class HadoopJobTracker extends HadoopComponent implements Runnable {

    private static final Logger LOG = Logger.getLogger(HadoopJobTracker.class.getName());
    private JobTracker tracker;

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HadoopJobTracker() {
        super();
    }

    @Start
    public void start() throws RemoteException, IOException,
            InterruptedException {
        
        Configuration configuration = this.getConfiguration();
        InetAddress i = InetAddress.getLocalHost();
        configuration.set("hadoop.jobtracker", i.getHostName());
        
        tracker = JobTracker.startTracker(new JobConf(configuration));
        tracker.offerService();
    }

    @Stop
    public void stop() throws IOException {

        tracker.stopTracker();

    }
}

/*
 # Addresses
hadoop.jobtracker=localhost
hadoop.jobtracker.port=9001
hadoop.namenode=localhost
hadoop.namenode.port=9000

# Hadoop options
hadoop.dfs.replication=1
hadoop.version=0.20.2

# Hadoop dirs
hadoop.dir.name=/tmp/dfs/name/
hadoop.dir.data=/tmp/dfs/data/
hadoop.dir.tmp=/tmp/
#hadoop.dir.secnn=/tmp/hadoop-secondary/
hadoop.dir.log=/var/log/hadoop/
hadoop.dir.install=/home/michel/hadoop-0.20.2/
hadoop.dir.format.script=/home/michel/workspace-eclipse/albonico/HadoopTest/dfs-format.sh

# Java options
hadoop.java.options=-Xmx256m -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=80000
mapred.child.java.opts=-Xmx256m

 * 
 * 
 */