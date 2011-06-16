package org.kevoree.library.channels;

import gnu.io.CommPort;
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

    @Start
    public void startRxTxChannel() {
         ComPortHandler.addListener((String) this.getDictionary().get("PORT"),this);
    }

    @Update
    public void updateRxTxChannel() {
        stopRxTxChannel();
        startRxTxChannel();
    }

    @Stop
    public void stopRxTxChannel() {
        ComPortHandler.removeListener((String) this.getDictionary().get("PORT"),this);
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

                 ComPortHandler.getPortByName((String) getDictionary().get("PORT")).sendMessage(getName(),getNodeName(),message.getContent().toString());

               // if (twA != null) {
               //     twA.sendMsg(message.getContent().toString());
               // }
                return null;
            }
        };
    }



}
