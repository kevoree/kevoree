package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/05/11
 * Time: 13:35
 */

object PeersHelper {

  def getPeers (model: ContainerRoot, groupName: String, nodeName: String): java.util.List[String] = {
    model.getGroups.find(group => group.getName == groupName) match {
      case Some(group) => {
        //Found minima score node name
        var nodes = List[String]()
        group.getSubNodes
          .filter(node => !node.getName.equals(nodeName))
          .filter(node => model.getNodeNetworks
          .exists(nn => nn.getInitBy.getName == nodeName && nn.getTarget.getName == node)
                 )
          .foreach {
          subNode => {
            nodes = nodes ++ List(subNode.getName)
          }
        }
        nodes
      }
      case None => println("GroupNot Found"); List()
    }
  }
}