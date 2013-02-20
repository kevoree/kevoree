package org.kevoree.library.javase.autoBasicGossiper;

import org.kevoree.annotation.GroupType;
import org.kevoree.library.javase.basicGossiper.group.BasicGossiperGroup;
import org.kevoree.library.javase.jmdns.JmDNSListener;
import org.kevoree.library.javase.jmdns.JmDnsComponent;

import java.io.IOException;
import java.net.InetAddress;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/13
 * Time: 11:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@GroupType
public class AutoBasicGossiperGroup extends BasicGossiperGroup implements JmDNSListener {

    private JmDnsComponent jmDnsComponent;

    @Override
    public void startGossiperGroup() throws IOException {
        super.startGossiperGroup();
        jmDnsComponent = new JmDnsComponent(this, this, Integer.parseInt(this.getDictionary().get("port").toString()), InetAddress.getByName(this.getDictionary().get("ip").toString()), true);
        jmDnsComponent.start();
    }

    @Override
    public void stopGossiperGroup() {
        super.stopGossiperGroup();
        jmDnsComponent.stop();
    }

    @Override
    public void notifyNewSubNode(String remoteNodeName) {
        logger.info("new remote node discovered, try to pull the model from this node");
        super.actor.doGossip(remoteNodeName);
    }
}
