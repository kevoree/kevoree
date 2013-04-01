package org.kevoree.framework.kaspects

import org.kevoree.ComponentInstance
import org.kevoree.Port

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/03/13
 * Time: 13:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class PortAspect {
    fun isProvidedPort(port : Port) : Boolean {
        return (port.eContainer() as ComponentInstance).getProvided().contains(port)
    }

    fun isRequiredPort(port : Port) : Boolean {
        return (port.eContainer() as ComponentInstance).getRequired().contains(port)
    }

    fun isBound(port : Port) : Boolean {
        return !port.getBindings().isEmpty()
    }
}