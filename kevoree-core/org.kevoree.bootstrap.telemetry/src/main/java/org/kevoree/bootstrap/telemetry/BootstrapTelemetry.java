package org.kevoree.bootstrap.telemetry;

import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.impl.TelemetryEventImpl;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;

/**
 * Created by duke on 8/14/14.
 */
public class BootstrapTelemetry {

    public static void main(String[] args) throws URISyntaxException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = Bootstrap.defaultNodeName;
        }
        String telemetryURL = "tcp://localhost:9966";
        if (System.getProperty("telemetry.url") != null) {
            telemetryURL = System.getProperty("telemetry.url");
        }
        Log.info("Telemetry Server : " + telemetryURL);
        MQTTDispatcher dispatcher = new MQTTDispatcher(telemetryURL, nodeName);
        TelemetryEvent event = TelemetryEventImpl.build(nodeName, "info", "Initiate Telemtry monitoring", "");
        dispatcher.notify(event);

        final Bootstrap boot = new Bootstrap(KevoreeKernel.self.get(), nodeName);
        boot.getCore().addTelemetryListener(dispatcher);
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
        try {
            if (bootstrapModel != null) {
                boot.bootstrapFromFile(new File(bootstrapModel));
            } else {
                boot.bootstrapFromKevScript(new ByteArrayInputStream(System.getProperty("node.script").getBytes()));
            }
        } catch (Exception e) {
            ByteArrayOutputStream boo = new ByteArrayOutputStream();
            PrintStream pr = new PrintStream(boo);
            e.printStackTrace(pr);
            pr.flush();
            pr.close();
            TelemetryEvent event2 = TelemetryEventImpl.build(nodeName, "error", "Error during bootstrap", new String(boo.toByteArray()));
            dispatcher.notify(event2);
            e.printStackTrace();
        }
    }

}
