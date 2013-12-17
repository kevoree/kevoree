package org.kevoree.platform.standalone;

import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.impl.DefaultKevoreeFactory;

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
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        Bootstrap bootstrap = new Bootstrap(nodeName);
        String bootstrapModel = System.getProperty("node.bootstrap");
        if (bootstrapModel != null) {
            bootstrap.bootstrapFromFile(new File(bootstrapModel));
        } else {
            if(new DefaultKevoreeFactory().getVersion().toLowerCase().contains("snapshot")){
                bootstrap.bootstrapFromKevScript(App.class.getClassLoader().getResourceAsStream("snapshot.kevs"));
            } else {
                bootstrap.bootstrapFromKevScript(App.class.getClassLoader().getResourceAsStream("default.kevs"));
            }
        }
    }

}
