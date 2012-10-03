package org.kevoree.library.sky.forkcloud.node;

import com.sun.akuma.Daemon;
import com.sun.akuma.JavaVMArguments;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.sky.api.KevoreeNodeRunner;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/10/12
 * Time: 08:49
 */
public class ForkCloudNodeRunner extends KevoreeNodeRunner {

    private String childNodeName = "";
    private ForkCloudNode sourceNode = null;
    private Daemon deamon = null;

    public ForkCloudNodeRunner(String nodeName, ForkCloudNode origin, Daemon d) {
        super(nodeName);
        childNodeName = nodeName;
        sourceNode = origin;
        deamon = d;
    }

    @Override
    public boolean startNode(ContainerRoot iaasModel, ContainerRoot jailBootStrapModel) {
        try {
            JavaVMArguments argsChild = JavaVMArguments.current();
            argsChild.setSystemProperty("node.name", childNodeName);
            File tempLoader = File.createTempFile("kevtemp", "bootmodel");
            tempLoader.deleteOnExit();
            KevoreeXmiHelper.save(tempLoader.getAbsolutePath(), iaasModel);
            argsChild.setSystemProperty("node.bootstrap",tempLoader.getAbsolutePath());
            deamon.daemonize(argsChild);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean stopNode() {
        return false;
    }
}
