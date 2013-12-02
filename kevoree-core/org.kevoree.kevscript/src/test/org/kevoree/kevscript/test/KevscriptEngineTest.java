package org.kevoree.kevscript.test;

import org.junit.Test;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.kevscript.Parser;
import org.kevoree.kevscript.Type;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.serializer.JSONModelSerializer;
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
 * To change this template use File | Settings | File Templates.
 */
public class KevscriptEngineTest {


    @Test
    public void _00_justParse() {
        Parser parser = new Parser();
        ParseResult<Type> parserResult = parser.parse(new InputBuffer(BufferFiller.asArray(KevscriptEngineTest.class.getResourceAsStream("/parserTestInput.kevs"))));
        assertNull("An error occurred during parse:" + parserResult.getError(),parserResult.getError());
    }



    @Test
    public void _01_parseAndInterpretTest() {
        try {
            KevScriptEngine engine = new KevScriptEngine();
            KevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot cr = factory.createContainerRoot();
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/parseInterpretTestInput.kevs"), cr);
        } catch (Exception e) {
           fail("An exception occurred:" + e.toString());
        }

    }


    //@Test
    public void _02_loadExecuteSave() {

        try {

            KevScriptEngine engine = new KevScriptEngine();
            JSONModelLoader loader = new JSONModelLoader();

            ContainerRoot root = (ContainerRoot) loader.loadModelFromStream(KevscriptEngineTest.class.getResourceAsStream("/lib.json")).get(0);
            engine.executeFromStream(KevscriptEngineTest.class.getResourceAsStream("/simple.kevs"), root);
            JSONModelSerializer saver = new JSONModelSerializer();
            saver.serializeToStream(root, System.out);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public static void main(String[] args) throws Exception {
        KevscriptEngineTest test = new KevscriptEngineTest();
        test._02_loadExecuteSave();
    }




}
