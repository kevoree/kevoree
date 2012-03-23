package org.kevoree.library.javase.system.bsd;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.system.AbstractSystemCommandComponentType;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/03/12
 * Time: 11:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "System")
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "START_COMMAND", defaultValue = "pkg_add -r git"),
		@DictionaryAttribute(name = "STOP_COMMAND", defaultValue = "sh -c \"pkg_delete `pkg_info | grep git | sed -e 's/ .*//g'`\"")
})
public class Git extends AbstractSystemCommandComponentType {
}
