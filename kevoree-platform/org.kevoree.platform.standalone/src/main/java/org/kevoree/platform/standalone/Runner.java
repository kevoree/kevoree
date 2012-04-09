package org.kevoree.platform.standalone;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/02/12
 * Time: 17:46
 */
public class Runner {

    public static void main( String[] args ) throws Exception {

        
     //   System.setProperty("kevoree.offline","true");
        System.setProperty("node.bootstrap", "/home/barais/triskell.kev");
        System.setProperty("node.name", "node0");
        //System.setProperty("node.log.level","DEBUG");
        //System.setProperty("node.update.timeout","30000");
        App.main(args);
    }

}
