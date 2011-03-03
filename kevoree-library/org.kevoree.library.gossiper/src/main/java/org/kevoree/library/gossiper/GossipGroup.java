/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractGroupType;

/**
 *
 * @author ffouquet
 */
@GroupType
public class GossipGroup extends AbstractGroupType {

    @Override
    public void update(ContainerRoot cr) {
        
    }

    @Start
    public void startGroup(){

    }

    @Stop
    public void stopGroup(){

    }

}
