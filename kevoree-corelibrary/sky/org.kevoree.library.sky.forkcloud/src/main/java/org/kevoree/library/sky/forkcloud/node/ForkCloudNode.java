package org.kevoree.library.sky.forkcloud.node;

import com.sun.akuma.Daemon;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.nodeType.IaaSNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/10/12
 * Time: 08:49
 */
@Library(name = "SKY")
@NodeType
public class ForkCloudNode extends IaaSNode {
    private static final Logger logger = LoggerFactory.getLogger(ForkCloudNode.class);

    Daemon d = new Daemon();

    @Override
    public void startNode() {
        super.startNode();
        if (d.isDaemonized()) {
            try {
                d.init("/var/run/kevoree/"+System.getProperty("node.name"));
            } catch (Exception e) {
                logger.error("Error while settings the pid file ", e);
            }
        }
    }

    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new ForkCloudNodeRunner(nodeName, this, d);
    }
}
