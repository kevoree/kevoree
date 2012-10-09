package org.kevoree.library.sky.api

import nodeType.AbstractHostNode
import org.kevoree.ContainerRoot

import org.slf4j.{LoggerFactory, Logger}
import actors.Actor

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class KevoreeNodeManager (node: AbstractHostNode) /*extends Actor*/ {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  var runners = new scala.collection.mutable.ArrayBuffer[KevoreeNodeRunner] with scala.collection.mutable.SynchronizedBuffer[KevoreeNodeRunner]


  case class STOP ()

  case class ADD_NODE (iaasModel: ContainerRoot, targetChildName: String, targetChildModel: ContainerRoot)

  case class REMOVE_NODE (iaasModel: ContainerRoot, targetChildName: String)

  // case class UPDATE_NODE(containerNode: ContainerNode, model: ContainerRoot)

  def stop () {
    logger.debug("try to stop all nodes")
    runners.foreach {
      runner => runner.stopNode()
    }
    runners = new scala.collection.mutable.ArrayBuffer[KevoreeNodeRunner] with scala.collection.mutable.SynchronizedBuffer[KevoreeNodeRunner]
  }

  def addNode (iaasModel: ContainerRoot, targetChildName: String, targetChildModel: ContainerRoot): Boolean = {
    logger.debug("try to add a node: " + targetChildName)
    val newRunner = node.createKevoreeNodeRunner(targetChildName)
    runners.append(newRunner)

    val result = newRunner.startNode(iaasModel, targetChildModel)
    if (!result) {
      logger.error("Can't start node")
    }
    result
  }

  def removeNode (iaasModel: ContainerRoot, targetChildName: String): Boolean = {
    logger.debug("try to remove " + targetChildName)
    runners.find(runner => runner.nodeName == targetChildName) match {
      case None => true// we do nothing because there is no node with this name
      case Some(runner) => {
        runners.remove(runners.indexOf(runner))
        runner.stopNode()
      }
    }
  }

  /*
  private def updateNode (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    logger.debug("try to update " + containerNode.getName)
    runners.find(runner => runner.nodeName == containerNode.getName) match {
      case None => logger.debug(containerNode.getName + " is not available"); false
      case Some(runner) => {
        logger.debug(containerNode.getName + " is available, ask for update")

        runner.updateNode(Helper.saveModelOnFile(model))
      }
    }
  }*/
}