package org.kevoree.library.sky.virtualCloud.command

import org.kevoree.framework.PrimitiveCommand
import org.kevoree.{ContainerRoot, ContainerNode, NodeType}
import org.kevoree.library.sky.virtualCloud.KevoreeNodeManager

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class RemoveNodeCommand (containerNode: ContainerNode, model: ContainerRoot, kevoreeNodeManager: KevoreeNodeManager)
  extends PrimitiveCommand {

  override def execute (): Boolean = {
    kevoreeNodeManager.removeNode(containerNode)
  }

  override def undo () {
    kevoreeNodeManager.addNode(containerNode, model)
  }
}