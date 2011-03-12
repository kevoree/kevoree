/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;
import org.kevoree.library.gossiper.version.GossiperMessages.VersionedModel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ffouquet
 */
//@GroupType
public abstract class GossipGroup extends AbstractGroupType implements GossiperGroup<VersionedModel> {

    public final AtomicReference<Date> lastCheckedTimeStamp = new AtomicReference<Date>(new Date(0l));
    private VectorClockActor currentClock = null;
    private GossiperActor gossipActor = null;

    @Override
    public VectorClock update(VersionedModel versioned) {
        if (versioned != null) {
            InputStream stream = new ByteArrayInputStream(versioned.getModel().getBytes());
            if (this.getModelService().updateModel(KevoreeXmiHelper.loadStream(stream))) {
                lastCheckedTimeStamp.set(this.getModelService().getLastModification());
                VectorClock ncur = currentClock.merge(versioned.getVector());
                return ncur;
            }
        }
        return currentClock.get();
    }

    @Override
    public VectorClock resolve(VersionedModel versioned) {
        if (versioned != null) {
            Date localDate = new Date(currentClock.get().getTimestamp());
            Date remoteDate = new Date(versioned.getVector().getTimestamp());
            //TODO TO IMPROVE
            if (localDate.before(remoteDate)) {
                InputStream stream = new ByteArrayInputStream(versioned.getModel().getBytes());
                this.getModelService().updateModel(KevoreeXmiHelper.loadStream(stream));
                lastCheckedTimeStamp.set(this.getModelService().getLastModification());
            }
            return currentClock.merge(versioned.getVector());
        }
        return currentClock.get();
    }

    @Override
    public void setCurrentClock(VectorClock clock) {
        currentClock.swap(clock);
    }

    @Override
    public VectorClock currentClock() {
        return currentClock.get();
    }

    @Override
    public String selectPeer() {
        ContainerRoot model = this.getModelService().getLastModel();
        /* Search self group */
        Group selfGroup = null;
        for (Group g : model.getGroups()) {
            if (g.getName().equals(this.getName())) {
                selfGroup = g;
            }
        }
        List<ContainerNode> groupNode = new ArrayList<ContainerNode>();
        if (selfGroup != null && selfGroup.getSubNodes() != null) {
            for (ContainerNode sub : selfGroup.getSubNodes()) {
                if (!sub.getName().equals(this.getNodeName())) {
                    groupNode.add(sub);
                }
            }
        }
        int nodesSize = groupNode.size();
        Random diceRoller = new SecureRandom();
        int peerIndex = diceRoller.nextInt(nodesSize);
        return groupNode.get(peerIndex).getName();
    }

    public VectorClock incrementedVectorClock() {
        if (this.getModelService().getLastModification().after(lastCheckedTimeStamp.get())) {
            lastCheckedTimeStamp.set(this.getModelService().getLastModification());
            return currentClock.incAndGet();
        }
        return currentClock.get();
    }

    @Override
    public void triggerModelUpdate() {
        System.out.println("Update trigger");
    }

    @Start
    public void startMyGroup() {
        System.out.println("StartGroup " + this.getClass().getName());
        currentClock = new VectorClockActor(this.getNodeName());
        gossipActor = new GossiperActor(Integer.parseInt(this.getDictionary().get("interval").toString()), this);
    }

    @Stop
    public void stopMyGroup() {
        System.out.println("StopGroup " + this.getClass().getName());
        gossipActor.stop();
        currentClock.stop();
    }

    @Update
    public void updateMyGroup() {
        System.out.println("UpdateGroup " + this.getClass().getName());
    }


}
