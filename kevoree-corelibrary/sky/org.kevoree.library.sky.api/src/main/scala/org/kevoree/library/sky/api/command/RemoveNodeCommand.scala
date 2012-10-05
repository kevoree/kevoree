package org.kevoree.library.sky.api.command

import org.kevoree.ContainerRoot
import org.kevoree.api.PrimitiveCommand
import org.kevoree.library.sky.api.nodeType.{AbstractHostNode, AbstractIaaSNode}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class RemoveNodeCommand (iaasModel: ContainerRoot,targetChildName : String,node: AbstractHostNode)
  extends PrimitiveCommand {

  override def execute (): Boolean = {
    node.getNodeManager.removeNode(iaasModel,targetChildName)
  }

  override def undo () {
    node.getNodeManager.addNode(iaasModel,targetChildName,iaasModel)
  }
}