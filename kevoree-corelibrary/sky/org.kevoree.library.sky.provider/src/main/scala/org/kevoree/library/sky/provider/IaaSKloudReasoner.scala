package org.kevoree.library.sky.provider

import org.kevoree.{NodeType, ContainerRoot}
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.library.sky.api.helper.{KloudNetworkHelper, KloudModelHelper}
import org.kevoree.framework.Constants
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/11/12
 * Time: 11:44
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object IaaSKloudReasoner {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def configureChildNodes (kloudModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
      // count current child for each Parent nodes
      val parents = countChilds(kloudModel)

      var min = Int.MaxValue
      var potentialParents = List[String]()

      // filter nodes that are not IaaSNode and are not child of IaaSNode
      kloudModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(kloudModel, n.getName)
        && kloudModel.getNodes.forall(parent => !parent.getHosts.contains(n))).foreach {
        node => {
          logger.debug("try to select a parent for {}", node.getName)
          // select a host for each user node
          if (potentialParents.isEmpty) {
            min = Int.MaxValue

            parents.foreach {
              parent => {
                if (parent._2 < min) {
                  min = parent._2
                }
              }
            }
            parents.foreach {
              parent => {
                if (parent._2 <= min) {
                  potentialParents = potentialParents ++ List(parent._1)
                }
              }
            }
          }
          kengine.addVariable("nodeName", node.getName)
          val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
          kengine.addVariable("parentName", potentialParents(index))
          kengine append "addChild {nodeName}@{parentName}"

          // define IP using selecting node to know what it the network used in this machine
          val ipOption = KloudNetworkHelper.selectIP(potentialParents(index), kloudModel)
          if (ipOption.isDefined) {
            kengine.addVariable("ipKey", Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
            kengine.addVariable("ip", ipOption.get)
            kengine append "network {nodeName} {'{ipKey}' = '{ip}' }\n"
            // find corresponding Kloud group to update the user group configuration on the kloud
            kloudModel.getGroups.filter(g => KloudModelHelper.isPaaSKloudGroup(kloudModel, g.getName) && g.getSubNodes.find(n => n.getName == node.getName).isDefined).foreach {
              group =>
                kengine.addVariable("groupName", group.getName)
                kengine append "updateDictionary {groupName} {ip='{ip}'}@{nodeName}"
            }

          } else {
            logger.debug("Unable to select an IP for {}", node.getName)
          }

          logger.debug("Add {} as child of {}", node.getName, potentialParents(index))
          potentialParents = potentialParents.filterNot(p => p == potentialParents(index))
          //        isEmpty = false
        }
      }
      true
    }

  def countChilds (kloudModel: ContainerRoot): List[(String, Int)] = {
    var counts = List[(String, Int)]()
    kloudModel.getNodes.filter {
      node =>
        val nodeType: NodeType = node.getTypeDefinition.asInstanceOf[NodeType]
        nodeType.getManagedPrimitiveTypes.filter(primitive => primitive.getName.toLowerCase == "addnode"
          || primitive.getName.toLowerCase == "removenode").size == 2
    }.foreach {
      node =>
        counts = counts ++ List[(String, Int)]((node.getName, node.getHosts.size))
    }
    counts
  }
}
