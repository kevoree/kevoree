package org.kevoree.library.sky.libvirt;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.nodeType.AbstractHostNode;
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
@NodeType
@DictionaryType({
		@DictionaryAttribute(name = "default_disk", optional = false)
})
public class LibVirtNode extends AbstractHostNode {
	private static final Logger logger = LoggerFactory.getLogger(LibVirtNode.class);

    @Override
    public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
        return new LibVirtKevoreeNodeRunner(nodeName,this);
    }
}
