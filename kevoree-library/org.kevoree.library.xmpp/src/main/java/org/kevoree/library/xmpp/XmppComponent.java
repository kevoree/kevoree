/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.xmpp;

import org.jivesoftware.smack.packet.Presence;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;

/**
 *
 * @author gnain
 */
@Provides({
    @ProvidedPort(name = "send", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "messageReceived", type = PortType.MESSAGE)
})
@Library(name = "Kevoree-Android-JavaSE")
@DictionaryType({@DictionaryAttribute(name = "login"),@DictionaryAttribute(name = "password")})
@ComponentType
public class XmppComponent extends AbstractComponentType {

    private ConnectionManager client;
    private LocalMessageListener defaultListener;

    @Port(name = "send")
    public void sendMessage(Object message) {
        System.out.println("XMPP Send msg =>"+message.toString());
        for (String dest : client.getAlreadyView()) {
            System.out.println("To =>" + dest);
            client.sendMessage(message.toString(), dest, defaultListener);
        }
    }

    public void messageReceived(String message) {
        getPortByName("messageReceived", MessagePort.class).process(message);
    }

    @Start
    public void start() {

        client = new ConnectionManager();
        client.login("entimid@gmail.com", "entimidpass");
        defaultListener = new LocalMessageListener(this);
        client.setDefaultResponseStrategy(defaultListener);
        client.connection.sendPacket(new Presence(Presence.Type.available));

    }

    @Stop
    public void stop() {
        client.connection.sendPacket(new Presence(Presence.Type.unavailable));
        client.connection.disconnect();
    }

}
