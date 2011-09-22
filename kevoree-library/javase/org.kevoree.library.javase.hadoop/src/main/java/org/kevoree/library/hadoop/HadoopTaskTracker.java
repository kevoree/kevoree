package org.kevoree.library.hadoop;

import java.io.IOException;
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
@Requires({
    @RequiredPort(name = "records", type = PortType.MESSAGE),
    @RequiredPort(name = "calibrations", type = PortType.MESSAGE)
})
@Provides({
    @ProvidedPort(name = "log", type = PortType.MESSAGE)
})
public class HadoopTaskTracker extends AbstractComponentType {

    private static final Logger LOG = Logger.getLogger(HadoopTaskTracker.class.getName());
    private TaskTracker taskTracker;
    private final Configuration configuration;

    public HadoopTaskTracker(Configuration configuration) {


        this.configuration = configuration;

    }

    @Start
    protected void start() throws IOException, InterruptedException {
        // TaskTrackers
        LOG.info("Starting TaskTracker!");

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
