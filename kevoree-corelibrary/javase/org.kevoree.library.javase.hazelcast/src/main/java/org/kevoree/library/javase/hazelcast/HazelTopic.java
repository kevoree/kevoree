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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/09/12
 * Time: 11:29
 */

@Library(name = "JavaSE")
@ChannelTypeFragment
public class HazelTopic extends AbstractChannelFragment implements MessageListener<Object> {

    HazelcastInstance hazelInstance = null;
    ITopic<Object> topic = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    ExecutorService messageExecutor = null;

    @Start
    public void startHazel(){
        messageExecutor = Executors.newSingleThreadExecutor();
        Config configApp = new Config();
        configApp.getGroupConfig().setName("KChannel_HazelTopic_"+getName());
        configApp.setInstanceName("KChannel_HazelTopic_"+getNodeName());
        hazelInstance = Hazelcast.newHazelcastInstance(configApp);
        topic = hazelInstance.getTopic(getName());
        topic.addMessageListener(this);
    }

    @Stop
    public void stopHazel(){
        messageExecutor.shutdownNow();//CLEAR IN FLIGHT MESSAGE
        topic = null;
        hazelInstance.shutdown();
        hazelInstance = null;
    }

    @Override
    public Object dispatch(Message message) {
        topic.publish(message);
        return null; //NO RESULT
    }

    @Override
    public ChannelFragmentSender createSender(String s, String s1) {
        return new NoopChannelFragmentSender();
    }

    @Override
    public void onMessage(final com.hazelcast.core.Message message) {
        messageExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
                    try {
                        forward(p, (Message) message.getMessageObject());
                    } catch(Exception e){
                        logger.error("Can't cast message");
                    }
                }
            }
        });
    }
}
