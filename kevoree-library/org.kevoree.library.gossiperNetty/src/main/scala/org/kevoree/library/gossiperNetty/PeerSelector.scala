package org.kevoree.library.gossiperNetty

import org.kevoree.ContainerRoot
import collection.mutable.HashMap
import scala.collection.JavaConversions._

class PeerSelector {

  private var peerCheckMap = new HashMap[String, Tuple2[Long, Int]]

  def selectPeer(model: ContainerRoot, groupName: String, timeout: Long): String = {
    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        //Found minima score node name
        var foundNodeName = "";
        val minScore = Long.MaxValue
        group.getSubNodes.foreach {
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

  def getScore(nodeName: String): Long = {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => nodeTuple._1
      case None => 0l //default
    }
  }

  def initNodeScore(nodeName: String) {
    peerCheckMap.get(nodeName) match {
      case Some(nodeTuple) => {
            peerCheckMap.put(nodeName,Tuple2(System.currentTimeMillis,nodeTuple._2+1))
      }
      case None => peerCheckMap.put(nodeName,Tuple2(System.currentTimeMillis,0))
    }
  }

}