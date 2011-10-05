package org.kevoree.library.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author sunye Use this wrapper to start and stop Hadoop's master node. This
 *         class should not depend on the TestCase.
 */
@Library(name = "Hadoop")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "jobTrackerName", optional = false)
})
public class HadoopTaskTracker extends HadoopComponent {

    private static final Logger LOG = LoggerFactory.getLogger(HadoopTaskTracker.class.getName());
    private String jobTrackerName = "";
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
    public void start() throws IOException, InterruptedException {
        LOG.info("Starting TaskTracker!");
        jobTrackerName = (String) this.getDictionary().get("jobTrackerName");

        Map<String, String> values = new HashMap<String, String>();
        String jobTrackerNodeName = "";


        /*
         * @FIXME : use while instead of foreach
         */
        for (ContainerNode each : this.getModelService().getLastModel().getNodes()) {
            for (ComponentInstance ci : each.getComponents()) {
                if (jobTrackerName.equals(ci.getName())) {
                    jobTrackerNodeName = each.getName();
                    break;
                }
            }
        }

        // retrieve NameNode IP address
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(),
                jobTrackerNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());


        Configuration configuration = this.getConfiguration();
        configuration.set("hadoop.jobtracker", ip);


        JobConf job = new JobConf(configuration);
        taskTracker = new TaskTracker(job);
        new Thread() {

            @Override
            public void run() {
                taskTracker.run();
            }
        }.start();
    }

    @Stop
    public void stop() throws IOException, InterruptedException {

        LOG.info("Stopping TaskTracker...");
        taskTracker.shutdown();
    }
}
