package org.kevoree.platform.standalone.telemetry;

import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.impl.TelemetryEventImpl;
import org.kevoree.factory.DefaultKevoreeFactory;

import java.io.*;

/**
 * Created by duke on 8/7/14.
 */
public class App {

    public static final String defaultNodeName = "node0";

    public static void main(String[] args) throws Exception {
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        String telemetryURL = "tcp://localhost:9966";
        if (System.getProperty("telemetry.url") != null) {
            telemetryURL = System.getProperty("telemetry.url");
        }
        MQTTDispatcher dispatcher = new MQTTDispatcher(telemetryURL, nodeName);

        try {

            TelemetryEvent event = TelemetryEventImpl.build(nodeName, "info", "Initiate Telemtry monitoring", "");
            dispatcher.notify(event);

            final Bootstrap bootstrap = new Bootstrap(nodeName);
            bootstrap.getCore().addTelemetryListener(dispatcher);

            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
                public void run() {
                    try {
                        bootstrap.stop();
                    } catch (Throwable ex) {
                        System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                    }
                }
            });
            String bootstrapModel = System.getProperty("node.bootstrap");


            if (bootstrapModel != null) {
                bootstrap.bootstrapFromFile(new File(bootstrapModel));
            } else {
                bootstrap.bootstrapFromKevScript(createBootstrapScript(nodeName));
            }
        } catch (Exception e) {
            ByteArrayOutputStream boo = new ByteArrayOutputStream();
            PrintStream pr = new PrintStream(boo);
            e.printStackTrace(pr);
            pr.flush();
            pr.close();


            TelemetryEvent event = TelemetryEventImpl.build(nodeName, "error", "Error during bootstrap", new String(boo.toByteArray()));
            dispatcher.notify(event);

        }
    }

    public static InputStream createBootstrapScript(String nodeName) {
        StringBuilder buffer = new StringBuilder();
        String versionRequest;
        if (new DefaultKevoreeFactory().getVersion().toLowerCase().contains("snapshot")) {
            buffer.append("repo \"https://oss.sonatype.org/content/groups/public/\"\n");
            versionRequest = "latest";
        } else {
            buffer.append("repo \"http://repo1.maven.org/maven2/\"\n");
            versionRequest = "release";
        }
        buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:");
        buffer.append(versionRequest);
        buffer.append("\n");
        buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:");
        buffer.append(versionRequest);
        buffer.append("\n");
        buffer.append("add node0 : JavaNode".replace("node0", nodeName) + "\n");
        buffer.append("add sync : WSGroup\n");
        buffer.append("attach node0 sync\n".replace("node0", nodeName));
        return new ByteArrayInputStream(buffer.toString().getBytes());
    }

}
