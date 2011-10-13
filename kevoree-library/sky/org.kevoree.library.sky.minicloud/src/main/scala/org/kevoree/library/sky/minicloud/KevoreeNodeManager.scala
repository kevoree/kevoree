package org.kevoree.library.sky.minicloud

import actors.DaemonActor
import org.kevoree.{ContainerRoot, ContainerNode}

import org.slf4j.{LoggerFactory, Logger}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object KevoreeNodeManager extends DaemonActor {
  private val logger: Logger = LoggerFactory.getLogger(KevoreeNodeManager.getClass)
  private var node : MiniCloudNode = null

  def setNode(n : MiniCloudNode) {
    node = n
  }

  var runners: List[KevoreeNodeRunner] = List()

  start()

  case class STOP ()

  case class ADD_NODE (containerNode: ContainerNode, model: ContainerRoot)

  case class REMOVE_NODE (containerNode: ContainerNode)

  case class UPDATE_NODE (containerNode: ContainerNode, model: ContainerRoot)

  def stop () {
    this ! STOP()
  }

  def addNode (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    (this !? ADD_NODE(containerNode, model)).asInstanceOf[Boolean]
  }

  def removeNode (containerNode: ContainerNode): Boolean = {
    (this !? REMOVE_NODE(containerNode)).asInstanceOf[Boolean]
  }

  def updateNode (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    (this !? UPDATE_NODE(containerNode, model)).asInstanceOf[Boolean]
  }

  def act () {
    loop {
      react {
        case STOP() => {
          removeAllInternal()
          this.exit()
        }
        case ADD_NODE(containerNode, model) => reply(addNodeInternal(containerNode, model))
        case REMOVE_NODE(containerNode) => reply(removeNodeInternal(containerNode))
        case UPDATE_NODE(containerNode, model) => reply(updateNodeInternal(containerNode, model))
      }
    }
  }

  private def addNodeInternal (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    logger.debug("try to add a node: " + containerNode.getName)
    val newRunner = new KevoreeNodeRunner(containerNode.getName, Helper.saveModelOnFile(model))
    val result = newRunner.startNode()
    if (result) {
      runners = runners ++ List(newRunner)
    } else {
      logger.error("Can't start node")
    }
    result
  }

  private def removeNodeInternal (containerNode: ContainerNode): Boolean = {
    logger.debug("try to remove " + containerNode.getName)
    runners.find(runner => runner.nodeName == containerNode.getName) match {
      case None => // we do nothing because there is no node with this name
      case Some(runner) => {
        runner.stopKillNode()
        runners = runners.filterNot(r => r == runner)
      }
    }
    true
  }

  private def removeAllInternal () {
    logger.debug("try to stop all nodes")
    runners.foreach {
      runner => runner.stopKillNode()
    }
    runners = List()
  }

  private def updateNodeInternal (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    logger.debug("try to update " + containerNode.getName)
    runners.find(runner => {println(runner.nodeName);runner.nodeName == containerNode.getName}) match {
      case None => logger.debug(containerNode.getName + " is not available"); false
      case Some(runner) => {
        logger.debug(containerNode.getName + " is available, ask for update")

        runner.updateNode(Helper.saveModelOnFile(model))
      }
    }
  }
}