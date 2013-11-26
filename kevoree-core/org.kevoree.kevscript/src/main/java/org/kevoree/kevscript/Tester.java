package org.kevoree.kevscript;

import org.kevoree.ContainerRoot;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 10:08
 */
public class Tester {


    public static void main(String[] args) throws FileNotFoundException {
        KevScriptEngine engine = new KevScriptEngine();
        JSONModelLoader loader = new JSONModelLoader();

        ContainerRoot root = (ContainerRoot) loader.loadModelFromStream(Tester.class.getResourceAsStream("/lib.json")).get(0);
        engine.executeFromStream(Tester.class.getResourceAsStream("/test.kevs"), root);
        JSONModelSerializer saver = new JSONModelSerializer();
        //saver.serializeToStream(root, System.out);
    }

}
