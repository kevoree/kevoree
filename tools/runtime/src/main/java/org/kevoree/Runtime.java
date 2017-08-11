package org.kevoree;

import org.kevoree.annotation.KevoreeInject;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.core.KevoreeCore;
import org.kevoree.core.KevoreeCoreImpl;
import org.kevoree.core.KevoreeDeployException;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.json.JSONModelLoader;
import org.kevoree.reflect.Injector;
import org.kevoree.resolver.MavenResolverException;
import org.kevoree.service.KevScriptService;
import org.kevoree.service.RuntimeService;
import org.kevoree.tools.KevoreeConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 23:47
 */
public class Runtime {

    private static final Path CONF_FILE = Paths.get(System.getProperty("user.home"), ".kevoree", "config.json");
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


    private Injector injector;
    private KevoreeCore core;
    private JSONModelLoader jsonLoader;
    private static HashMap<String, String> ctxVars = new HashMap<>();
    private boolean stopping = false;
    private Exception bootstrapError = null;

    public Runtime() {
        this(System.getProperty("node.name", "node0"));
    }

    public Runtime(String nodeName) {
        this(nodeName, new KevoreeConfig.Builder()
                .useDefault()
                .useFile(CONF_FILE)
                .useSystemProperties()
                .build());
    }

    public Runtime(String nodeName, KevoreeConfig config) {
        // First initiate Security Manager to ensure that no kill can be called on the JVM
        System.setSecurityManager(new KevoreeSecurityManager());

        java.lang.Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                if (core.isStarted()) {
                    stopping = true;
                    Runtime.this.stop();
                }
            }
        });

        Pattern p = Pattern.compile("(%(%([a-zA-Z0-9_]+)%)%)");
        Matcher m = p.matcher(nodeName);
        while (m.find()) {
            nodeName = shortId();
            ctxVars.put(m.group(3), nodeName);
        }

        // init log level
        String log = "INFO";
        if (config.hasPath("log.level")) {
            log = config.getString("log.level");
        }
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

        // Create Kevoree core
        core = new KevoreeCoreImpl();

        try {
            // Services injection
            injector = new Injector(KevoreeInject.class);
            injector.register(KevScriptService.class, new KevScriptEngine(config));
            injector.register(RuntimeService.class, new MavenRuntimeService(core, injector));
        } catch (MavenResolverException e) {
            Log.error("Unable to create the RuntimeService", e);
            return;
        }

        try {
            // inject services in core
            injector.inject(core);
            jsonLoader = core.getFactory().createJSONLoader();
            core.onStop(() -> {
                Log.info("Stopped.");
                if (!stopping) {
                    System.setSecurityManager(null);
                    java.lang.Runtime.getRuntime().exit(0);
                }
            });
            core.setNodeName(nodeName);
            Log.info("Starting Kevoree using version: {} [PID:{}]", core.getFactory().getVersion(), ManagementFactory.getRuntimeMXBean().getName());
            Log.info("Platform node name: {}", nodeName);
            core.start();
        } catch (KevoreeCoreException e) {
            Log.error("Unable to inject services in KevoreeCore", e);
        }
    }

    public Injector getInjector() {
        return this.injector;
    }

    private void checkBootstrap(Exception e) {
    	if (e == null) {
    		Log.info("Bootstrap succeed");
    	} else {
    		Log.error("Bootstrap failed");
    	}
    }

    public void bootstrap() throws Exception {
        this.bootstrap(this::checkBootstrap);
    }

    public void bootstrap(UpdateCallback callback) throws Exception {
        // this is questionable whether or not to default to System.getProperty here
        // or to give this work to the caller: for now I'll let it as is
        String bootstrapModel = System.getProperty("node.bootstrap");
        if (bootstrapModel != null) {
            Log.debug("Runtime using -Dnode.bootstrap={}", bootstrapModel);
            this.bootstrapFromFile(new File(bootstrapModel), callback);
        } else {
            String script = createBootstrapScript(core.getNodeName());
            Log.debug("No bootstrap model given, using default:\n{}", script);
            this.bootstrapFromKevScript(new ByteArrayInputStream(script.getBytes()), callback);
        }
    }

    public void bootstrap(ContainerRoot model, UpdateCallback callback) throws Exception {
        ContainerNode currentNode = model.findNodesByID(core.getNodeName());
        if (currentNode != null) {
            if (currentNode.findNetworkInformationByID("ip") == null) {
                // Add network information
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

            if (core.isStarted()) {
                final CountDownLatch latch = new CountDownLatch(1);
                core.update(model, UUID.randomUUID(), (e) -> {
                    bootstrapError = e;
                    latch.countDown();
                }, "/");
                latch.await();
                if (bootstrapError != null) {
                    stop();
                }
                callback.run(bootstrapError);
            } else {
                Log.error("KevoreeCore has been stopped while bootstrapping");
                System.setSecurityManager(null);
            }
        } else {
            throw new KevoreeDeployException("Unable to find node \""+core.getNodeName()+"\" in given model");
        }
    }

    public void bootstrap(ContainerRoot model) throws Exception {
        this.bootstrap(model, this::checkBootstrap);
    }

    public void bootstrapFromKevScript(InputStream input, UpdateCallback callback) throws Exception {
    	if (callback == null) {
            callback = this::checkBootstrap;
    	}

        ContainerRoot emptyModel = core.getFactory().createContainerRoot();
        core.getFactory().root(emptyModel);

        try {
            injector.get(KevScriptService.class).executeFromStream(input, emptyModel, ctxVars);
        } catch (KevScriptException e) {
            Log.error("Unable to bootstrap Kevoree from this KevScript", e);
            stop();
        }

        this.bootstrap(emptyModel, callback);
    }

    public void bootstrapFromKevScript(InputStream input) throws Exception {
    	this.bootstrapFromKevScript(input, null);
    }

    public void bootstrapFromModel(InputStream input) throws Exception {
        this.bootstrapFromModel(input, this::checkBootstrap);
    }

    public void bootstrapFromModel(InputStream input, UpdateCallback callback) throws Exception {
        List<KMFContainer> models;

        try {
            models = jsonLoader.loadModelFromStream(input);
        } catch (Exception e) {
            Log.error("Unable to load given JSON", e);
            stop();
            callback.run(e);
            return;
        }

        if (models != null) {
            ContainerRoot model = (ContainerRoot) models.get(0);
            if (model != null) {
                this.bootstrap(model, callback);
            } else {
                throw new KevoreeDeployException("Model is null");
            }
        } else {
            callback.run(new KevoreeDeployException("Empty model"));
            stop();
        }
    }

    public void bootstrapFromFile(File input, UpdateCallback callback) throws Exception {
        FileInputStream fin = new FileInputStream(input);
        if (input.getName().endsWith(".kevs")) {
            bootstrapFromKevScript(fin, callback);
        } else {
        	if (input.getName().endsWith(".json")) {
        	    bootstrapFromModel(fin, callback);
            } else {
                Log.error("Bootstrap model should be .kevs or .json (current: {})", input.getName().substring(input.getName().lastIndexOf(".")));
            }
        }
        fin.close();
    }

    public void bootstrapFromFile(File input) throws Exception {
    	this.bootstrapFromFile(input, null);
    }

    public void stop() {
        try {
            if (core.isStarted()) {
                core.stop();
            } else {
                System.setSecurityManager(null);
            }
        } catch (Throwable ex) {
            Log.error("Error while stopping core", ex);
            if (!stopping) {
                System.setSecurityManager(null);
                java.lang.Runtime.getRuntime().exit(0);
            }
        }
    }

    private String createBootstrapScript(String nodeName) {
        return "add node0: JavaNode\n".replace("node0", nodeName) +
                "add sync: WSGroup\n" +
                "attach node0 sync\n".replace("node0", nodeName);
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
        //System.setProperty("node.bootstrap", "/home/leiko/dev/kevoree/kevoree/tools/runtime/src/test/resources/script0.kevs");
        String nodeName = System.getProperty("node.name", "node0");
        Runtime runtime = new Runtime(nodeName);
        runtime.bootstrap();
    }
}
