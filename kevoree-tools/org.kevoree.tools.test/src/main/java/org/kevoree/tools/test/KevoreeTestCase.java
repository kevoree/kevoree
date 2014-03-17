package org.kevoree.tools.test;

import org.junit.After;
import org.kevoree.ContainerRoot;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreeTestCase {

    private HashMap<String, KevoreePlatformCtrl> runners = new HashMap<String, KevoreePlatformCtrl>();

    private AtomicInteger integer = new AtomicInteger(2000);

    private Long globalTimeOut = 10000l;

    public void setGlobalTimeOut(Long gt) {
        this.globalTimeOut = gt;
    }

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
        bootstrap(nodeName, bootfile, globalTimeOut);
    }

    public void bootstrap(String nodeName, String bootfile, Long timeout) throws Exception {
        if (runners.containsKey(nodeName)) {
            throw new Exception("Already started : " + nodeName);
        }
        KevoreePlatformCtrl p = new KevoreePlatformCtrl(nodeName);
        p.setModelDebugPort(integer.getAndIncrement());
        runners.put(nodeName, p);
        p.start(bootfile, timeout);
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
            }
        }
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
                throw new Exception("Script execution error on " + nodeName + " : " + script);
            }
        }
    }

    public void waitLog(String nodeName, String log, long timems) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            long initTime = System.currentTimeMillis();
            KevoreePlatformCtrl runner = runners.get(nodeName);
            boolean found = false;
            while (!found) {
                long current = System.currentTimeMillis();
                long spent = current - initTime;
                if (spent > timems) {
                    throw new Exception("WaiLog on " + nodeName + " TimeOut / " + log);
                }
                String line = runner.getLines().poll(timems - spent, TimeUnit.MILLISECONDS);
                if (line != null) {
                    line = nodeName + "/" + line;
                    if (line.equals(log)) {
                        found = true;
                    } else {
                        Boolean result;
                        try {
                            result = line.matches(log);
                        } catch (Exception e) {
                            result = false;
                        }
                        if (result) {
                            found = true;
                        } else {
                            String logR = log.replace("*", ".*").replace("[", "\\[").replace("]", "\\]");
                            if (line.matches(logR)) {
                                found = true;
                            }
                        }
                    }
                }
            }
        }
    }


}
