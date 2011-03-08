/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.AbstractGroupType;

/**
 *
 * @author ffouquet
 */
@GroupType
public class GossipGroup extends AbstractGroupType implements Runnable {

	private boolean running = true;
	private int gossipInterval = 5000;
	
    @Override
    public void triggerModelUpdate() {
        System.out.println("Update trigger");
    }

    @Start
    public void startMyGroup() {
        System.out.println("StartGroup " + this.getClass().getName());

        System.out.println("last date "+this.getModelService().getLastModification());

    }

    @Stop
    public void stopMyGroup() {
        System.out.println("StopGroup " + this.getClass().getName());
    }

    @Update
    public void updateMyGroup(){
        System.out.println("UpdateGroup " + this.getClass().getName());
    }

	@Override
	public void run() {
		while(running) {
            try {
                Thread.sleep(gossipInterval);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ContainerNode node = selectPeer();
            //adminClient.setAdminClientCluster(metadataStore.getCluster());

			// TODO :
			  // VectorClock clock = node.getGroup().get();
				//Occured result =  this.clock.compare(clock);
			// if () {

        }
	}

	 /**
     * Randomly select a distinct peer. Method is <code>protected</code> rather
     * than <code>private</code>, so that it may be  overridden if
     * peer selection logic is to be changed e.g., to add datacenter/rack awareness.
     *
     * @return Peer for Gossip.
     */
    protected ContainerNode selectPeer() {
        Cluster cluster = metadataStore.getCluster();
        int nodes = cluster.getNumberOfNodes();
        Node node;
        do {
            node = cluster.getNodeById(random.nextInt(nodes));
        } while(node.getId() == metadataStore.getNodeId());

        return node;
    }
}
