package org.kevoree.tools.test;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreeTestCase extends TestCase {

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
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        //shutdown all platforms
        for (String nodeName : runners.keySet()) {
            shutdown(nodeName);
        }
    }



}
