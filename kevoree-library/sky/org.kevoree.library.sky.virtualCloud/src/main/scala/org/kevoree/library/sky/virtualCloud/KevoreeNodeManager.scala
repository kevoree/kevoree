package org.kevoree.library.sky.virtualCloud

import actors.DaemonActor
import org.kevoree.framework.{Constants, KevoreePlatformHelper}
import org.kevoree.{ContainerRoot, ContainerNode}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class KevoreeNodeManager extends DaemonActor {

  private var runnners: List[KevoreeNodeRunner] = List()

  start()

  case class STOP ()

  case class ADD_NODE (containerNode: ContainerNode, model: ContainerRoot)

  case class REMOVE_NODE (containerNode: ContainerNode)

  def stop () {
    this ! STOP()
  }

  def addNode (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    (this !? ADD_NODE(containerNode, model)).asInstanceOf[Boolean]
  }

  def removeNode (containerNode: ContainerNode): Boolean = {
    (this ! REMOVE_NODE(containerNode)).asInstanceOf[Boolean]
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
      }
    }
  }

  private def addNodeInternal (containerNode: ContainerNode, model: ContainerRoot): Boolean = {
    val port = KevoreePlatformHelper
      .getProperty(model, containerNode.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT);
    val portint = Integer.parseInt(port)

    val newRunner = new KevoreeNodeRunner(containerNode.getName, portint, ModelManager.saveModelOnFile(model))
    val result = newRunner.startNode()
    if (result) {
      runnners = runnners :+ newRunner
    }
    result
  }

  private def removeNodeInternal (containerNode: ContainerNode): Boolean = {
    runnners.find(runner => runner.nodeName == containerNode.getName) match {
      case None => // we do nothing because there is no node with this name
      case Some(runner) => runner.stopKillNode()
    }
    true
  }

  private def removeAllInternal () {
    runnners.foreach {
      runner => runner.stopKillNode()
    }
  }
}