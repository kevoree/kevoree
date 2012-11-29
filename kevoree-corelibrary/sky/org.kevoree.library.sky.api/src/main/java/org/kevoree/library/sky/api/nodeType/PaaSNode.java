package org.kevoree.library.sky.api.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.NodeFragment;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/10/12
 * Time: 13:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@NodeFragment
@DictionaryType({
		@DictionaryAttribute(name = "WALLCLOCKTIME", defaultValue = "N/A", optional = true)
		// the wall time for the PaaS node (see wikipedia for more details: http://en.wikipedia.org/wiki/Wall_clock_time), N/A means infinite
})

public interface PaaSNode extends CloudNode {
}
