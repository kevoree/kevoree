package org.kevoree.library.sky.virtualCloud

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
class KevoreeNodeManager (node: VirtualCloudNode) extends DaemonActor {
  private final val logger: Logger = LoggerFactory.getLogger(classOf[VirtualCloudNode])

  private var runnners: List[KevoreeNodeRunner] = List()

  def getRunners = runnners

  start()

  case class STOP ()

  case class ADD_NODE (containerNode: ContainerNode, model: ContainerRoot)

  case class REMOVE_NODE (containerNode: ContainerNode)

  case class UPDATE_NODE (containerNode: ContainerNode, model: ContainerRoot, modelBackup: String)

  def stop () {
    this ! STOP()
  }

  def addNode (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    (this !? ADD_NODE(containerNode, model)).asInstanceOf[Boolean]
  }

  def removeNode (containerNode: ContainerNode): Boolean = {
    (this !? REMOVE_NODE(containerNode)).asInstanceOf[Boolean]
  }

  def updateNode (containerNode: ContainerNode, model: ContainerRoot, modelBackup: String): Boolean = {
    (this !? UPDATE_NODE(containerNode, model, modelBackup)).asInstanceOf[Boolean]
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
        case UPDATE_NODE(containerNode, model, modelBackup) => reply(updateNodeInternal(containerNode, model,
                                                                                         modelBackup))
      }
    }
  }

  private def addNodeInternal (containerNode: ContainerNode, model: ContainerRoot): Boolean = {

    val newRunner = new KevoreeNodeRunner(containerNode.getName, Helper.saveModelOnFile(model))
    val result = newRunner.startNode()
    if (result) {
      runnners = runnners ++ List(newRunner)
    } else {
      logger.error("Can't start node")
    }
    result
  }

  private def removeNodeInternal (containerNode: ContainerNode): Boolean = {
    logger.debug("try to remove " + containerNode.getName)
    runnners.find(runner => runner.nodeName == containerNode.getName) match {
      case None => // we do nothing because there is no node with this name
      case Some(runner) => {
        runner.stopKillNode()
        runnners = runnners.filterNot(r => r == runner)
      }
    }
    true
  }

  private def removeAllInternal () {
    logger.debug("try to stop all nodes")
    runnners.foreach {
      runner => runner.stopKillNode()
    }
    runnners = List()
  }

  private def updateNodeInternal (containerNode: ContainerNode, model: ContainerRoot, modelBackup: String): Boolean = {
    logger.debug("try to update " + containerNode.getName)
    runnners.find(runner => runner.nodeName == containerNode.getName) match {
      case None => logger.debug(containerNode.getName + " is not available"); false
      case Some(runner) => {
        logger.debug(containerNode.getName + " is available, ask for update")

        runner.updateNode(Helper.saveModelOnFile(model), modelBackup)
      }
    }
  }
}