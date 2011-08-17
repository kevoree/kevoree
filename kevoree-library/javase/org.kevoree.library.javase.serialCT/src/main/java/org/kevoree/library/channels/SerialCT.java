package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.extra.osgi.rxtx.KevoreeSharedCom;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
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

    KContentListener cl = new KContentListener(this);

    @Start
    public void startRxTxChannel() {
        KevoreeSharedCom.addObserver((String) this.getDictionary().get("PORT"), cl);
    }

    @Update
    public void updateRxTxChannel() {
        stopRxTxChannel();
        startRxTxChannel();
    }

    @Stop
    public void stopRxTxChannel() {
        KevoreeSharedCom.removeObserver((String) this.getDictionary().get("PORT"), cl);
    }

    @Override
    public Object dispatch(Message msg) {

        System.out.println("Hu");

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
    public ChannelFragmentSender createSender(final String remoteNodeName, final String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                String messageTosSend = getName() + ":" + getNodeName() + "[" + message.getContent().toString() + "]";
                KevoreeSharedCom.send((String) getDictionary().get("PORT"),messageTosSend);
                return null;
            }
        };
    }


}
