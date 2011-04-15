package org.kevoree.library.com.skype;

import com.skype.ChatMessage;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import com.skype.Chat;
import com.skype.ChatMessageListener;
import com.skype.Skype;
import com.skype.SkypeException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.framework.MessagePort;

/**
 * This Kevoree component encapsulates Skype. It allows sending (chat and SMS) messages
 * using the Skype account currently running on the local host. It also receives the (chat) messages
 * sent to this account and forward them.
 * @author Olivier Barais and Brice Morin
 * @copyright INRIA and SINTEF IKT
 */
@Provides({
    @ProvidedPort(name = "sendSMS", type = PortType.MESSAGE),
    @ProvidedPort(name = "sendChatMessage", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "forwardIncomingChatMessage", type = PortType.MESSAGE)
})
@DictionaryType({
    @DictionaryAttribute(name = "tel")})
@Library(name = "Kevoree::Chat")
@ComponentType
public class SkypeComponent extends AbstractComponentType {

    private SkypeChatListener chatListener;

    public SkypeComponent(){
        try {
            chatListener = new SkypeChatListener(this);
            Skype.addChatMessageListener(chatListener);
        } catch (SkypeException ex) {
            Logger.getLogger(SkypeComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Start
    public void start() {
        tel = (String) this.getDictionary().get("tel").toString();
    }

    @Stop
    public void stop() {
        Skype.removeChatMessageListener(chatListener);
    }

    @Update
    public void update() {
        tel = (String) this.getDictionary().get("tel").toString();
    }
    String tel;

    @Port(name = "sendSMS")
    public void sendSMS(Object message) {
        try {
            Skype.sendSMS(tel, message.toString());
        } catch (SkypeException e) {
            Logger.getLogger(SkypeComponent.class.getName()).log(Level.SEVERE, "Error while sendind SMS. Please verify that Skype is running on the computer.", e);
        }
    }

    @Port(name = "sendChatMessage")
    public void sendChatMessage(Object message) {
        try {
            Chat c = Skype.chat(tel);
            if (c != null) {
                c.send(message.toString());
            }
        } catch (SkypeException e) {
            Logger.getLogger(SkypeComponent.class.getName()).log(Level.SEVERE, "Error while sendind chat message. Please verify that Skype is running on the computer.", e);
        }
    }

    private void forward(String msg) {
        if (this.isPortBinded("forwardIncomingChatMessage")) {
            this.getPortByName("forwardIncomingChatMessage", MessagePort.class).process(msg);
        }
    }

    private class SkypeChatListener implements ChatMessageListener {

        SkypeComponent skype;

        public SkypeChatListener(SkypeComponent skype) {
            this.skype = skype;
        }

        @Override
        public void chatMessageReceived(ChatMessage cm) throws SkypeException {
            Logger.getLogger(SkypeChatListener.class.getName()).log(Level.INFO, "SkypeChatListener has received: "+cm.getContent()+" from "+cm.getSenderDisplayName());
            skype.forward(cm.getContent());
        }

        @Override
        public void chatMessageSent(ChatMessage cm) throws SkypeException {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
