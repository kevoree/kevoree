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
class KevoreeNodeManager(node: AbstractHostNode) extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  var runners: List[KevoreeNodeRunner] = List()

  case class STOP()

  case class ADD_NODE(iaasModel: ContainerRoot, targetChildName: String, targetChildModel: ContainerRoot)

  case class REMOVE_NODE(iaasModel: ContainerRoot, targetChildName: String)

 // case class UPDATE_NODE(containerNode: ContainerNode, model: ContainerRoot)

  def stop() {
    this !? STOP()
  }

  def addNode(iaasModel: ContainerRoot, targetChildName: String, targetChildModel: ContainerRoot): Boolean = {
    (this !? ADD_NODE(iaasModel, targetChildName, targetChildModel)).asInstanceOf[Boolean]
  }

  def removeNode(iaasModel: ContainerRoot, targetChildName: String): Boolean = {
    (this !? REMOVE_NODE(iaasModel,targetChildName)).asInstanceOf[Boolean]
  }
/*
  def updateNode(containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    (this !? UPDATE_NODE(containerNode, model)).asInstanceOf[Boolean]
  }*/

  def act() {
    loop {
      react {
        case STOP() => {
          removeAllInternal()
          reply()
          this.exit()
        }
        case ADD_NODE(iaasModel, targetChildName, targetChildModel) => reply(addNodeInternal(iaasModel, targetChildName, targetChildModel))
        case REMOVE_NODE(iaasModel,targetNodeName) => reply(removeNodeInternal(iaasModel,targetNodeName))
        //case UPDATE_NODE(containerNode, model) => reply(updateNodeInternal(containerNode, model))
      }
    }
  }

  private def addNodeInternal(iaasModel: ContainerRoot, targetChildName: String, targetChildModel: ContainerRoot): Boolean = {
    logger.debug("try to add a node: " + targetChildName)
    val newRunner = node.createKevoreeNodeRunner(targetChildName)
    runners = runners ++ List(newRunner)
    val result = newRunner.startNode(iaasModel,targetChildModel)
    if (!result) {
      logger.error("Can't start node")
    }
    result
  }

  private def removeNodeInternal(iaasModel: ContainerRoot, targetChildName: String): Boolean = {
    logger.debug("try to remove " + targetChildName)
    runners.find(runner => runner.nodeName == targetChildName) match {
      case None => // we do nothing because there is no node with this name
      case Some(runner) => {
        runner.stopNode()
        runners = runners.filterNot(r => r == runner)
      }
    }
    true
  }

  private def removeAllInternal() {
    logger.debug("try to stop all nodes")
    runners.foreach {
      runner => runner.stopNode()
    }
    runners = List()
  }

  /*
  private def updateNodeInternal (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
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