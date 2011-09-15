package org.kevoree.library.gossiperNetty.group

import collection.mutable.HashMap
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.PeerSelector
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.lang.Math
import org.slf4j.LoggerFactory
import actors.DaemonActor

class GroupPeerSelector(timeout: Long, modelHandlerService : KevoreeModelHandlerService, nodeName : String) extends PeerSelector with DaemonActor {

  private val logger = LoggerFactory.getLogger(classOf[GroupPeerSelector])
  private val peerCheckMap = new HashMap[String, (Long, Int)]
  private val peerNbFailure = new HashMap[String, Int]

  case class STOP ()

  case class MODIFY_NODE_SCORE (nodeName1: String, failure: Boolean)

  case class RESET_NODE_FAILURE (nodeName1: String)

  case class SELECT_PEER (groupName: String)

  //case class RESET_ALL ()

  def stop () {
    this ! STOP()
  }

  def modifyNodeScoreAction (nodeName1: String, failure: Boolean) {
    this ! MODIFY_NODE_SCORE(nodeName1, failure)
  }

  /*def resetAll () {
    this !? RESET_ALL()
  }*/

  def resetNodeFailureAction (nodeName1: String) {
    this ! RESET_NODE_FAILURE(nodeName1)
  }

  def selectPeer(groupName: String) : String = {
    (this !? SELECT_PEER(groupName)).asInstanceOf[String]
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      react {
        //reactWithin(timeout.longValue){
        case STOP() => {
          this.exit()
        }
        case MODIFY_NODE_SCORE(nodeName1, failure) => this.modifyNodeScore(nodeName1, failure)
        case SELECT_PEER(groupName) => reply(this.selectPeerInternal(groupName))
        //case RESET_ALL() => reset(); reply("")
        case RESET_NODE_FAILURE(nodeName1) => this.resetNodeFailure(nodeName1)
      }
    }
  }


  private def selectPeerInternal(groupName: String): String = {
		val model = modelHandlerService.getLastModel

    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        //Found minima score node name
        var foundNodeName = List[String]();
        var minScore = Long.MaxValue


        group.getSubNodes
          .filter(node => !node.getName.equals(nodeName))
          .filter(node => model.getNodeNetworks
          .exists(nn => nn.getInitBy.getName == nodeName && nn.getTarget.getName == node.getName))
          .foreach {
          subNode => {
            if (getScore(subNode.getName) <= minScore) {
              minScore = getScore(subNode.getName)
            }
          }
        }
        group.getSubNodes
          .filter(node => !node.getName.equals(nodeName))
          .filter(node => model.getNodeNetworks
          .exists(nn => nn.getInitBy.getName == nodeName && nn.getTarget.getName == node.getName))
          .foreach {
          subNode => {
            if (getScore(subNode.getName) == minScore) {
              foundNodeName = foundNodeName ++ List(subNode.getName)
            }
          }
        }
        // select randomly a peer between all potential available nodes which have a good score
        val nodeName1 = foundNodeName.get((Math.random() * foundNodeName.size).asInstanceOf[Int])

        //Init node score
        //initNodeScore(nodeName)
        modifyNodeScore(nodeName1, false)


        logger.debug("return a peer between connected nodes: " + nodeName1)
        nodeName1
      }
      case None => logger.debug(groupName + " not Found"); ""
    }
  }

  private def getScore(nodeName: String): Long = {
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

  /*private def reset () {
    peerCheckMap.keySet.foreach {
      nodeName =>
        peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 0))
        peerNbFailure.put(nodeName, 0)
        logger.info("spam to say that scores are reinitiliaze")
    }
  }*/

}