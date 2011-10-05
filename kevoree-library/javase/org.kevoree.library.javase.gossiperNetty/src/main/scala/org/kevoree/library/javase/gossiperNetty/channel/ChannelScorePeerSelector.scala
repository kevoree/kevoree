package org.kevoree.library.javase.gossiperNetty.channel

import collection.mutable.HashMap
import scala.collection.JavaConversions._
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.lang.Math
import org.slf4j.LoggerFactory
import actors.DaemonActor
import org.kevoree.library.javase.gossiperNetty.PeerSelector
import org.kevoree.ContainerNode

class ChannelScorePeerSelector (timeout: Long, modelHandlerService: KevoreeModelHandlerService, nodeName: String)
  extends PeerSelector with DaemonActor {

  private val logger = LoggerFactory.getLogger(classOf[ChannelScorePeerSelector])
  private val peerCheckMap = new HashMap[String, (Long, Int)]
  private val peerNbFailure = new HashMap[String, Int]

  case class STOP ()

  case class MODIFY_NODE_SCORE (nodeName1: String, failure: Boolean)

  case class RESET_NODE_FAILURE (nodeName1: String)

  case class SELECT_PEER (groupName: String)

  case class RESET_ALL ()

  def stop () {
    this ! STOP()
  }

  def updateNodeScore (nodeName1: String, failure: Boolean) {
    this ! MODIFY_NODE_SCORE(nodeName1, failure)
  }

  def resetAll () {
    this !? RESET_ALL()
  }

  def resetNodeFailureManagement (nodeName1: String) {
    this ! RESET_NODE_FAILURE(nodeName1)
  }

  def selectPeer (groupName: String): String = {
    (this !? SELECT_PEER(groupName)).asInstanceOf[String]
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      react {
        case STOP() => {
          this.exit()
        }
        case MODIFY_NODE_SCORE(nodeName1, failure) => this.modifyNodeScore(nodeName1, failure)
        case SELECT_PEER(groupName) => reply(this.selectPeerInternal(groupName))
        case RESET_ALL() => reset(); reply("")
        case RESET_NODE_FAILURE(nodeName1) => this.resetNodeFailure(nodeName1)
      }
    }
  }


  private def selectPeerInternal (name: String): String = {
    val model = modelHandlerService.getLastModel

    model.getHubs.find(channel => channel.getName == name) match {
      case Some(channel) => {
        //Found minima score node name
        var foundNodeName = List[String]();
        var minScore = Long.MaxValue

        model.getMBindings.filter(b => b.getHub.getName.equals(channel.getName))
          .filter(b => !b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName.equals(nodeName))
          .foreach{
          binding => {
            val subNode = binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode]
            if (getScore(subNode.getName) <= minScore) {
              minScore = getScore(subNode.getName)
            }
          }
        }
        model.getMBindings.filter(b => b.getHub.getName.equals(channel.getName))
          .filter(b => !b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName.equals(nodeName))
          .foreach{
          binding => {
            val subNode = binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode]
            if (getScore(subNode.getName) == minScore) {
              foundNodeName = foundNodeName ++ List(subNode.getName)
            }
          }
        }
        if (foundNodeName.size > 0) {
        // select randomly a peer between all potential available nodes which have a good score
        val nodeName1 = foundNodeName.get((Math.random() * foundNodeName.size).asInstanceOf[Int])

        //Init node score
        //initNodeScore(nodeName)
        modifyNodeScore(nodeName1, false)


        logger.debug("return a peer between connected nodes: " + nodeName1)
        nodeName1
        } else {
          ""
        }
      }
      case None => logger.debug(name + " not Found"); ""
    }
  }

  private def getScore (nodeName: String): Long = {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => nodeTuple._1
      case None => 0l //default
    }
  }

  /*private def initNodeScore(nodeName: String) {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => {
            peerCheckMap.put(nodeName,Tuple2(System.currentTimeMillis,nodeTuple._2+1))
      }
      case None => peerCheckMap.put(nodeName,Tuple2(System.currentTimeMillis,0))
    }
  }*/

  private def modifyNodeScore (nodeName: String, failure: Boolean) {
    if (failure) {
      logger.debug("increase node score of " + nodeName + " due to communication failure")
      peerNbFailure.get(nodeName) match {
        case Some(nodeTuple) => {
          peerNbFailure.put(nodeName, nodeTuple + 1)
          peerCheckMap.get(nodeName) match {
            case Some(nodeTuple1) => {
              peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, nodeTuple1._2 + 2 * (nodeTuple + 1)))
              logger.debug("Node score of " + nodeName + " is now " + nodeTuple + 2 * (nodeTuple + 1))
            }
            case None => peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 2)) // must not appear
          }
        }
        case None => {
          peerNbFailure.put(nodeName, 2)
          peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 2))
        }
      }
    } else {
      peerCheckMap.get(nodeName) match {
        case Some(nodeTuple) => {
          peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, nodeTuple._2 + 1))
        }
        case None => peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 0))
      }
    }
  }

  private def resetNodeFailure (nodeName: String) {
    peerNbFailure.get(nodeName) match {
      case Some(nodeTuple) => {
        peerNbFailure.put(nodeName, 0)
      }
      case None => {
        peerNbFailure.put(nodeName, 0)
      }
    }
  }

  private def reset () {
    peerCheckMap.keySet.foreach {
      nodeName =>
        peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 0))
        peerNbFailure.put(nodeName, 0)
        logger.debug("spam to say that scores are reinitiliaze")
    }
  }

}