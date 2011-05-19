package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/05/11
 * Time: 13:35
 */

object PeersHelper {

  private val logger = LoggerFactory.getLogger("PeersHelper")

  def getPeers (model: ContainerRoot, groupName: String, nodeName: String): java.util.List[String] = {
    logger.debug("try to get all connected peers for the group: " + groupName)
    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        //Found minima score node name
        var nodes = List[String]()
        group.getSubNodes
          .filter(node => !node.getName.equals(nodeName))
          .filter(node => model.getNodeNetworks
          .exists(nn => nn.getInitBy.getName == nodeName && nn.getTarget.getName == node.getName))
          .foreach {
          subNode => {
            nodes = nodes ++ List(subNode.getName)
          }
        }
        logger.debug("return a list of connected peers: size = " + nodes.size)
        nodes
      }
      case None => println("GroupNot Found"); List()
    }
  }
}