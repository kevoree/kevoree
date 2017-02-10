package org.kevoree.bootstrap;

import com.typesafe.config.Config;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NetworkInfo;
import org.kevoree.Value;
import org.kevoree.annotation.KevoreeInject;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.KevScriptService;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.api.telemetry.TelemetryListener;
import org.kevoree.bootstrap.kernel.KevoreeCLKernel;
import org.kevoree.bootstrap.reflect.Injector;
import org.kevoree.bootstrap.util.ConfigHelper;
import org.kevoree.core.KevoreeCoreBean;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.pmodeling.api.json.JSONModelLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 23:47
 */
public class Bootstrap {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String defaultNodeName = "node0";

    private KevoreeKernel microKernel;
    private KevoreeCoreBean core;
    private KevScriptEngine kevScriptEngine;
    private JSONModelLoader jsonLoader;
    private int exitId;
    private static HashMap<String, String> ctxVars = new HashMap<String, String>();
    private TelemetryListener telemetryListener;

    public Bootstrap(KevoreeKernel k, String nodeName, Config config) {
        //First initiate Security Manager to ensure that no Kill can be called on the JVM
        this.exitId = new AtomicInteger().incrementAndGet();
        System.setSecurityManager(new KevoreeSecurityManager(this.exitId));
        //Init all subObjects
        this.microKernel = k;

        // init log level
        String log = System.getProperty("log.level");
        if ("DEBUG".equalsIgnoreCase(log)) {
            Log.set(Log.LEVEL_DEBUG);
        } else if ("WARN".equalsIgnoreCase(log)) {
            Log.set(Log.LEVEL_WARN);
        } else if ("INFO".equalsIgnoreCase(log)) {
            Log.set(Log.LEVEL_INFO);
        } else if ("ERROR".equalsIgnoreCase(log)) {
            Log.set(Log.LEVEL_ERROR);
        } else if ("TRACE".equalsIgnoreCase(log)) {
            Log.set(Log.LEVEL_TRACE);
        } else if ("NONE".equalsIgnoreCase(log)) {
            Log.set(Log.LEVEL_NONE);
        } else {
            log = "INFO";
            Log.set(Log.LEVEL_INFO);
        }
        Log.info("Log level= {}", log);

        Injector injector = new Injector(KevoreeInject.class);
        KevoreeCLKernel kernel = new KevoreeCLKernel(this, injector);

        String registryUrl = "http://";
        if (config.getBoolean("registry.ssl")) {
            registryUrl = "https://";
        }
        registryUrl += config.getString("registry.host");
        if (registryUrl.endsWith("/")) {
            registryUrl = registryUrl.substring(0, registryUrl.length() - 1);
        }
        int port = config.getInt("registry.port");
        if (port != 80) {
            registryUrl += ":" + port;
        }

        kevScriptEngine = new KevScriptEngine(registryUrl);
        core = new KevoreeCoreBean(kevScriptEngine);
        core.onStop(new KevoreeCoreBean.OnStopHandler() {
            public void execute() {
                Runtime.getRuntime().exit(exitId);
            }
        });

        jsonLoader = core.getFactory().createJSONLoader();

        // cross links
        core.setNodeName(nodeName);
        kernel.setNodeName(nodeName);
        kernel.setCore(core);
        injector.register(BootstrapService.class, kernel);
        injector.register(KevScriptService.class, kevScriptEngine);
        core.setBootstrapService(kernel);
        Log.info("Starting Kevoree using version: {}", core.getFactory().getVersion());
        Log.info("Platform node name: {}", nodeName);
        core.start();
    }

    public KevoreeCoreBean getCore() {
        return core;
    }

    public KevoreeKernel getKernel() {
        return microKernel;
    }

    public KevScriptEngine getKevScriptEngine() {
        return kevScriptEngine;
    }

    protected void registerTelemetryToLogListener() {
        if (telemetryListener == null) {
            telemetryListener = new TelemetryListener() {
                @Override
                public void notify(TelemetryEvent telemetryEvent) {
                    if (telemetryEvent.type().equals("info")) {
                        Log.info("[{}] {}", telemetryEvent.origin(), telemetryEvent.message());
                    } else if (telemetryEvent.type().equals("warn")) {
                        Log.warn("[{}] {}", telemetryEvent.origin(), telemetryEvent.message());
                    } else if (telemetryEvent.type().equals("error")) {
                        Log.error("[{}] {}{}", telemetryEvent.origin(), telemetryEvent.message(), telemetryEvent.stack());
                    } else {
                        Log.debug("[{}] {}{}", telemetryEvent.origin(), telemetryEvent.message(), telemetryEvent.stack());
                    }
                }
            };
        }
        core.addTelemetryListener(telemetryListener);
    }

    protected void unregisterTelemetryToLogListener() {
        if (telemetryListener != null) {
            core.removeTelemetryListener(telemetryListener);
        }
    }

    private void checkBootstrap(boolean succeed) {
    	if (succeed) {
    		Log.info("Bootstrap succeed");
    	} else {
    		Log.info("Bootstrap failed");
    		System.exit(this.exitId);
    	}
    }

    public void bootstrap(ContainerRoot model, UpdateCallback callback) {
        ContainerRoot emptyModel = initialModel();
        core.getFactory().root(emptyModel);
        ModelCompare compare = core.getFactory().createModelCompare();
        compare.merge(emptyModel, model).applyOn(emptyModel);
        core.update(emptyModel, callback, "/");
    }

    public void bootstrap(ContainerRoot model) {
        bootstrap(model, new UpdateCallback() {
			
			@Override
			public void run(Boolean applied) {
				checkBootstrap(applied);
			}
		});
    }

    private ContainerRoot initialModel() {
        return core.getKevoreeFactory().createContainerRoot();
    }


    public void bootstrapFromKevScript(InputStream input, UpdateCallback callback) throws Exception {
    	if (callback == null) {
            callback = new UpdateCallback() {
				
				@Override
				public void run(Boolean applied) {
					checkBootstrap(applied);
				}
			};
    	}

        ContainerRoot emptyModel = initialModel();
        core.getFactory().root(emptyModel);
        for (ClassLoader cl : microKernel.getClassLoaders()) {
            InputStream is = cl.getResourceAsStream("KEV-INF/kevlib.json");
            if (is != null) {
                try {
                    ContainerRoot CLroot = (ContainerRoot) core.getFactory().createJSONLoader().loadModelFromStream(is).get(0);
                    core.getFactory().root(CLroot);
                    core.getFactory().createModelCompare().merge(emptyModel, CLroot).applyOn(emptyModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        kevScriptEngine.executeFromStream(input, emptyModel, ctxVars);
        //Add network information
        ContainerNode currentNode = emptyModel.findNodesByID(core.getNodeName());
        if (currentNode != null) {
            if (currentNode.findNetworkInformationByID("ip") == null) {
                NetworkInfo info = core.getFactory().createNetworkInfo();
                info.setName("ip");
                currentNode.addNetworkInformation(info);
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                int i = 0;
                for (NetworkInterface networkInterface : Collections.list(nets)) {
                    if (networkInterface.isUp()) {
                        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                            if (!inetAddress.isLoopbackAddress()) {
                                Value netprop = core.getFactory().createValue();
                                netprop.setName(i + "_" + networkInterface.getName());
                                netprop.setValue(inetAddress.getHostAddress());
                                info.addValues(netprop);
                                i++;
                            }
                        }
                    }
                }
            }
        }
        core.update(emptyModel, callback, "/");
    }

    public void bootstrapFromKevScript(InputStream input) throws Exception {
    	this.bootstrapFromKevScript(input, null);
    }

    public void bootstrapFromFile(File input, UpdateCallback callback) throws Exception {
        FileInputStream fin = new FileInputStream(input);
        if (input.getName().endsWith(".kevs")) {
            bootstrapFromKevScript(fin, callback);
        } else {
        	if (input.getName().endsWith(".json")) {
                bootstrap((ContainerRoot) jsonLoader.loadModelFromStream(fin).get(0), callback);
            } else {
                Log.error("Unable to bootstrap from file. Extension should be .kevs or .json (current: {})", input.getName().substring(input.getName().lastIndexOf(".")));
            }
        }

        fin.close();
    }

    public void bootstrapFromFile(File input) throws Exception {
    	this.bootstrapFromFile(input, null);
    }

    public void stop() {
        try {
            System.out.println(); // give some space if ^C is in the terminal
            if (core.isStarted()) {
                core.stop();
            }
            Log.info("Stopped.");
        } catch (Throwable ex) {
            System.out.println("Error while stopping kevoree platform: " + ex.getMessage());
        }
    }

    public static String createBootstrapScript(String nodeName, String version) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("add node0: JavaNode\n".replace("node0", nodeName));
        buffer.append("add sync: WSGroup\n");
        buffer.append("attach node0 sync\n".replace("node0", nodeName));
        return buffer.toString();
    }

    private static String shortId() {
        final StringBuilder builder = new StringBuilder();
        final Random random = new Random();
        for (int i = 0; i < 9; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        Pattern p = Pattern.compile("(%(%([a-zA-Z0-9_]+)%)%)");
        Matcher m = p.matcher(nodeName);
        while (m.find()) {
            nodeName = shortId();
            ctxVars.put(m.group(3), nodeName);
        }

        final Bootstrap boot = new Bootstrap(KevoreeKernel.self.get(), nodeName, ConfigHelper.get());
        boot.registerTelemetryToLogListener();
        if (boot.getKernel() == null) {
            throw new Exception("Kevoree as not be started from KCL microkernel context");
        }

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                Thread.currentThread().setContextClassLoader(loader);
                boot.stop();
            }
        });

        String bootstrapModel = System.getProperty("node.bootstrap");
        try {
            if (bootstrapModel != null) {
                boot.bootstrapFromFile(new File(bootstrapModel));
            } else {
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
                    boot.bootstrapFromKevScript(new ByteArrayInputStream(createBootstrapScript(nodeName, version).getBytes()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
