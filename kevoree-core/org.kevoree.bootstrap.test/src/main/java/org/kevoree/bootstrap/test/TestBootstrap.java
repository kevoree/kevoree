package org.kevoree.bootstrap.test;

import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.KevoreeCoreBean;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Created by duke on 8/13/14.
 */
public class TestBootstrap {

    public static void main(String[] args) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = Bootstrap.defaultNodeName;
        }
        final Bootstrap boot = new Bootstrap(KevoreeKernel.self.get(), nodeName, "http://registry.kevoree.org");
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(loader);
                    boot.stop();
                } catch (Throwable ex) {
                    System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                }
            }
        });
        String bootstrapModel = System.getProperty("node.bootstrap");
        final KevoreeCoreBean core = boot.getCore();
        UpdateCallback callback = new UpdateCallback() {
            @Override
            public void run(Boolean aBoolean) {
                int port = 2000;
                if (System.getProperty("model.debug.port") != null) {
                    port = Integer.parseInt(System.getProperty("model.debug.port"));
                }
                Log.info("Start management port on {}", port);

                Thread server = new Thread(new JeroMQCoreWrapper(core, port));
                server.start();
            }
        };
        try {
            if (bootstrapModel != null) {
                boot.bootstrapFromFile(new File(bootstrapModel), callback);
            } else {
                boot.bootstrapFromKevScript(new ByteArrayInputStream(System.getProperty("node.script").getBytes()), callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
