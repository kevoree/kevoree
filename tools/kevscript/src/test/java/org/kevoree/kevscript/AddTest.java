package org.kevoree.kevscript;

import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.KevScriptException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class AddTest extends AbstractKevScriptTest {

    @Test
    public void testSimple() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/add/simple.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testMultiple() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/add/multiple.kevs"), this.model);
            ContainerNode node0 = this.model.findNodesByID("node0");
            ContainerNode node1 = this.model.findNodesByID("node1");
            ContainerNode node2 = this.model.findNodesByID("node2");
            assertNotNull(node0);
            assertNotNull(node1);
            assertNotNull(node2);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }
}
