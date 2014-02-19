package org.kevoree.platform.standalone.test;

import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.log.Log;

import java.io.File;

/**
 * Created by duke on 18/02/2014.
 */
public class App {

    public static void main(String[] args) throws Exception {
        String nodeName = System.getProperty("node.name");
        Bootstrap bootstrap = new Bootstrap(nodeName);
        final KevoreeCoreBean core = bootstrap.getCore();

        if (nodeName == null) {
            nodeName = org.kevoree.platform.standalone.App.defaultNodeName;
        }
        String bootstrapModel = System.getProperty("node.bootstrap");

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

        if (bootstrapModel != null) {
            bootstrap.bootstrapFromFile(new File(bootstrapModel),callback);
        } else {
            bootstrap.bootstrapFromKevScript(org.kevoree.platform.standalone.App.createBootstrapScript(nodeName),callback);
        }


    }

}
