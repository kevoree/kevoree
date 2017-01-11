package org.kevoree.kevscript;

import org.junit.Test;
import org.kevoree.Channel;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;

import static org.junit.Assert.*;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class BindTest extends AbstractKevScriptTest {

    @Test
    public void testSimple() throws Exception {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/bind/simple.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            ComponentInstance ticker = node.findComponentsByID("ticker");
            assertNotNull(ticker);
            Channel chan = this.model.findHubsByID("chan");
            assertNotNull(chan);
            assertEquals(1, this.model.getmBindings().size());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }
}
