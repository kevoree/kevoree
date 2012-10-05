package org.kevoree.library.sky.api.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.NodeFragment;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/10/12
 * Time: 13:23
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@DictionaryType({
		@DictionaryAttribute(name = "inet", optional = true),
		@DictionaryAttribute(name = "subnet", optional = true),
		@DictionaryAttribute(name = "mask", optional = true),
		@DictionaryAttribute(name = "role", defaultValue = "host/container", vals = {"host", "container", "host/container"}, optional = true)
})
@NodeFragment
public interface IaaSNode extends CloudNode,HostNode {
}
