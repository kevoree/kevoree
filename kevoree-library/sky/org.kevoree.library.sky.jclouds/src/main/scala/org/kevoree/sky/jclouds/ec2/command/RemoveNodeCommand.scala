package org.kevoree.sky.jclouds.ec2.command

import org.kevoree.framework.PrimitiveCommand
import org.kevoree.{ContainerRoot, ContainerNode}
import org.kevoree.library.sky.virtualCloud.KevoreeNodeManager

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 15:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class RemoveNodeCommand(containerNode : ContainerNode, model: ContainerRoot, kevoreeNodeManager: KevoreeNodeManager) extends PrimitiveCommand {
  override def execute (): Boolean = {

    true
  }

  override def undo () {

  }
}