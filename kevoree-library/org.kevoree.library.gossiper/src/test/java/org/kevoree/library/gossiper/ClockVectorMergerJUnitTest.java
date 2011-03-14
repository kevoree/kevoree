/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.ChannelRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.WritableRepresentation;

/**
 *
 * @author ffouquet
 */
public class ClockVectorMergerJUnitTest {

    public ClockVectorMergerJUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void checkMerger() {
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

        VectorClockActor actor = new VectorClockActor("C");
        actor.swap(v1);
        VectorClock v3 = actor.merge(v2);

        assert (v3.getEnties(0).getNodeID().equals("A"));
        assert (v3.getEnties(0).getVersion() == 2);

        assert (v3.getEnties(1).getNodeID().equals("E"));
        assert (v3.getEnties(1).getVersion() == 4);

        assert (v3.getEnties(2).getNodeID().equals("B"));
        assert (v3.getEnties(2).getVersion() == 4);

        assert (v3.getEnties(3).getNodeID().equals("C"));
        assert (v3.getEnties(3).getVersion() == 5);

        assert (v3.getEnties(4).getNodeID().equals("D"));
        assert (v3.getEnties(4).getVersion() == 5);

        assert (v3.getEnties(5).getNodeID().equals("F"));
        assert (v3.getEnties(5).getVersion() == 6);






    }
	 
	@Test
    public void checkCompare() {
		 VectorClock v1 = org.kevoree.library.gossiper.version.GossiperMessages.VectorClock.newBuilder().
                setTimestamp(System.currentTimeMillis()).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("A").setVersion(2).setTimestamp(System.currentTimeMillis()))
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("B").setVersion(3).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("C").setVersion(1).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("D").setVersion(5).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("E").setVersion(4).setTimestamp(System.currentTimeMillis()))
				 .build();

        VectorClock v2 = org.kevoree.library.gossiper.version.GossiperMessages.VectorClock.newBuilder().
                setTimestamp(System.currentTimeMillis()).
                addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("E").setVersion(2).setTimestamp(System.currentTimeMillis()))
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("C").setVersion(5).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("B").setVersion(4).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("D").setVersion(3).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("A").setVersion(1).setTimestamp(System.currentTimeMillis())).
                //addEnties(org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry.newBuilder().setNodeID("F").setVersion(6).setTimestamp(System.currentTimeMillis()))
				.build();

		System.out.println(org.kevoree.library.gossiper.VersionUtils.compare(v1, v2));
		
	}
	
}
