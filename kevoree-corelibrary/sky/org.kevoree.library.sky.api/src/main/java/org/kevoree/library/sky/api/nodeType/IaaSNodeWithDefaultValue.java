package org.kevoree.library.sky.api.nodeType;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/11/12
 * Time: 14:39
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@DictionaryType({
        @DictionaryAttribute(name = "default_ARCH", defaultValue = "N/A", vals = {"i686", "x86_64"}, optional = true),
        @DictionaryAttribute(name = "default_RAM", defaultValue = "N/A", optional = true),
        // GB, MB, KB is allowed, N/A means undefined
        @DictionaryAttribute(name = "default_CPU_CORE", defaultValue = "N/A", optional = true),
        // number of allowed cores, N/A means undefined
        //@DictionaryAttribute(name = "default_CPU_FREQUENCY", defaultValue = "N/A", optional = true),
        // in MHz, N/A means undefined
        @DictionaryAttribute(name = "default_OS", defaultValue = "N/A", optional = true),
        // number of allowed cores, N/A means undefined
        @DictionaryAttribute(name = "default_DISK_SIZE", defaultValue = "N/A", optional = true)
        // the disk size allowed/available for the node (GB, MB, KB is allowed), undefined value can be set using N/A
})
public interface IaaSNodeWithDefaultValue extends IaaSNode {
}
