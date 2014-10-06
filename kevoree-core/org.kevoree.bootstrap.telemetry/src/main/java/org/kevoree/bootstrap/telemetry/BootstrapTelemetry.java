package org.kevoree.bootstrap.telemetry;

import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.impl.TelemetryEventImpl;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;

import java.io.*;
import java.net.URISyntaxException;

/**
 * Created by duke on 8/14/14.
 */
public class BootstrapTelemetry {

    private static PrintStream systemOut, systemErr, myOut, myErr;
    private static JMXClient jmxClient;
    private static SigarClient sigarClient;
    private static MQTTDispatcher dispatcher;
    private static String nodeName;

    private static void activateJMX() {
        jmxClient = new JMXClient(dispatcher, nodeName);
        jmxClient.init();
    }

    private static void stopJMX() {
        jmxClient.close();
    }

    private static void activateSigar() {
        sigarClient = new SigarClient(dispatcher, nodeName);
        sigarClient.init();
    }

    private static void stopSigar() {
        sigarClient.stop();
    }

    public static void main(String[] args) throws URISyntaxException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = Bootstrap.defaultNodeName;
        }
        final String finalNodeName = nodeName;

        String telemetryURL = "tcp://localhost:9966";
        if (System.getProperty("telemetry.url") != null) {
            telemetryURL = System.getProperty("telemetry.url");
        }
        Log.info("Telemetry Server : " + telemetryURL);
        dispatcher = new MQTTDispatcher(telemetryURL, nodeName);

        Log.setLogger(new Log.Logger(){
            @Override
            public void log(int level, String message, Throwable ex) {
                switch (level) {
                    case Log.LEVEL_ERROR:dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_ERROR, message.replace("\n","\\n"), (ex!=null?ex.toString().replace("\n","\\n").replace("\t","\\t"):"")));break;
                    case Log.LEVEL_WARN:dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_WARNING, message.replace("\n","\\n"), (ex!=null?ex.toString().replace("\n","\\n").replace("\t","\\t"):"")));break;
                    case Log.LEVEL_INFO:dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_INFO, message.replace("\n","\\n"), (ex!=null?ex.toString().replace("\n","\\n").replace("\t","\\t"):"")));break;
                    case Log.LEVEL_DEBUG:dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_DEBUG, message.replace("\n","\\n"), (ex!=null?ex.toString().replace("\n","\\n").replace("\t","\\t"):"")));break;
                    case Log.LEVEL_TRACE:dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_TRACE, message.replace("\n","\\n"), (ex!=null?ex.toString().replace("\n","\\n").replace("\t","\\t"):"")));break;
                    default: dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_DEBUG, message.replace("\n","\\n"), (ex!=null?ex.toString().replace("\n","\\n").replace("\t","\\t"):"")));break;
                }
            }
        });

        dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.LOG_INFO, "Initiate Telemetry monitoring", ""));

        dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.LOG_INFO, "Initiate JMX Telemetry", ""));
        activateJMX();
        //activateSigar();

        systemOut = System.out;
        systemErr = System.err;

        myOut = new PrintStream(new OutputStream() {
            StringBuffer buffer = new StringBuffer();

            @Override
            public void flush() throws IOException {
                systemOut.flush();
                dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_DEBUG, buffer.toString(), ""));
                buffer = new StringBuffer();
            }

            @Override
            public void write(int b) throws IOException {
                systemOut.write(b);
                buffer.append((char)b);
            }
        });
        myErr = new PrintStream(new OutputStream() {
            StringBuffer buffer = new StringBuffer();

            @Override
            public void flush() throws IOException {
                systemErr.flush();
                dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_DEBUG, buffer.toString(), ""));
                buffer = new StringBuffer();
            }

            @Override
            public void write(int b) throws IOException {
                systemErr.write(b);
                buffer.append((char)b);
            }
        });

        hackSystemStreams();

        final Bootstrap boot = new Bootstrap(KevoreeKernel.self.get(), nodeName);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(loader);
                    System.out.println("Shutting system down");
                    stopJMX();
                    // stopSigar();
                    boot.stop();
                    dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_INFO, "Platform stopped", ""));
                } catch (Throwable ex) {
                    dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_ERROR, "Error stopping kevoree platform", ex.toString()));
                    System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                } finally {
                    dispatcher.closeConnection();
                }
            }
        });
        boot.getCore().addTelemetryListener(dispatcher);
        String bootstrapModel = System.getProperty("node.bootstrap");
        try {
            if (bootstrapModel != null) {
                dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_INFO, "Platform boot from file:" + bootstrapModel, ""));
                boot.bootstrapFromFile(new File(bootstrapModel));
            } else {
                dispatcher.notify(TelemetryEventImpl.build(finalNodeName, TelemetryEvent.Type.LOG_INFO, "Platform boot from script", ""));
                if (System.getProperty("node.script") != null) {
                    boot.bootstrapFromKevScript(new ByteArrayInputStream(System.getProperty("node.script").getBytes()));
                } else {
                    String version;
                    if (boot.getCore().getFactory().getVersion().toLowerCase().contains("snapshot")) {
                        version = "latest";
                    } else {
                        version = "release";
                    }
                    Log.info("Create minimal system with library in version {}", version);
                    boot.bootstrapFromKevScript(new ByteArrayInputStream(Bootstrap.createBootstrapScript(nodeName, version).getBytes()));
                }
            }
        } catch (Exception e) {
            ByteArrayOutputStream boo = new ByteArrayOutputStream();
            PrintStream pr = new PrintStream(boo);
            e.printStackTrace(pr);
            pr.flush();
            pr.close();
            dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.LOG_ERROR, "Error during bootstrap", new String(boo.toByteArray())));
            //e.printStackTrace();
        }
    }

    protected static void hackSystemStreams() {
        System.setOut(myOut);
        System.setErr(myErr);
    }

    protected static void restoreSystemStreams() {
        System.setOut(systemOut);
        System.setErr(systemErr);
    }

}
