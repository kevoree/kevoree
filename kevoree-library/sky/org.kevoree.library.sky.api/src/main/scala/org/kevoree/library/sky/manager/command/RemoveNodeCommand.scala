package org.kevoree.library.sky.manager.command

import org.kevoree.{ContainerRoot, ContainerNode}
import org.kevoree.api.PrimitiveCommand
import org.kevoree.library.sky.manager.nodeType.IaaSNode

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class RemoveNodeCommand (iaasModel: ContainerRoot,targetChildName : String,node: IaaSNode)
  extends PrimitiveCommand {

  override def execute (): Boolean = {
    node.getNodeManager.removeNode(iaasModel,targetChildName)
  }

  override def undo () {
    node.getNodeManager.addNode(iaasModel,targetChildName,iaasModel)
  }
}