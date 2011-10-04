package org.kevoree.library.hadoop;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.PortType;
import org.kevoree.annotation.ProvidedPort;
import org.kevoree.annotation.Provides;
import org.kevoree.annotation.RequiredPort;
import org.kevoree.annotation.Requires;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

/**
 * 
 * @author sunye Use this wrapper to start and stop Hadoop's master node. This
 *         class should not depend on the TestCase.
 */
@Library(name = "Hadoop")
@ComponentType
public class HadoopTaskTracker extends HadoopComponent {

    private static final Logger LOG = Logger.getLogger(HadoopTaskTracker.class.getName());
    private TaskTracker taskTracker;
    

    public HadoopTaskTracker() {
        super();
    }

    @Start
    /**
     * 
     * @TODO: Retrieve and set job tracker address
     * 
     */
    protected void start() throws IOException, InterruptedException {
        LOG.info("Starting TaskTracker!");
        
        Configuration configuration = this.getConfiguration();
        configuration.set("hadoop.jobtracker", null);


        JobConf job = new JobConf(configuration);
        taskTracker = new TaskTracker(job);
        taskTracker.run();
    }

    @Stop
    protected void stop() throws IOException {

        LOG.info("Stopping TaskTracker...");
        taskTracker.shutdown();
    }
}
