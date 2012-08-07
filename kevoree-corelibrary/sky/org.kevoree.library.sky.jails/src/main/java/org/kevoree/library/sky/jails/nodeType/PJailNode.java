package org.kevoree.library.sky.jails.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.api.nodeType.PaaSNode;

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
		@DictionaryAttribute(name = "archive", defaultValue = "false", vals = {"false", "true"}, optional = true),
		@DictionaryAttribute(name = "flavor", optional = true),
		@DictionaryAttribute(name = "MODE", defaultValue = "RELAX", vals = {"STRICT", "RELAX", "AVOID"}, optional = true)//,
		// how the restrictions are manage : STRICT = the jail is stopped, RELAX = the jail continue to execute, AVOID means to refused to execute something that break the limitation
		//		@DictionaryAttribute(name = "OS", defaultValue = "N/A", vals={"N/A", "Windows 7", "Ubuntu-11.10", "BSD-9"}, optional = false)
})
public class PJailNode extends JavaSENode implements PaaSNode{

}
