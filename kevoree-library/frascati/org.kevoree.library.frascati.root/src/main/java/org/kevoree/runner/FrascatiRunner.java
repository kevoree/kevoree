package org.kevoree.runner;

import org.kevoree.platform.osgi.standalone.App;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/02/12
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public class FrascatiRunner {

    public static void main( String[] args ) throws Exception {

        System.setProperty("node.bootstrap", FrascatiRunner.class.getClassLoader().getResource("fbootstrap.kev").getPath());
        System.setProperty("node.name", "node0");
        App.main(args);

    }

}
