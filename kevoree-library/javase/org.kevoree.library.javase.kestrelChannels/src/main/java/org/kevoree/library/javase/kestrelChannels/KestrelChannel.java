package org.kevoree.library.javase.kestrelChannels;

import com.twitter.util.*;
import com.twitter.util.Duration;
import com.twitter.util.StorageUnit;
import com.twitter.util.Time;
import com.twitter.util.Timer;
import net.lag.kestrel.PersistentQueue;
import net.lag.kestrel.config.QueueConfig;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lag.kestrel.Kestrel;
import net.lag.kestrel.config.QueueConfig;
import net.lag.kestrel.config.QueueBuilder;
import net.lag.kestrel.config.Protocol;

import java.util.concurrent.TimeUnit;

import scala.Some;
import scala.collection.immutable.List;
import scala.Option;


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 29/11/11
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */

public class KestrelChannel  extends AbstractChannelFragment {

    private KestrelServer server;


    @Start
    public void startChannel() {


    }


    @Stop
    public void stopChannel() {


    }
    @Update
    public void updateChannel() {

    }
    @Override
    public Object dispatch(Message message) {

        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, message);
        }
        /*
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!message.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, message);
            }
        } */
        return null;
    }


    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
