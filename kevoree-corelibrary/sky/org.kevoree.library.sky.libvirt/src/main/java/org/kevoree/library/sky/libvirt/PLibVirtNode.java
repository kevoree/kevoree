package org.kevoree.library.sky.libvirt;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.nodeType.PJavaSENode;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 18:55
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "SKY")
@NodeType
@DictionaryType({
		@DictionaryAttribute(name = "disk", optional = false)
})
public class PLibVirtNode extends PJavaSENode{
}
