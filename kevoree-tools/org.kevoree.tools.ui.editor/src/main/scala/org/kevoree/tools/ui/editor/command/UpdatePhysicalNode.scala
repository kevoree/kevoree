package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import scala.collection.JavaConversions._
import org.kevoree.{KevoreeFactory, ContainerNode}

/**
 * User: ffouquet
 * Date: 29/07/11
 * Time: 10:28
 */

class UpdatePhysicalNode extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  var targetCNode: ContainerNode = null

  def setTargetCNode(c: ContainerNode) {
    targetCNode = c
  }

  def getCurrentPhysName: String = {
    kernel.getModelHandler.getActualModel.getPhysicalNodes.find(physNode => physNode.getHosts.contains(targetCNode)) match {
      case None => ""
      case Some(physNode) => physNode.getName
    }
  }


  def execute(p: AnyRef) {
    p match {
      case physNodeName: String if (physNodeName != null && physNodeName != "") => {

        val model = kernel.getModelHandler.getActualModel
        //CLEAN PREVIOUS RELATIONSHIP
        model.getPhysicalNodes.foreach {
          physNode =>
            if (physNode.getHosts.contains(targetCNode)) {
              physNode.getHosts.remove(targetCNode)
            }
        }

        val physNode = model.getPhysicalNodes.find(physNode => physNode.getName == physNodeName) match {
          case Some(previousPhysNode) => previousPhysNode
          case None => {
            val newPhysNode = KevoreeFactory.eINSTANCE.createPhysicalNode()
            newPhysNode.setName(physNodeName)
            model.getPhysicalNodes.add(newPhysNode)
            newPhysNode
          }
        }
        if (!physNode.getHosts.contains(targetCNode)) {
          physNode.getHosts.add(targetCNode)
        }
        //CLEAN UNUSED PHYSNODE
        //WARNING COULD LEAD MAJOR PB
        val toClean = model.getPhysicalNodes.filter(physNode => physNode.getHosts.isEmpty).toList ++ List() //FORCE CLONE
        toClean.foreach {
          toClean =>
            model.getPhysicalNodes.remove(toClean)
        }

      }
      case _ =>
    }
  }

}