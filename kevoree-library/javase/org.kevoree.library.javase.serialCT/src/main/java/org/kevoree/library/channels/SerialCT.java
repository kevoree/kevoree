package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;


/**
 * @author ffouquet
 */
@Library(name = "KevoreeArduinoJava")
@DictionaryType({
        @DictionaryAttribute(name = "PORT", optional = false, defaultValue = "/dev/tty0")
})
@ChannelTypeFragment
public class SerialCT extends AbstractChannelFragment {

    TwoWayActors twA = null;

    @Start
    public void startRxTxChannel() {
        twA = new TwoWayActors((String) this.getDictionary().get("PORT"), this);
    }

    @Update
    public void updateRxTxChannel() {
        stopRxTxChannel();
        startRxTxChannel();
    }

    @Stop
    public void stopRxTxChannel() {
        twA.killConnection();
        twA = null;
    }


    @Override
    public Object dispatch(Message msg) {

        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (msg.getPassedNodes().isEmpty()) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                if (twA != null) {
                    twA.sendMsg(message.getContent().toString());
                }
                return null;
            }
        };
    }



}
