package org.kevoree.library.sky.minicloud.command

import org.kevoree.framework.PrimitiveCommand
import org.kevoree.{ContainerRoot, ContainerNode, NodeType}
import org.kevoree.library.sky.minicloud.KevoreeNodeManager

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class AddNodeCommand(containerNode : ContainerNode, model: ContainerRoot, kevoreeNodeManager: KevoreeNodeManager) extends PrimitiveCommand {
  def execute () : Boolean = {
    kevoreeNodeManager.addNode(containerNode, model)
  }

  def undo () {
    kevoreeNodeManager.removeNode(containerNode)
  }
}