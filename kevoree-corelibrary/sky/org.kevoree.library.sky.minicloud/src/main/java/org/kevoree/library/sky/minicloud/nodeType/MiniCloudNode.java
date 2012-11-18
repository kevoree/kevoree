package org.kevoree.library.sky.minicloud.nodeType;

import org.kevoree.annotation.*;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.nodeType.AbstractHostNode;
import org.kevoree.library.sky.api.nodeType.HostNode;
import org.kevoree.library.sky.minicloud.MiniCloudKevoreeNodeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@DictionaryType({
        @DictionaryAttribute(name = "VMARGS", optional = true)
})
@NodeType
@PrimitiveCommands(value = {
        @PrimitiveCommand(name = HostNode.ADD_NODE, maxTime = 120000)
}, values = {HostNode.REMOVE_NODE})

public class MiniCloudNode extends AbstractHostNode {
    private static final Logger logger = LoggerFactory.getLogger(MiniCloudNode.class);

    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new MiniCloudKevoreeNodeRunner(nodeName, this);
    }
}
