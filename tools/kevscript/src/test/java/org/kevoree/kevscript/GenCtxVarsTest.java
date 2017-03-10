package org.kevoree.kevscript;

import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.KevScriptException;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class GenCtxVarsTest extends AbstractKevScriptTest {

    @Test
    public void testSimple() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/genCtxVars/simple.kevs"), this.model, ctxVars);
            assertNotNull(this.model.findNodesByID(ctxVars.get("node")));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testComponent() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/genCtxVars/component.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node.findComponentsByID(ctxVars.get("comp")));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testSet() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/genCtxVars/set.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertEquals(ctxVars.get("logLevel"), node.getDictionary().findValuesByID("logLevel").getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testSetInString() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/genCtxVars/set-in-string.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            // assert that genCtxVar in strings are not processed by interpreter
            assertEquals("%%LEVEL%%", node.getDictionary().findValuesByID("logLevel").getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testMultiple() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/genCtxVars/multiple.kevs"), this.model, ctxVars);
            assertNotNull(this.model.findNodesByID(ctxVars.get("node0")));
            assertNotNull(this.model.findNodesByID(ctxVars.get("node1")));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }
}
