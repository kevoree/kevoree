package org.kevoree.bootstrap;

import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.KevScriptService;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.bootstrap.kernel.KevoreeCLKernel;
import org.kevoree.bootstrap.reflect.KevoreeInjector;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.modeling.api.json.JSONModelLoader;
import org.kevoree.modeling.api.xmi.XMIModelLoader;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 23:47
 */
public class Bootstrap {

    private KevoreeKernel microKernel;

    private KevoreeCoreBean core;

    private KevoreeCLKernel kernel;

    private KevoreeInjector injector;

    private KevScriptEngine kevScriptEngine;

    private XMIModelLoader xmiLoader;

    private JSONModelLoader jsonLoader;

    public KevoreeCoreBean getCore() {
        return core;
    }

    public KevoreeKernel getKernel() {
        return microKernel;
    }

    public static final String defaultNodeName = "node0";

    public static void main(String[] args) {

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        final Bootstrap boot = new Bootstrap(KevoreeKernel.self.get(), nodeName);
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
                if (System.getProperty("node.script") != null) {
                    boot.bootstrapFromKevScript(new ByteArrayInputStream(System.getProperty("node.script").getBytes()));
                } else {
                    String version;
                    if (boot.getCore().getFactory().getVersion().endsWith("SNAPSHOT")) {
                        version = "latest";
                    } else {
                        version = "release";
                    }
                    boot.bootstrapFromKevScript(new ByteArrayInputStream(createBootstrapScript(nodeName, version).getBytes()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bootstrap(KevoreeKernel k, String nodeName) {
        //First initiate Security Manager to ensure that no Kill can be called on the JVM
        System.setSecurityManager(new KevoreeSecurityManager());
        //Init all subObjects
        this.microKernel = k;
        core = new KevoreeCoreBean();

        kernel = new KevoreeCLKernel(this);
        injector = new KevoreeInjector();
        kevScriptEngine = new KevScriptEngine();
        xmiLoader = core.getFactory().createXMILoader();
        jsonLoader = core.getFactory().createJSONLoader();

        //Cross links
        core.setNodeName(nodeName);
        kernel.setNodeName(nodeName);
        kernel.setCore(core);
        injector.addService(BootstrapService.class, kernel);
        injector.addService(KevScriptService.class, kevScriptEngine);
        kernel.setInjector(injector);
        core.setBootstrapService(kernel);
        Log.info("Bootstrap Kevoree node : {}, version {}", nodeName, core.getFactory().getVersion());
        core.start();
    }

    public void stop() {
        core.stop();
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
                Log.info("Bootstrap completed");
            }
        });
    }

    public ContainerRoot initialModel() {
        if (System.getProperty("dev.target.dirs") != null) {
            ContainerRoot emptyModel = null;
            try {
                emptyModel = bootstrapFromClassPath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (DeployUnit du : emptyModel.getDeployUnits()) {
                kevScriptEngine.addIgnoreIncludeDeployUnit(du);
            }
            return emptyModel;
        } else {
            return core.getKevoreeFactory().createContainerRoot();
        }
    }


    public void bootstrapFromKevScript(InputStream input, UpdateCallback callback) throws Exception {
        //TODO perhaps not delegate load of dev classpath to system for continuous integration
        ContainerRoot emptyModel = initialModel();
        core.getFactory().root(emptyModel);
        kevScriptEngine.executeFromStream(input, emptyModel);
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
                                NetworkProperty netprop = core.getFactory().createNetworkProperty();
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
        bootstrapFromKevScript(input, new UpdateCallback() {
            @Override
            public void run(Boolean applied) {
                Log.info("Bootstrap completed");
            }
        });
    }

    public ContainerRoot bootstrapFromClassPath() {
        Object classpath = System.getProperty("dev.target.dirs");
        if (classpath != null && !classpath.equals("")) {
            ContainerRoot result = null;
            JSONModelLoader loader = core.getFactory().createJSONLoader();
            ModelCompare compare = core.getFactory().createModelCompare();
            String[] paths = classpath.toString().split(File.pathSeparator);
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                File pathP = new File(path + File.separator + "KEV-INF" + File.separator + "lib.json");
                if (pathP.exists()) {
                    Log.info("Load Bootstrap model from {}", pathP.getAbsolutePath());
                    if (result == null) {
                        FileInputStream ins = null;
                        try {
                            ins = new FileInputStream(pathP);
                            Object res = loader.loadModelFromStream(ins).get(0);
                            result = (ContainerRoot) res;
                        } catch (Exception e) {
                            e.printStackTrace();
                            //noop
                        } finally {
                            if (ins != null) {
                                try {
                                    ins.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    } else {
                        FileInputStream ins = null;
                        try {
                            ins = new FileInputStream(pathP);
                            ContainerRoot addModel = (ContainerRoot) loader.loadModelFromStream(ins).get(0);
                            compare.merge(result, addModel).applyOn(result);
                        } catch (Exception e) {
                            //noop
                        } finally {
                            if (ins != null) {
                                try {
                                    ins.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    }
                }
            }
            if (result != null) {
                return result;
            } else {
                return core.getFactory().createContainerRoot();
            }
        }
        return core.getFactory().createContainerRoot();
    }

    public void bootstrapFromFile(File input, UpdateCallback callback) throws Exception {
        FileInputStream fin = new FileInputStream(input);
        if (input.getName().endsWith(".kevs")) {
            bootstrapFromKevScript(fin, callback);
        } else {
            if (input.getName().endsWith(".kev")) {
                bootstrap((ContainerRoot) xmiLoader.loadModelFromStream(fin).get(0), callback);
            } else {
                if (input.getName().endsWith(".json")) {
                    bootstrap((ContainerRoot) jsonLoader.loadModelFromStream(fin).get(0), callback);
                } else {
                    Log.error("Can't bootstrap because no extension found for {}", input.getName());
                }
            }

        }

        fin.close();
    }

    public void bootstrapFromFile(File input) throws Exception {
        FileInputStream fin = new FileInputStream(input);
        if (input.getName().endsWith(".kevs")) {
            bootstrapFromKevScript(fin);
        } else {
            if (input.getName().endsWith(".kev")) {
                bootstrap((ContainerRoot) xmiLoader.loadModelFromStream(fin).get(0));
            } else {
                if (input.getName().endsWith(".json")) {
                    bootstrap((ContainerRoot) jsonLoader.loadModelFromStream(fin).get(0));
                } else {
                    Log.error("Can't bootstrap because no extension found for {}", input.getName());
                }
            }

        }
        fin.close();
    }

    public static String createBootstrapScript(String nodeName, String version) {
        StringBuilder buffer = new StringBuilder();
        String versionRequest;
        if (version.toLowerCase().contains("snapshot")) {
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
        return buffer.toString();
    }

}
