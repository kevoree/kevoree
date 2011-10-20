/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.hadoop;

import java.io.File;
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

        
        // JobHistory log file
        //new File("/private/var/log/hadoop/history").mkdirs();


        Configuration configuration = this.getConfiguration();
        InetAddress i = InetAddress.getLocalHost();
        String hostName = i.getHostName();
        configuration.set("hadoop.jobtracker", hostName);
        String jthost = hostName + ":" + configuration.get("hadoop.jobtracker.port");
        configuration.set("mapred.job.tracker", jthost);

        
        String nnhost = "hdfs://" + hostName + ":" + configuration.get("hadoop.namenode.port");
        configuration.set("fs.default.name", nnhost);
        
        //configuration.set("mapred.job.tracker.info.bindAddress",null);
        //configuration.set("mapred.job.tracker.info.port",null);
        

        
        configuration.set("mapred.job.tracker.http.address", "http://" + hostName + ":54314");


        
        


        configuration.set("hadoop.namenode", hostName);
        configuration.set("dfs.namenode.http-address", hostName);

        configuration.set("dfs.info.bindAddress", i.getHostName());
        //configuration.set("dfs.info.port", configuration.get("hadoop.namenode.port"));

        
        configuration.set("dfs.http.address", "http://" + hostName + ":" + configuration.get("dfs.info.port"));
        
        
        
        
        HadoopConfiguration cfg = new HadoopConfiguration(configuration);
        cfg.writeMapredSite();
        
        new Thread() {
            public void run() {
                runTracker();
            }
        }.start();

    }

    @Stop
    public void stop() throws IOException {
        if (tracker != null) {
            tracker.stopTracker();
        }

    }


    @Update
    public void update() {
        
    }

    public void runTracker() {
        try {
            System.out.println("======> Starting JobTracker");
            
            tracker = JobTracker.startTracker(new JobConf(getConfiguration()));
            tracker.offerService();
            
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    public static void main(String[] args) {
        try {
            HadoopJobTracker tracker = new HadoopJobTracker();
            tracker.start();
            //Thread.sleep(10000);
            //tracker.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
