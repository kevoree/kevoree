package org.kevoree.library.sky.forkcloud.node;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.skife.gressil.Daemon;
import org.skife.gressil.Status;

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
    Daemon daemon = null;
    Status status = null;

    public ForkCloudNodeRunner(String nodeName, ForkCloudNode origin) {
        super(nodeName);
        childNodeName = nodeName;
        sourceNode = origin;
        daemon = new Daemon();
    }

    @Override
    public boolean startNode(ContainerRoot iaasModel, ContainerRoot jailBootStrapModel) {
        try {
            // argsChild.setSystemProperty("node.name", childNodeName);
            File tempLoader = File.createTempFile("kevtemp", "bootmodel");
            tempLoader.deleteOnExit();
            KevoreeXmiHelper.save(tempLoader.getAbsolutePath(), iaasModel);
            // argsChild.setSystemProperty("node.bootstrap", tempLoader.getAbsolutePath());

            //PID = daemon.daemonize(argsChild);

            status = daemon.forkish();

            System.out.println("Yop");


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean stopNode() {
        //daemon.kill(PID, 2);
        return true;
    }
}
