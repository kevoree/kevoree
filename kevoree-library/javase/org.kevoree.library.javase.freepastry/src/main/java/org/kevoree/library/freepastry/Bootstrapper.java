/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.freepastry;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;

/**
 *
 * @author sunye
 */
@Library(name = "Freepastry")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "port", optional = false, defaultValue= "1200"),
    @DictionaryAttribute(name = "address")
})
public class Bootstrapper extends AbstractComponentType {

    private static final int PORT = 1200;
    private static InetAddress HOST;
    protected PastryPeer peer;

    public Bootstrapper() {
        try {
            HOST = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
        }
    }

    @Start
    public void start() throws Exception {
        InetSocketAddress address = new InetSocketAddress(HOST, PORT);
        peer = new PastryPeer(address);
        
        peer.bootsrap();
        //peer.createPast();
       

        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        kengine.addVariable("nodeName", getNodeName());
        kengine.addVariable("name", getName());
        kengine.addVariable("host", HOST.getHostName());
        kengine.append("updateDictionary {name}@{nodeName} { address='{host}' }");
        kengine.interpretDeploy();
        
    }

    @Stop
    public void stop() {
    }

    @Update
    public void update() {
    }
}
