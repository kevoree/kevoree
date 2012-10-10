package org.kevoree.library.javase.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractGroupType;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 12/09/12
 * Time: 11:16
 */
@GroupType
public class HazelGroup extends AbstractGroupType implements MembershipListener {

    HazelcastInstance hazelInstance = null;

    @Start
    public void startHazel(){
        Config configApp = new Config();
        configApp.getGroupConfig().setName("KGroup_HazelGroup_"+getName());
        configApp.setInstanceName("KGroup_HazelGroup_"+getNodeName());
        hazelInstance = Hazelcast.newHazelcastInstance(configApp);
        Cluster cluster = hazelInstance.getCluster();
        cluster.addMembershipListener(this);
    }

    @Stop
    public void stopHazel(){
        hazelInstance.shutdown();
        hazelInstance = null;
    }

    @Override
    public void triggerModelUpdate() {

    }

    @Override
    public void push(ContainerRoot containerRoot, String s) throws Exception {

    }

    @Override
    public ContainerRoot pull(String s) throws Exception {
        return null;
    }


    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        System.out.println("source="+membershipEvent.getSource());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        System.out.println("source="+membershipEvent.getSource());
    }

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}
}
