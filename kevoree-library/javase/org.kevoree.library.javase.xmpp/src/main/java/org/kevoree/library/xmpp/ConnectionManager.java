package org.kevoree.library.xmpp;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager implements ConnectionListener {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    public XMPPConnection connection;
    private List<String> alreadyView = new ArrayList<String>();
    private Map<String, Chat> activeChats = new HashMap<String, Chat>();

    public void login(String userName, String password) {
        try {

            ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "Work");
            connection = new XMPPConnection(config);
            connection.connect();
            connection.login(userName, password);
            connection.addConnectionListener(this);

        } catch (XMPPException e) {
//            e.printStackTrace();
			logger.error("Log in failed", e);
        }
    }

    public void sendMessage(String message, String to, MessageListener messageList) {
        try {

            if (!activeChats.containsKey(to)) {
                activeChats.put(to, connection.getChatManager().createChat(to, messageList));
            }
            activeChats.get(to).sendMessage(message);

        } catch (XMPPException e) {
//            e.printStackTrace();
			logger.error("Send message failed", e);
        }
    }

    public void disconnect() {
        connection.disconnect();
    }

    public List<String> getAlreadyView() {
        return alreadyView;
    }

    public void setDefaultResponseStrategy(final MessageListener messageList) {

        PacketTypeFilter filter = new PacketTypeFilter(Message.class);
        PacketListener myListener = new PacketListener() {

            public void processPacket(Packet packet) {
                String name = packet.getFrom().substring(0, packet.getFrom().indexOf("/"));
                if (!alreadyView.contains(name)) {
                    alreadyView.add(name);
                    sendMessage("Entimid IRISA/INRIA Bonjour ;-) !", name, messageList);
                } else {
                    logger.debug("Already ok ");
                }
            }
        };
        connection.addPacketListener(myListener, filter);
    }

    public void connectionClosed() {
        logger.debug("XMPP Connection Closed");
    }

    @Override
    public void connectionClosedOnError(Exception excptn) {
//        excptn.printStackTrace();
		logger.error("Connection closed on error", excptn);
    }

    @Override
    public void reconnectingIn(int i) {
        logger.debug("XMPP Reconnecting(" + i + ")...");
    }

    @Override
    public void reconnectionSuccessful() {
        logger.debug("XMPP Re-Connection Ok");
    }

    @Override
    public void reconnectionFailed(Exception excptn) {
//        excptn.printStackTrace();
		logger.error("Reconnection failed", excptn);

    }
}
