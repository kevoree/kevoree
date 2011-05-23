package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.ContainerRoot
import collection.mutable.HashMap
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.PeerSelector
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.slf4j.LoggerFactory

class StrictGroupPeerSelector (timeout: Long, modelHandlerService: KevoreeModelHandlerService, nodeName: String)
  extends PeerSelector {

  private val logger = LoggerFactory.getLogger("StrictGroupPeerSelector")

  private val peerCheckMap = new HashMap[String, (Long, Int)]

  def selectPeer (groupName: String): String = {
    logger.debug("Try to select a peer between connected nodes")
    val model = modelHandlerService.getLastModel

    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        logger.debug("group found: we now look for node on this group")
        //Found minima score node name
        var foundNodeName = "";
        val minScore = Long.MaxValue
        group.getSubNodes
          .filter(node => !node.getName.equals(nodeName))
          .filter(node => model.getNodeNetworks
          .exists(nn => nn.getInitBy.getName == nodeName && nn.getTarget.getName == node.getName))
          .foreach {
          subNode => {
            logger.debug(subNode.getName + " is one of the node  which are potentially available to do gossip")
            if (getScore(subNode.getName) < minScore) {
              foundNodeName = subNode.getName
              // TODO need to fix minScore to getScore(subNode.getName)
              logger.debug(subNode.getName +
                " is one of the node  which are potentially available to do gossip (if its score is good)")
            }
          }
        }
        //Init node score
        initNodeScore(foundNodeName)

        logger.debug("return a peer between connected nodes: " + foundNodeName)
        foundNodeName
      }
      case None => logger.debug(groupName + " Not Found"); ""
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

}