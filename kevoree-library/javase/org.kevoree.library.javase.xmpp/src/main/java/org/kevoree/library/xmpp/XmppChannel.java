package org.kevoree.library.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Library(name = "JavaSE")
@ChannelTypeFragment
@ThirdParties({
        @ThirdParty(name = "org.kevoree.extra.marshalling", url = "mvn:org.kevoree.extra/org.kevoree.extra.marshalling")
})
@DictionaryType({
        @DictionaryAttribute(name = "userName"),
        @DictionaryAttribute(name = "password")
})
public class XmppChannel extends AbstractChannelFragment implements ConnectionListener {
	private static final Logger logger = LoggerFactory.getLogger(XmppChannel.class);

    ConnectionConfiguration config = null;
    XMPPConnection connection = null;


    @Start
    public void startChannel() {
        try {
            config = new ConnectionConfiguration("talk.google.com", 5222, "Work");
            connection = new XMPPConnection(config);
            connection.connect();
            connection.login(this.getDictionary().get("userName").toString(), this.getDictionary().get("password").toString());
            connection.addConnectionListener(this);
            connection.sendPacket(new Presence(Presence.Type.available));

            PacketTypeFilter filter = new PacketTypeFilter(org.jivesoftware.smack.packet.Message.class);
            PacketListener myListener = new PacketListener() {

                public void processPacket(Packet packet) {
                    String name = packet.getFrom().substring(0, packet.getFrom().indexOf("/"));

                    logger.debug("rec" + name + "=" + packet.getPropertyNames());
                }
            };
            connection.addPacketListener(myListener, filter);


        } catch (Exception e) {
//            e.printStackTrace();
			logger.error("", e);
        }


    }

    @Stop
    public void stopChannel() {
        connection.disconnect();
    }

    @Override
    public Object dispatch(Message message) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChannelFragmentSender createSender(String s, String s1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void connectionClosed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reconnectingIn(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reconnectionSuccessful() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reconnectionFailed(Exception e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
