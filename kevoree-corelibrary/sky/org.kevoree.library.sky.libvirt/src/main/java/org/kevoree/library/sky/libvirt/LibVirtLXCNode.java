package org.kevoree.library.sky.libvirt;

import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 15/11/12
 * Time: 08:31
 */
@Library(name = "SKY")
@NodeType
public class LibVirtLXCNode extends LibVirtNode {

    private static final Logger logger = LoggerFactory.getLogger(LibVirtKvmNode.class);

    @Override
    public void startNode () {
        try {
            connection = new Connect("lxc:///", false);
        } catch (LibvirtException e) {
            logger.error("Unable to find the lxc daemon!", e);
        }
        super.startNode();
    }

    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new LibVirtLXCKevoreeNodeRunner(nodeName, this, connection);
    }
}
