package org.kevoree.kevscript;

import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.KevScriptException;

import static org.junit.Assert.*;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class VersionTest extends AbstractKevScriptTest {

    @Test
    public void testDefaultVersion() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/version/default-version.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals("node", node.getName());
            assertEquals("JavascriptNode", node.getTypeDefinition().getName());
            assertEquals("42", node.getTypeDefinition().getVersion());
            assertEquals("kevoree-node-javascript", node.getTypeDefinition().getDeployUnits().get(0).getName());
            assertEquals("5.4.0", node.getTypeDefinition().getDeployUnits().get(0).getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testIntegerDefault() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/version/integer-default.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals("node", node.getName());
            assertEquals("JavascriptNode", node.getTypeDefinition().getName());
            assertEquals("42", node.getTypeDefinition().getVersion());
            assertEquals("kevoree-node-javascript", node.getTypeDefinition().getDeployUnits().get(0).getName());
            assertEquals("5.4.0", node.getTypeDefinition().getDeployUnits().get(0).getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testIntegerLatest() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/version/integer-latest.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals("node", node.getName());
            assertEquals("JavascriptNode", node.getTypeDefinition().getName());
            assertEquals("1", node.getTypeDefinition().getVersion());
            assertEquals("kevoree-node-javascript", node.getTypeDefinition().getDeployUnits().get(0).getName());
            assertEquals("5.4.0-beta.0", node.getTypeDefinition().getDeployUnits().get(0).getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testIntegerRelease() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/version/integer-release.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals("node", node.getName());
            assertEquals("JavascriptNode", node.getTypeDefinition().getName());
            assertEquals("1", node.getTypeDefinition().getVersion());
            assertEquals("kevoree-node-javascript", node.getTypeDefinition().getDeployUnits().get(0).getName());
            assertEquals("5.4.0", node.getTypeDefinition().getDeployUnits().get(0).getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    @Test
    public void testLatestLatest() throws KevScriptException {
        try {
            this.kevs.executeFromStream(getClass().getResourceAsStream("/kevs/version/latest-latest.kevs"), this.model);
            ContainerNode node = this.model.findNodesByID("node");
            assertNotNull(node);
            assertEquals("node", node.getName());
            assertEquals("JavascriptNode", node.getTypeDefinition().getName());
            assertEquals("42", node.getTypeDefinition().getVersion());
            assertEquals("kevoree-node-javascript", node.getTypeDefinition().getDeployUnits().get(0).getName());
            assertEquals("5.4.0-beta.0", node.getTypeDefinition().getDeployUnits().get(0).getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }
}
