package org.kevoree.library.sky.manager.command

import org.kevoree.{ContainerRoot, ContainerNode}
import org.kevoree.library.sky.manager.KevoreeNodeManager
import org.kevoree.api.PrimitiveCommand

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class AddNodeCommand(containerNode : ContainerNode, model: ContainerRoot) extends PrimitiveCommand {
  def execute () : Boolean = {
    KevoreeNodeManager.addNode(containerNode, model)
  }

  def undo () {
    KevoreeNodeManager.removeNode(containerNode)
  }
}