package org.kevoree.kevscript;

import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.DeployUnit;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class CtxVarsTest extends AbstractKevScriptTest {

    @Test
    public void testSimple() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String NODE_NAME = "myNode";
            ctxVars.put("node", NODE_NAME);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/simple.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID(NODE_NAME);
            assertNotNull(node);
            assertEquals(NODE_NAME, node.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testComponent() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String COMP = "ticker";
            ctxVars.put("comp", COMP);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/component.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node.findComponentsByID(COMP));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testTdefVersion() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String TDEF_VERS = "42";
            ctxVars.put("TDEF_VERS", TDEF_VERS);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/tdef-version.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals(TDEF_VERS, node.getTypeDefinition().getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testSet() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String LEVEL = "DEBUG";
            ctxVars.put("LEVEL", LEVEL);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/set.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals(LEVEL, node.getDictionary().findValuesByID("logLevel").getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testSetInString() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String LEVEL = "DEBUG";
            ctxVars.put("LEVEL", LEVEL);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/set-in-string.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            // assert that ctxVar in strings are not processed by interpreter
            assertEquals("%LEVEL%", node.getDictionary().findValuesByID("logLevel").getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testMultiple() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String NODE0 = "myNode0";
            final String NODE1 = "myNode1";
            ctxVars.put("node0", NODE0);
            ctxVars.put("node1", NODE1);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/multiple.kevs"), this.model, ctxVars);
            assertNotNull(this.model.findNodesByID(NODE0));
            assertNotNull(this.model.findNodesByID(NODE1));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testDuVersion() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            ctxVars.put("DU_VERS", "RELEASE");
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/du-version.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            TypeDefinition tdef = node.getTypeDefinition();
            DeployUnit du = (DeployUnit) tdef.select("deployUnits[]/filters[name=platform,value=js]").get(0).eContainer();
            assertEquals("5.4.0", du.getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testNetwork() throws KevScriptException {
        try {
            HashMap<String, String> ctxVars = new HashMap<>();
            final String IP = "1.2.3.4";
            ctxVars.put("ip", IP);
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/ctxVars/network.kevs"), this.model, ctxVars);
            ContainerNode node = this.model.findNodesByID("node");
            assertEquals(IP, node.findNetworkInformationByID("foo").findValuesByID("bar").getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }
}
