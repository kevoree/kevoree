/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper;

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
public class GossipGroup extends AbstractGroupType {

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

}
