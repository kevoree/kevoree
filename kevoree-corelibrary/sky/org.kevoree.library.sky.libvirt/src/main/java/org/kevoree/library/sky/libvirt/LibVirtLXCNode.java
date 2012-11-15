package org.kevoree.library.sky.libvirt;

import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.KevoreeNodeRunner;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 15/11/12
 * Time: 08:31
 */
@Library(name = "SKY")
@NodeType
public class LibVirtLXCNode extends LibVirtNode {
    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new LibVirtLXCKevoreeNodeRunner(nodeName, this, connection);
    }
}
