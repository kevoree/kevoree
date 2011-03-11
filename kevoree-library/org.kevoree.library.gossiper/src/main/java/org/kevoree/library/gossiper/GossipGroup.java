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
import org.kevoree.library.gossiper.version.Occured;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry;
import org.kevoree.library.gossiper.version.VersionUtils;

/**
 * @author ffouquet
 */
//@GroupType
public abstract class GossipGroup extends AbstractGroupType implements Runnable {

    private AtomicBoolean running = new AtomicBoolean(false);
    public AtomicReference<VectorClock> clockRef = new AtomicReference<VectorClock>(VectorClock.newBuilder().setTimestamp(System.currentTimeMillis()).build());
    public final AtomicReference<Date> lastCheckedTimeStamp = new AtomicReference<Date>(new Date(0l));

    public void incrementedVectorClock() {
        if (this.getModelService().getLastModification().after(lastCheckedTimeStamp.get())) {
            //Increment & Replace local vector clock
            VectorClock currentClock = clockRef.get();
            Long currentTimeStamp = System.currentTimeMillis();
            List<ClockEntry> incrementedEntries = new ArrayList<ClockEntry>();
            boolean selfFound = false;
            for (ClockEntry clock : currentClock.getEntiesList()) {
                if (clock.getNodeID().equals(this.getNodeName())) {
                    selfFound = true;
                    incrementedEntries.add(ClockEntry.newBuilder(clock).setVersion(clock.getVersion() + 1).setTimestamp(currentTimeStamp).build());
                } else {
                    incrementedEntries.add(clock);
                }
            }
            if(!selfFound){
                incrementedEntries.add(ClockEntry.newBuilder().setNodeID(this.getNodeName()).setVersion(1).setTimestamp(currentTimeStamp).build());
            }
            clockRef.set(VectorClock.newBuilder().addAllEnties(incrementedEntries).setTimestamp(currentTimeStamp).build());
            lastCheckedTimeStamp.set(this.getModelService().getLastModification());
        }
    }

    @Override
    public void triggerModelUpdate() {
        System.out.println("Update trigger");
    }

    @Start
    public void startMyGroup() {

        System.out.println("StartGroup " + this.getClass().getName());
        running.set(true);
        Thread myThread = new Thread(this);
        myThread.setPriority(Thread.MIN_PRIORITY);
        myThread.start();

        System.out.println("last date " + this.getModelService().getLastModification());



    }

    @Stop
    public void stopMyGroup() {
        System.out.println("StopGroup " + this.getClass().getName());
        running.set(false);
    }

    @Update
    public void updateMyGroup() {
        System.out.println("UpdateGroup " + this.getClass().getName());
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                //     Thread.sleep(Integer.parseInt((String) this.getDictionary().get("interval")));
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ContainerNode node = selectPeer();
            //adminClient.setAdminClientCluster(metadataStore.getCluster());
            VectorClock clock = getVectorFromPeer(node);

            System.out.println("-------------");
			System.out.print("local clock: ");
            print(this.clockRef.get());
			System.out.print("remote clock: ");
            print(clock);
            System.out.println("-------------");


            Occured result = VersionUtils.compare(this.clockRef.get(), clock);
            if (result == Occured.AFTER) {

                System.out.println("after : We do nothing because AFTER potentially means EQUALS");
                // we do nothing because VectorClocks are equals (and so models are equals)
                // or local VectorClock is more recent (and so local model is more recent)
            } else if (result == Occured.BEFORE) {
                // we update our local model because the selected peer has a more recent VectorClock (and so a more recent model)
                VersionedModel versioned = getVersionnedModelToPeer(node);
                updateClock(versioned);
            } else if (result == Occured.CONCURRENTLY) {
                System.out.println("CONCURRENTLY");
                VersionedModel versioned = getVersionnedModelToPeer(node);
                mergeReconcile(versioned);
                // Other possibility not implemented :
                // It is not possible to find the most recent VectorClock (and so the more recent model)
                // That's why we choose to keep all local information about local node, component, ...
                // We also choose to keep all remote information describe into the model of the selected peer.
            }

        }
    }

    private void print(VectorClock clock) {
        System.out.println();
        for (ClockEntry entry : clock.getEntiesList()) {
            System.out.print(entry.getNodeID() + ":" + entry.getVersion());
            System.out.print(",");
        }
        System.out.println();
    }
    
    private synchronized void mergeReconcile(VersionedModel versioned) {
        if (versioned != null) {
            System.out.println("reconcile");
			
			System.out.print("local clock: ");
            print(this.clockRef.get());
			System.out.print("remote clock: ");
            print(versioned.getVector());
            
            Date localDate = new Date(this.clockRef.get().getTimestamp());
            Date remoteDate = new Date(versioned.getVector().getTimestamp());
            //TODO TO IMPROVE
            if(localDate.before(remoteDate)){
                InputStream stream = new ByteArrayInputStream(versioned.getModel().getBytes());
                this.getModelService().updateModel(KevoreeXmiHelper.loadStream(stream));
                lastCheckedTimeStamp.set(this.getModelService().getLastModification());
            }
            VectorClock clock = VersionUtils.merge(this.clockRef.get(), versioned.getVector());
            this.clockRef.set(clock);
        }
    }

    private synchronized void updateClock(VersionedModel versioned) {
        if (versioned != null) {
			System.out.print("local clock: ");
            print(clockRef.get());
			System.out.print("remote clock: ");
            print(versioned.getVector());
            InputStream stream = new ByteArrayInputStream(versioned.getModel().getBytes());

            if (this.getModelService().updateModel(KevoreeXmiHelper.loadStream(stream))) {
                lastCheckedTimeStamp.set(this.getModelService().getLastModification());
                VectorClock clock = VersionUtils.merge(this.clockRef.get(), versioned.getVector());

                
                
                System.out.println("merged");
                print(clock);
                this.clockRef.set(clock);
            }
        } else {
            System.out.println("Null rec");
        }

    }

    /* Pick a node randomly */
    protected ContainerNode selectPeer() {
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
        return groupNode.get(peerIndex);
    }


    /* Override in child classes */
    protected abstract VectorClock getVectorFromPeer(ContainerNode node);

    /* Override in child classes*/
    protected abstract VersionedModel getVersionnedModelToPeer(ContainerNode node);
}
