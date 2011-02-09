package org.kevoree.library.xmpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class ConnectionManager implements ConnectionListener {

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
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, String to, MessageListener messageList) {
        try {

            if (!activeChats.containsKey(to)) {
                activeChats.put(to, connection.getChatManager().createChat(to, messageList));
            }
            activeChats.get(to).sendMessage(message);

        } catch (XMPPException e) {
            e.printStackTrace();
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
                    System.out.println("Already ok ");
                }
            }
        };
        connection.addPacketListener(myListener, filter);
    }

    public void connectionClosed() {
        System.out.print("XMPP Connection Closed");
    }

    @Override
    public void connectionClosedOnError(Exception excptn) {
        excptn.printStackTrace();
    }

    @Override
    public void reconnectingIn(int i) {
        System.out.print("XMPP Reconnecting(" + i + ")...");
    }

    @Override
    public void reconnectionSuccessful() {
        System.out.print("XMPP Re-Connection Ok");
    }

    @Override
    public void reconnectionFailed(Exception excptn) {
        excptn.printStackTrace();
    }
}
