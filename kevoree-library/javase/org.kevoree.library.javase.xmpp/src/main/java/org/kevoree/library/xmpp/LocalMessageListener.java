package org.kevoree.library.xmpp;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalMessageListener implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(LocalMessageListener.class);

    private XmppComponent mainComp;

    public LocalMessageListener(XmppComponent _mainComp) {
        mainComp = _mainComp;
    }

    public void processMessage(Chat arg0, Message arg1) {
        logger.debug("MsgRec=>" + arg1.getBody());
        mainComp.messageReceived(arg1.getBody());
    }
}
