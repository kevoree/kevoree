package org.kevoree.library.sky.libvirt;

import org.kevoree.annotation.*;
import org.kevoree.library.sky.api.nodeType.AbstractHostNode;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/11/12
 * Time: 19:03
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@NodeFragment
@DictionaryType({
		@DictionaryAttribute(name = "default_DISK", optional = false),
		@DictionaryAttribute(name = "default_COPY_MODE", vals = {"base", "clone", "as_is"}, optional = false)
})
public abstract class LibVirtNode extends AbstractHostNode {
}
