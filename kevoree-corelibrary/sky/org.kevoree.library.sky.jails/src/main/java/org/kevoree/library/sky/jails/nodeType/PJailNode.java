package org.kevoree.library.sky.jails.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.library.sky.api.nodeType.PJavaSENode;

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
		@DictionaryAttribute(name = "flavor", optional = true)

})
public class PJailNode extends PJavaSENode {

}
