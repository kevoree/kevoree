package org.kevoree.library.javase.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/09/12
 * Time: 11:41
 */
@Library(name = "JavaSE")
@ChannelTypeFragment
public class HazelQueue extends AbstractChannelFragment implements Runnable {

    HazelcastInstance hazelInstance = null;
    BlockingQueue<Object> dqueue = null;
    Thread pollThread = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void startHazel() {
        pollThread = new Thread(this);
        pollThread.start();
        Config configApp = new Config();
        configApp.getGroupConfig().setName("KevoreeChannel_" + getName());
        configApp.setInstanceName("KevoreeNode_" + getNodeName());
        hazelInstance = Hazelcast.newHazelcastInstance(configApp);
        Cluster cluster = hazelInstance.getCluster();
        dqueue = hazelInstance.getQueue(getName());

    }

    @Stop
    public void stopHazel() {
        try {
            pollThread.interrupt();//CLEAR IN FLIGHT MESSAGE
        } catch (Exception ignore) {

        }
        pollThread = null;
        dqueue = null;
        hazelInstance.shutdown();
        hazelInstance = null;
    }

    @Override
    public Object dispatch(org.kevoree.framework.message.Message message) {
        try {
            dqueue.put(message);
        } catch (InterruptedException e) {
            logger.error("Message lost on channel {}", getName() + getNodeName(), e);
        }
        return null; //NO RESULT
    }

    @Override
    public ChannelFragmentSender createSender(String s, String s1) {
        return new NoopChannelFragmentSender();
    }

    @Override
    public void run() {
        try {
            Message msg = (Message) dqueue.poll();
            for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
                forward(p, msg);
            }
        } catch (Exception e) {
            logger.error("Bad message rec", e);
        }

    }
}
