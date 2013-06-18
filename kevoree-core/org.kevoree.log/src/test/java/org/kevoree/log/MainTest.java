package org.kevoree.log;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/06/13
 * Time: 19:42
 */
public class MainTest {

    @Test
    public void simpleTest() {
        long previous = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Log.info("Hello from VM={} OS={}", System.getProperty("java.vendor"), System.getProperty("os.name"));
        }
        System.out.println("time " + (System.currentTimeMillis() - previous) + " ms");
    }

    @Test
    public void simpleCallerTest() {
        Log.setPrintCaller(true);
        simpleTest();
    }

}
