package org.kevoree.framework.kaspects

import java.util.ArrayList
import org.kevoree.ComponentInstance
import org.kevoree.MBinding

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/03/13
 * Time: 13:39
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class ComponentInstanceAspect {
    fun getRelatedBindings(component: ComponentInstance): List<MBinding> {
        val mbindings = ArrayList<MBinding>()
        for (port in component.getProvided()) {
            mbindings.addAll(port.getBindings())
        }
        for (port in component.getRequired()) {
            mbindings.addAll(port.getBindings())
        }
        return mbindings
    }
}
 