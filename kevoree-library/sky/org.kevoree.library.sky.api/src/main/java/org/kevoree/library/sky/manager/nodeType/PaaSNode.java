package org.kevoree.library.sky.manager.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.NodeFragment;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/12/11
 * Time: 10:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@DictionaryType({
		@DictionaryAttribute(name = "ARCH", defaultValue = "x86", vals = {"x86", "x86_64"}, optional = false),
		@DictionaryAttribute(name = "RAM", defaultValue = "N/A", optional = false),
		 // GB, MB, KB is allowed, N/A means undefined
		@DictionaryAttribute(name = "CPU_FREQUENCY", defaultValue = "N/A", optional = false),
		// in MHz, N/A means undefined
//		@DictionaryAttribute(name = "CPU_CORE", defaultValue = "N/A", optional = false),
		// number of allowed cores, N/A means undefined
		@DictionaryAttribute(name = "WALLCLOCKTIME", defaultValue = "N/A", optional = false),
		// the wall time for the jail (see wikipedia for more details), N/A means undefined
		@DictionaryAttribute(name = "DATA_SIZE", defaultValue = "2GB", optional = false),
		// the data size allowed for the jail (GB, MB, KB is allowed), undefined value can be set using N/A
		@DictionaryAttribute(name = "MODE", defaultValue = "RELAX", vals = {"STRICT", "RELAX", "AVOID"}, optional = false)//,
		// how the restrictions are manage : STRICT = the jail is stopped, RELAX = the jail continue to execute, AVOID means to refused to execute something that break the limitation
//		@DictionaryAttribute(name = "OS", defaultValue = "N/A", vals={"N/A", "Windows 7", "Ubuntu-11.10", "BSD-9"}, optional = false)
})
@NodeFragment
public interface PaaSNode {
}
