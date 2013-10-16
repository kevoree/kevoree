package org.kevoree.framework.kaspects

import org.kevoree.Instance
import org.kevoree.ContainerRoot
import java.util.ArrayList

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/03/13
 * Time: 15:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class ContainerRootAspect {
    fun getAllInstances(model: ContainerRoot): List<Instance> {
        val instances = ArrayList<Instance>()
        instances.addAll(model.hubs)
        instances.addAll(model.groups)
        instances.addAll(model.nodes)
        for (node in model.nodes) {
            instances.addAll(node.components)
        }
        return instances
    }
}