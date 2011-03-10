/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.DictionaryAttribute;
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.gossiper.version.VersionUtils;

/**
 * @author ffouquet
 */
//@GroupType
@DictionaryType({
	@DictionaryAttribute(name = "interval", defaultValue = "60000", optional = true)
})
public abstract class GossipGroup extends AbstractGroupType implements Runnable {

	private boolean running;
	public final AtomicReference<VectorClock> clockRef = new AtomicReference<VectorClock>(VectorClock.newBuilder().build());

	@Override
	public void triggerModelUpdate() {
		System.out.println("Update trigger");
	}

	@Start
	public void startMyGroup() {
		System.out.println("StartGroup " + this.getClass().getName());
		running = true;
		Thread myThread = new Thread(this);
		myThread.setPriority(Thread.MIN_PRIORITY);
		myThread.start();

		System.out.println("last date " + this.getModelService().getLastModification());

	}

	@Stop
	public void stopMyGroup() {
		System.out.println("StopGroup " + this.getClass().getName());
		running = false;
	}

	@Update
	public void updateMyGroup() {
		System.out.println("UpdateGroup " + this.getClass().getName());
	}

	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(Integer.parseInt((String)this.getDictionary().get("interval")));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			ContainerNode node = selectPeer();
			//adminClient.setAdminClientCluster(metadataStore.getCluster());
			VectorClock clock = getVectorFromPeer(node);
			Occured result = VersionUtils.compare(this.clockRef.get(), clock);
			/*if (result == Occured.BEFORE) {
			// we do nothing because VectorClocks are equals (and so models are equals)
			// or local VectorClock is more recent (and so local model is more recent)
			} else*/
			if (result == Occured.AFTER) {
				// we update our local model because the selected peer has a more recent VectorClock (and so a more recent model)
				updateClock(node);
			}/* else if (result == Occured.CONCURRENTLY) {
			// We do nothing because we do not know what we need to do !
			
			// Other possibility not implemented :
			// It is not possible to find the most recent VectorClock (and so the more recent model)
			// That's why we choose to keep all local information about local node, component, ...
			// We also choose to keep all remote information describe into the model of the selected peer.
			
			}*/

		}
	}

	private synchronized void updateClock(ContainerNode node) {
		VersionedModel versioned = getVersionnedModelToPeer(node);
		if (this.getModelService().updateModel(KevoreeXmiHelper.load(versioned.getModel()))) {
			VectorClock clock = VersionUtils.merge(this.clockRef.get(), versioned.getVector());
			this.clockRef.set(clock);
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
