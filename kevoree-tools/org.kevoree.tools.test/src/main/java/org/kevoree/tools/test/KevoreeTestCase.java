package org.kevoree.tools.test;

import junit.framework.TestCase;
import org.junit.After;
import org.kevoree.log.Log;

import java.util.HashMap;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreeTestCase {

    private HashMap<String, KevoreePlatformCtrl> runners = new HashMap<String, KevoreePlatformCtrl>();

    public void shutdown(String nodeName) throws Exception {
        if (runners.containsKey(nodeName)) {
            KevoreePlatformCtrl p = runners.get(nodeName);
            p.stop();
        } else {
            throw new Exception("Not started : " + nodeName);
        }
    }

    public void bootstrap(String nodeName, String bootfile) throws Exception {
        if (runners.containsKey(nodeName)) {
            throw new Exception("Already started : " + nodeName);
        }
        KevoreePlatformCtrl p = new KevoreePlatformCtrl(nodeName);
        runners.put(nodeName, p);
        p.start(bootfile);
        Log.info("Kevoree Platform started {}",nodeName);
    }

    @After
    public void tearDown() throws Exception {
        Log.info("Cleanup and stop every platforms");
        //shutdown all platforms
        for (String nodeName : runners.keySet()) {
            shutdown(nodeName);
        }
    }



}
