package org.kevoree.library.xmpp;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class LocalMessageListener implements MessageListener {

    private XmppComponent mainComp;

    public LocalMessageListener(XmppComponent _mainComp) {
        mainComp = _mainComp;
    }

    public void processMessage(Chat arg0, Message arg1) {
        System.out.println("MsgRec=>"+arg1.getBody());
        mainComp.messageReceived(arg1.getBody());
    }
}
