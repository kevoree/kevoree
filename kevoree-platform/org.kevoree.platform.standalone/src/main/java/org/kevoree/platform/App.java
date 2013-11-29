package org.kevoree.platform;

import org.kevoree.bootstrap.Bootstrap;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/2013
 * Time: 11:27
 */
public class App {

    private static final String defaultNodeName = "node0";

    public static void main(String[] args) throws Exception {
        System.out.println("Kevoree Runtime");
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        Bootstrap bootstrap = new Bootstrap(nodeName);
        String bootstrapModel = System.getProperty("node.bootstrap");
        if (bootstrapModel != null) {
            bootstrap.bootstrapFromFile(new File(bootstrapModel));
        } else {
            bootstrap.bootstrapFromKevScript(App.class.getClassLoader().getResourceAsStream("default.kevs"));
        }
    }

}
