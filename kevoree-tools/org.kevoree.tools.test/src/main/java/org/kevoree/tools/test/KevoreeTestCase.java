package org.kevoree.tools.test;

import org.junit.After;
import org.kevoree.ContainerRoot;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreeTestCase {

    private HashMap<String, KevoreePlatformCtrl> runners = new HashMap<String, KevoreePlatformCtrl>();

    private AtomicInteger integer = new AtomicInteger(2000);

    public void shutdown(String nodeName) throws Exception {
        if (runners.containsKey(nodeName)) {
            KevoreePlatformCtrl p = runners.get(nodeName);
            p.stop();
        } else {
            throw new Exception("Not started : " + nodeName);
        }
    }

    public void bootstrap(String nodeName) throws Exception {
        bootstrap(nodeName, null);
    }

    public void bootstrap(String nodeName, String bootfile) throws Exception {
        if (runners.containsKey(nodeName)) {
            throw new Exception("Already started : " + nodeName);
        }
        KevoreePlatformCtrl p = new KevoreePlatformCtrl(nodeName);
        p.setModelDebugPort(integer.getAndIncrement());
        runners.put(nodeName, p);
        p.start(bootfile);
        Log.info("Kevoree Platform started {}", nodeName);
    }

    @After
    public void tearDown() throws Exception {
        Log.info("Cleanup and stop every platforms");
        //shutdown all platforms
        for (String nodeName : runners.keySet()) {
            shutdown(nodeName);
        }
    }

    public ContainerRoot getCurrentModel(String nodeName) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            KevoreePlatformCtrl runner = runners.get(nodeName);
            runner.getWorker().send("getModel");
            JSONModelLoader loader = new JSONModelLoader();
            ContainerRoot model = (ContainerRoot) loader.loadModelFromString(runner.getWorker().recvStr()).get(0);
            return model;
        }
    }

    public void deploy(String nodeName, ContainerRoot model) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            KevoreePlatformCtrl runner = runners.get(nodeName);
            JSONModelSerializer saver = new JSONModelSerializer();
            String modelTxt = saver.serialize(model);
            runner.getWorker().send("pushModel");
            runner.getWorker().recv();
            runner.getWorker().send(modelTxt);
            if (!Boolean.parseBoolean(runner.getWorker().recvStr())) {
                throw new Exception("Model deploy error : " + nodeName);
            }        }
    }

    public void exec(String nodeName, String script) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            KevoreePlatformCtrl runner = runners.get(nodeName);
            runner.getWorker().send("pushScript");
            runner.getWorker().recv();
            runner.getWorker().send(script);
            if (!Boolean.parseBoolean(runner.getWorker().recvStr())) {
                throw new Exception("Script execution error : " + nodeName);
            }
        }
    }

}
