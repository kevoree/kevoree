/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper.rest;

import java.util.List;
import java.util.UUID;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.gossiper.GossiperChannel;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;
import org.kevoree.library.gossiper.version.GossiperMessages.VersionedModel;

/**
 *
 * @author ffouquet
 */
public class RestGossiperChannel extends AbstractChannelFragment implements GossiperChannel {

    @Start
    public void startGossiperChannel() {
    }

    @Stop
    public void stopGossiperChannel() {
    }

    @Update
    public void updateGossiperChannel() {
    }

    @Override
    public Object dispatch(Message msg) {
        //CREATE NEW MESSAGE

        //SYNCHRONOUS NON IMPLEMENTED
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new NoopChannelFragmentSender();
    }

    @Override
    public void localDelivery(Object o) {
        Message localMsg = new Message();
        localMsg.setContent(o);
        //TODO IMPROVE
        
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, localMsg);
        }
    }

    @Override
    public void notifyPeer(String nodeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notifyPeers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VersionedModel getUUIDDataFromPeer(String nodeName, UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VectorClock getUUIDVectorClockFromPeer(String nodeName, UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<UUID> getMsgUUIDSFromPeer(String nodeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
