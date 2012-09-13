package org.kevoree.library.javase.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import org.kevoree.annotation.*;
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
        Config configApp = new Config();
        configApp.getGroupConfig().setName("KChannel_HazelQueue_"+getName());
        configApp.setInstanceName("KChannel_HazelQueue_"+getNodeName());
        hazelInstance = Hazelcast.newHazelcastInstance(configApp);
        dqueue = hazelInstance.getQueue(getName());
        updateHazel();
    }

    @Stop
    public void stopHazel() {
        try {
            if (pollThread != null) {
                pollThread.interrupt();//CLEAR IN FLIGHT MESSAGE
            }
        } catch (Exception ignore) {}
        pollThread = null;
        dqueue = null;
        hazelInstance.shutdown();
        hazelInstance = null;
    }

    @Update
    public void updateHazel() {
        if (getBindedPorts().size() > 0) {
            pollThread = new Thread(this);
            pollThread.start();
        } else {
            try {
                if (pollThread != null) {
                    pollThread.interrupt();//CLEAR IN FLIGHT MESSAGE
                }
            } catch (Exception ignore) {
            }
        }
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
            for (; ; ) {
                Object o = dqueue.take();
                Message msg = null;
                if (o instanceof Message) {
                    msg = (Message) o;
                } else {
                    msg = new Message();
                    msg.setContent(o);
                }
                for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
                    forward(p, msg);
                }
            }
        } catch (Exception e) {
            logger.error("Bad message rec", e);
        }

    }
}
