package org.kevoree.experiment.library.gossiperNetty

import collection.mutable.HashMap
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.PeerSelector
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.slf4j.LoggerFactory
import actors.DaemonActor

class StrictGroupPeerSelector (timeout: Long, modelHandlerService: KevoreeModelHandlerService, nodeName: String)
  extends PeerSelector with DaemonActor {

  private val logger = LoggerFactory.getLogger(classOf[StrictGroupPeerSelector])

  private val peerCheckMap = new HashMap[String, (Long, Int)]
  private val peerNbFailure = new HashMap[String, Int]

  this.start()


  case class STOP ()
  case class MODIFY_NODE_SCORE(nodeName : String)
  case class RESET_NODE_FAILURE(nodeName : String)

  def stop() {
    this ! STOP()
  }

  def modifyNodeScoreAction(nodeName : String) {
    this ! MODIFY_NODE_SCORE(nodeName)
  }

  def resetNodeFailureAction(nodeName : String) {
    this ! RESET_NODE_FAILURE(nodeName)
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      react {
        //reactWithin(timeout.longValue){
        case STOP() => {
          this.exit()
        }
        case MODIFY_NODE_SCORE(nodeName) => {
          this.modifyNodeScore(nodeName)
        }
        case RESET_NODE_FAILURE(nodeName) => this.resetNodeFailure(nodeName)
      }
    }
  }

  def selectPeer (groupName: String): String = {
    logger.debug("Try to select a peer between connected nodes.")
    val model = modelHandlerService.getLastModel

    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        //logger.debug("group found: we now look for node on this group")
        //Found minima score node name
        var foundNodeName = "";
        var minScore = Long.MaxValue


        group.getSubNodes
          .filter(node => !node.getName.equals(nodeName))
          .filter(node => model.getNodeNetworks
          .exists(nn => nn.getInitBy.getName == nodeName && nn.getTarget.getName == node.getName))
          .foreach {
          subNode => {
            //logger.debug(subNode.getName + " is one of the node which are potentially available to do gossip")
            if (getScore(subNode.getName) < minScore) {
              foundNodeName = subNode.getName
              minScore = getScore(subNode.getName)
              //              logger.debug(subNode.getName +
              //                " is one of the node  which are potentially available to do gossip (if its score is good)")
            }
          }
        }
        //Init node score
        initNodeScore(foundNodeName)

        logger.debug("return a peer between connected nodes: " + foundNodeName)
        foundNodeName
      }
      case None => logger.debug(groupName + " not Found"); ""
    }
  }

  private def getScore (nodeName: String): Long = {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => nodeTuple._1
      case None => 0l //default
    }
  }

  private def initNodeScore (nodeName: String) {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => {
        peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, nodeTuple._2 + 1))
      }
      case None => peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 0))
    }
  }

  private def modifyNodeScore (nodeName: String) {
    logger.debug("increase node score of " + nodeName + " due to communication failure")
    peerNbFailure.get(nodeName) match {
      case Some(nodeTuple) => {
        peerNbFailure.put(nodeName, nodeTuple + 1)
        peerCheckMap.get(nodeName) match {
          case Some(nodeTuple1) => {
            peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, nodeTuple1._2 + 2 * (nodeTuple +1)))
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
  }

  private def resetNodeFailure(nodeName : String) {
    peerNbFailure.get(nodeName) match {
      case Some(nodeTuple) => {
        peerNbFailure.put(nodeName, 0)
      }
      case None => {
        peerNbFailure.put(nodeName, 0)
      }
    }
  }

}