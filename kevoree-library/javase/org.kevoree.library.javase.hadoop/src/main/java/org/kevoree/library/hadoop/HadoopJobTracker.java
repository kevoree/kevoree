/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobTracker;

import org.kevoree.annotation.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
public class HadoopJobTracker extends HadoopComponent {

    private static final Logger LOG = LoggerFactory.getLogger(HadoopJobTracker.class.getName());
    private JobTracker tracker;

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

        new Thread(new Runnable() {

            public void run() {
                try {
                    
                    tracker.offerService();
                }
                catch (InterruptedException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
                catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }).start();

    }

    @Stop
    public void stop() throws IOException {

        tracker.stopTracker();

    }
}
