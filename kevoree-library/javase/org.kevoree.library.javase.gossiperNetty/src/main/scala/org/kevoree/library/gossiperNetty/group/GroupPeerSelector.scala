package org.kevoree.library.gossiperNetty.group

import org.kevoree.ContainerRoot
import collection.mutable.HashMap
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.PeerSelector
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService

class GroupPeerSelector(timeout: Long, modelHandlerService : KevoreeModelHandlerService, nodeName : String) extends PeerSelector {

  private val peerCheckMap = new HashMap[String, Tuple2[Long, Int]]

  def selectPeer(groupName: String): String = {
		val model = modelHandlerService.getLastModel
    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        //Found minima score node name
        var foundNodeName = "";
        val minScore = Long.MaxValue
        group.getSubNodes.filter(node => !node.getName.equals(nodeName)).foreach {
          subNode => {
            if (getScore(subNode.getName) < minScore) {
              foundNodeName = subNode.getName
            }
          }
        }
        //Init node score
        initNodeScore(foundNodeName)
        foundNodeName
      }
      case None => println("GroupNot Found"); ""
    }
  }

  private def getScore(nodeName: String): Long = {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => nodeTuple._1
      case None => 0l //default
    }
  }

  private def initNodeScore(nodeName: String) {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => {
            peerCheckMap.put(nodeName,Tuple2(System.currentTimeMillis,nodeTuple._2+1))
      }
      case None => peerCheckMap.put(nodeName,Tuple2(System.currentTimeMillis,0))
    }
  }

}