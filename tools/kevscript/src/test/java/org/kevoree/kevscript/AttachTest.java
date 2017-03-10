package org.kevoree.kevscript;

import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.Group;
import org.kevoree.KevScriptException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class AttachTest extends AbstractKevScriptTest {

    @Test
    public void testSimple() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/attach/simple.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            Group sync = this.model.findGroupsByID("sync");
            assertNotNull(node);
            assertNotNull(sync);

            assertEquals(1, node.getGroups().size());
            assertEquals(1, sync.getSubNodes().size());

            assertEquals(sync.getName(), node.getGroups().get(0).getName());
            assertEquals(node.getName(), sync.getSubNodes().get(0).getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testMultiple() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/attach/multiple.kevs"), this.model);
            ContainerNode node0 = this.model.findNodesByID("node0");
            ContainerNode node1 = this.model.findNodesByID("node1");
            ContainerNode node2 = this.model.findNodesByID("node2");
            Group sync = this.model.findGroupsByID("sync");

            assertNotNull(node0);
            assertNotNull(node1);
            assertNotNull(node2);
            assertNotNull(sync);

            assertEquals(1, node0.getGroups().size());
            assertEquals(0, node1.getGroups().size());
            assertEquals(1, node2.getGroups().size());
            assertEquals(2, sync.getSubNodes().size());

            assertEquals(sync.getName(), node0.getGroups().get(0).getName());
            assertEquals(sync.getName(), node2.getGroups().get(0).getName());

            assertNotNull(sync.findSubNodesByID(node0.getName()));
            assertNull(sync.findSubNodesByID(node1.getName()));
            assertNotNull(sync.findSubNodesByID(node2.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testWildcard() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/attach/wildcard.kevs"), this.model);
            ContainerNode node0 = this.model.findNodesByID("node0");
            ContainerNode node1 = this.model.findNodesByID("node1");
            ContainerNode node2 = this.model.findNodesByID("node2");
            Group sync = this.model.findGroupsByID("sync");

            assertNotNull(node0);
            assertNotNull(node1);
            assertNotNull(node2);
            assertNotNull(sync);

            assertEquals(1, node0.getGroups().size());
            assertEquals(1, node1.getGroups().size());
            assertEquals(1, node2.getGroups().size());
            assertEquals(3, sync.getSubNodes().size());

            assertEquals(sync.getName(), node0.getGroups().get(0).getName());
            assertEquals(sync.getName(), node1.getGroups().get(0).getName());
            assertEquals(sync.getName(), node2.getGroups().get(0).getName());

            assertNotNull(sync.findSubNodesByID(node0.getName()));
            assertNotNull(sync.findSubNodesByID(node1.getName()));
            assertNotNull(sync.findSubNodesByID(node2.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }
}
