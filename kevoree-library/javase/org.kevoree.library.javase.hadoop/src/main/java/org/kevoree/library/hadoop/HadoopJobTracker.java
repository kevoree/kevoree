/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobTracker;

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
public class HadoopJobTracker extends AbstractComponentType implements Runnable {

    private static final Logger LOG = Logger.getLogger(HadoopJobTracker.class.getName());
    private JobConf job;
    private JobTracker tracker;
    private JobConf configuration;

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HadoopJobTracker(JobConf conf) {
        this.configuration = conf;
    }

    @Start
    public void start() throws RemoteException, IOException,
            InterruptedException {

        tracker = JobTracker.startTracker(configuration);
        tracker.offerService();

    }

    @Stop
    public void stop() throws IOException {
        
        tracker.stopTracker();

    }


}
