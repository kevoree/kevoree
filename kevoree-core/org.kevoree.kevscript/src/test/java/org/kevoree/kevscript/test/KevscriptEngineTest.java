package org.kevoree.kevscript.test;

import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.kevscript.Parser;
import org.kevoree.kevscript.Type;
import org.kevoree.pmodeling.api.json.JSONModelLoader;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;
import org.waxeye.input.BufferFiller;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: gregory.nain
 * Date: 02/12/2013
 * Time: 17:03
 */
public class KevscriptEngineTest {

    //@Test
    public void _01_parseAndInterMinimal() {
        try {
            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot cr = factory.createContainerRoot();
            factory.root(cr);
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/minimal.kevs"), cr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    //@Test
    public void _01_parseAndInterMinimal2() {
        try {
            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot cr = factory.createContainerRoot();
            factory.root(cr);
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/minimal2.kevs"), cr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }

    // @Test
    public void _00_justParse() {
        Parser parser = new Parser();
        ParseResult<Type> parserResult = parser.parse(new InputBuffer(BufferFiller.asArray(KevscriptEngineTest.class.getResourceAsStream("/parserTestInput.kevs"))));
        assertNull("An error occurred during parse:" + parserResult.getError(), parserResult.getError());
    }


    //    @Test
    public void _01_parseAndInterpretTest() {
        try {
            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot cr = factory.createContainerRoot();
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/parseInterpretTestInput.kevs"), cr);
        } catch (Exception e) {
            e.printStackTrace();

            fail("An exception occurred:" + e.toString());
        }

    }

    //  @Test
    public void testLifecycle() {
        try {
            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot cr = factory.createContainerRoot();
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/lifecycle.kevs"), cr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }

    }


    //@Test
    public void _02_loadExecuteSave() {

        try {

            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            JSONModelLoader loader = new JSONModelLoader(factory);

            ContainerRoot root = (ContainerRoot) loader.loadModelFromStream(KevscriptEngineTest.class.getResourceAsStream("/lib.json")).get(0);
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/parseInterpretTestInput.kevs"), root);
            JSONModelSerializer saver = new JSONModelSerializer();
            saver.serializeToStream(root, System.out);

        } catch (Exception e) {
//            e.printStackTrace();

//            fail("An exception occurred:" + e.toString());
        }
    }

    //  @Test
    public void _01_parseAndInterpretBigScriptTest() {
        try {
            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot cr = factory.createContainerRoot();
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/bigScript.kevs"), cr);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred:" + e.toString());
        }
    }


    public static void main(String[] args) throws Exception {
        /*KevscriptEngineTest test = new KevscriptEngineTest();
        test._02_loadExecuteSave();*/
    }


}
