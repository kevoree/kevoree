package org.kevoree.library.sky.forkcloud.node;

import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.nodeType.AbstractHostNode;
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
public class ForkCloudNode extends AbstractHostNode {
    private static final Logger logger = LoggerFactory.getLogger(ForkCloudNode.class);

    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new ForkCloudNodeRunner(nodeName, this);
    }
}
