package org.kevoree.bootstrap;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NetworkInfo;
import org.kevoree.NetworkProperty;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.KevScriptService;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.bootstrap.kernel.KevoreeCLKernel;
import org.kevoree.bootstrap.reflect.KevoreeInjector;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.loader.XMIModelLoader;
import org.kevoree.log.Log;

import java.io.File;
import java.io.FileInputStream;
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
        core.start();
    }

    public void stop() {
        core.stop();
    }

    public void bootstrap(ContainerRoot model) {
        core.update(model, new UpdateCallback() {
            @Override
            public void run(Boolean applied) {
                Log.info("Bootstrap completed");
            }
        });
    }

    public void bootstrapFromKevScript(InputStream input) throws Exception {
        ContainerRoot emptyModel = core.getFactory().createContainerRoot();
        kevScriptEngine.executeFromStream(input, emptyModel);
        //Add network information

        ContainerNode currentNode = emptyModel.findNodesByID(core.getNodeName());
        if (currentNode != null) {
            if (currentNode.findNetworkInformationByID("ip") == null) {
                NetworkInfo info = core.getFactory().createNetworkInfo();
                info.setName("ip");
                currentNode.addNetworkInformation(info);
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface networkInterface : Collections.list(nets)) {
                    int i = 0;
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
        core.update(emptyModel, new UpdateCallback() {
            @Override
            public void run(Boolean applied) {
                Log.info("Bootstrap completed");
            }
        });
    }

    public void bootstrapFromFile(File input) throws Exception {
        FileInputStream fin = new FileInputStream(input);
        if (input.getName().endsWith(".kevs")) {
            bootstrapFromKevScript(fin);
        }
        if (input.getName().endsWith(".kev")) {
            bootstrap((ContainerRoot) xmiLoader.loadModelFromStream(fin).get(0));
        }
        if (input.getName().endsWith(".json")) {
            bootstrap((ContainerRoot) jsonLoader.loadModelFromStream(fin).get(0));
        }
        fin.close();
    }
}
