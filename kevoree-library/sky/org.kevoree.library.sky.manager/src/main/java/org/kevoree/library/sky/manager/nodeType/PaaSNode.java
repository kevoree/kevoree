package org.kevoree.library.sky.manager.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/12/11
 * Time: 10:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@DictionaryType({
		@DictionaryAttribute(name = "RAM", defaultValue = "512", optional = false),
		@DictionaryAttribute(name = "CPU_LOAD", defaultValue = "N/A",optional = false),
		@DictionaryAttribute(name = "CPU", defaultValue = "1", optional = false)//,
//		@DictionaryAttribute(name = "OS", defaultValue = "N/A", vals={"N/A", "Windows 7", "Ubuntu-11.10", "BSD-9"}, optional = false)
})
public interface PaaSNode {
}
