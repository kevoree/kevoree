package org.kevoree.runner;

import org.kevoree.platform.standalone.App;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/02/12
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public class FrascatiRunner {

    public static void main( String[] args ) throws Exception {

        System.setProperty("node.bootstrap", FrascatiRunner.class.getClassLoader().getResource("run.kev").getPath());
        System.setProperty("node.name", "node0");
        System.setProperty("node.log.level","DEBUG");
        System.setProperty("node.update.timeout","100000");

        App.main(args);

    }

}
