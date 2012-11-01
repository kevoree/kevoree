package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.KevoreeNodeRunner
import org.kevoree.ContainerRoot
import org.kevoree.library.sky.api.nodeType.AbstractHostNode

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 18:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class LibVirtKevoreeNodeRunner(nodeName : String, iaasNode: AbstractHostNode) extends KevoreeNodeRunner (nodeName)  {
  def startNode (iaasModel: ContainerRoot, childBootStrapModel: ContainerRoot) = {

    false
  }

  def stopNode () = {
    false
  }
}
