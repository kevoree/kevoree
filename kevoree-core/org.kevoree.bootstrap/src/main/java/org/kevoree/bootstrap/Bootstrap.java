package org.kevoree.bootstrap;

import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.KevScriptService;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.bootstrap.kernel.KevoreeCLKernel;
import org.kevoree.bootstrap.reflect.KevoreeInjector;
import org.kevoree.compare.DefaultModelCompare;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.loader.XMIModelLoader;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.compare.ModelCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private KevoreeCoreBean core = new KevoreeCoreBean();

    private KevoreeCLKernel kernel = new KevoreeCLKernel();

    private KevoreeInjector injector = new KevoreeInjector();

    private KevScriptEngine kevScriptEngine = new KevScriptEngine();

    private XMIModelLoader xmiLoader = new XMIModelLoader();

    private JSONModelLoader jsonLoader = new JSONModelLoader();

    public KevoreeCoreBean getCore() {
        return core;
    }

    public Bootstrap(String nodeName) {
        System.setSecurityManager(new KevoreeSecurityManager());
        core.setNodeName(nodeName);
        kernel.setNodeName(nodeName);
        injector.addService(ModelService.class, core);
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
        core.update(model, callback);
    }

    public void bootstrap(ContainerRoot model) {
        bootstrap(model, new UpdateCallback() {
            @Override
            public void run(Boolean applied) {
                Log.info("Bootstrap completed");
            }
        });
    }

    public void bootstrapFromKevScript(InputStream input, UpdateCallback callback) throws Exception {
        //TODO perhaps not delegate load of dev classpath to system for continuous integration
        ContainerRoot emptyModel = bootstrapFromClassPath();
        //By default DeployUnit coming from classPath are considered as complete
        //TODO ugly hack for dev mode
        for (DeployUnit du : emptyModel.getDeployUnits()) {
            kernel.manualAttach(du, kernel.system);
            kevScriptEngine.addIgnoreIncludeDeployUnit(du);
        }

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
        core.update(emptyModel, callback);
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
        Object classpath = System.getProperty("java.class.path");
        if (classpath != null && !classpath.equals("")) {
            ContainerRoot result = null;
            JSONModelLoader loader = new JSONModelLoader();
            ModelCompare compare = new DefaultModelCompare();
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
                            result = (ContainerRoot) loader.loadModelFromStream(ins).get(0);
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
            bootstrapFromKevScript(fin,callback);
        } else {
            if (input.getName().endsWith(".kev")) {
                bootstrap((ContainerRoot) xmiLoader.loadModelFromStream(fin).get(0),callback);
            } else {
                if (input.getName().endsWith(".json")) {
                    bootstrap((ContainerRoot) jsonLoader.loadModelFromStream(fin).get(0),callback);
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
}
