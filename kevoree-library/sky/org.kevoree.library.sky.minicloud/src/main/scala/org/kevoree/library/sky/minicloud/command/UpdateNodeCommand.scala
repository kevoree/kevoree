/*
package org.kevoree.library.sky.minicloud.command

import org.kevoree.{ContainerRoot, ContainerNode}
import org.kevoree.library.sky.minicloud.KevoreeNodeManager
import org.kevoree.framework.{KevoreeXmiHelper, PrimitiveCommand}
import java.io.File

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/09/11
 * Time: 13:22
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class UpdateNodeCommand (containerNode: ContainerNode, model: ContainerRoot, kevoreeNodeManager: KevoreeNodeManager)
  extends PrimitiveCommand{
  val modelBackup = System.getProperty("java.io.tmpdir") + File.separator + "bootstrap_backup" + containerNode.getName + ".kev"
  def execute (): Boolean  = {
    kevoreeNodeManager.updateNode(containerNode, model, modelBackup)
  }

  def undo () {
    val root = KevoreeXmiHelper.load(modelBackup)
    kevoreeNodeManager.updateNode(containerNode, root, modelBackup)
  }
}*/
