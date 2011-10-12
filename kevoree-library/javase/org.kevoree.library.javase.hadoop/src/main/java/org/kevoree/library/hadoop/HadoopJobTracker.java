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
        String hostName = i.getHostName();
        configuration.set("hadoop.jobtracker", hostName);
        String jthost = hostName + ":" + configuration.get("hadoop.jobtracker.port");
        configuration.set("mapred.job.tracker", jthost);

        //configuration.set("mapred.job.tracker.info.bindAddress",null);
        //configuration.set("mapred.job.tracker.info.port",null);
        configuration.set("mapred.job.tracker.http.address", "http://" + hostName + ":54314");

        new Thread() {
            public void run() {
                runTracker();
            }
        }.start();

    }

    @Stop
    public void stop() throws IOException {

        tracker.stopTracker();

    }

    public static void main(String[] args) {
        try {
            InetAddress i = InetAddress.getLocalHost();
            System.out.println(i.getHostName());
        }
        catch (Exception e) {
        }
    }

    public void runTracker() {
        try {
            tracker = JobTracker.startTracker(new JobConf(getConfiguration()));
            //tracker.offerService();
        }
        catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
