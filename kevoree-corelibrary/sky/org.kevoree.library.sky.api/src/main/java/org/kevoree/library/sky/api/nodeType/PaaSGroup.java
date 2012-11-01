package org.kevoree.library.sky.api.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 01/11/12
 * Time: 19:06
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
//@GroupFragment // TODO use it when nature will be defined and managed on the model
@DictionaryType({
		@DictionaryAttribute(name = "masterNode", optional = false),
		@DictionaryAttribute(name = "port", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true)
})
public interface PaaSGroup {
}
