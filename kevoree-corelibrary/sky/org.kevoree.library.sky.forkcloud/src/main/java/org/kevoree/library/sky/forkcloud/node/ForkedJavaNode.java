package org.kevoree.library.sky.forkcloud.node;

import com.sun.akuma.Daemon;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/10/12
 * Time: 09:27
 */
@Library(name = "SKY")
@NodeType
public class ForkedJavaNode extends JavaSENode {

    private static final Logger logger = LoggerFactory.getLogger(ForkedJavaNode.class);

    @Override
    public void startNode() {

        JFrame f = new JFrame();
        f.setVisible(true);

        super.startNode();
        Daemon d = new Daemon();
        if (d.isDaemonized()) {
            try {
                d.init("/var/run/kevoree/" + System.getProperty("node.name"));
            } catch (Exception e) {
                logger.error("Error while settings the pid file ", e);
            }
        }
    }
}
