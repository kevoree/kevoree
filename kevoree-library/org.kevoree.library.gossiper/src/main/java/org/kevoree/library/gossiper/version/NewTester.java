/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper.version;

import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;

/**
 *
 * @author ffouquet
 */
public class NewTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        VectorClock v1 = org.kevoree.library.gossiper.version.GossiperMessages.VectorClock.newBuilder().
                setTimestamp(System.currentTimeMillis()).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("A").setVersion(2).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("B").setVersion(3).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("C").setVersion(1).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("D").setVersion(5).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("E").setVersion(4).setTimestamp(System.currentTimeMillis())).build();

        VectorClock v2 = org.kevoree.library.gossiper.version.GossiperMessages.VectorClock.newBuilder().
                setTimestamp(System.currentTimeMillis()).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("E").setVersion(2).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("C").setVersion(5).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("B").setVersion(4).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("D").setVersion(3).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("A").setVersion(1).setTimestamp(System.currentTimeMillis())).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("F").setVersion(6).setTimestamp(System.currentTimeMillis())).build();

        VectorClock v3 = VersionUtils.merge(v1, v2);

       for(ClockEntry ce : v3.getEntiesList()){
           System.out.println(ce.getNodeID()+":"+ce.getVersion()+"-"+ce.getTimestamp());
       }


    }
}
