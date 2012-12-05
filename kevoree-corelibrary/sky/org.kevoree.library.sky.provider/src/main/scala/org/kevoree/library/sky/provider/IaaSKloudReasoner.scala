package org.kevoree.library.sky.provider

import org.kevoree.{ContainerNode, NodeType, ContainerRoot}
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.library.sky.api.helper.{KloudNetworkHelper, KloudModelHelper}
import org.kevoree.framework.{KevoreePropertyHelper, Constants}
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/11/12
 * Time: 11:44
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object IaaSKloudReasoner extends KloudReasoner {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def configureIsolatedNodes(iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    var doSomething = false
    iaasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(iaasModel, n) && iaasModel.getGroups.forall(g => !g.getSubNodes.contains(n))).foreach {
      node =>
        val parentNodeOption = node.getHost
        if (parentNodeOption.isDefined) {
          val ipOption = KevoreePropertyHelper.getStringNetworkProperty(iaasModel, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
          if (ipOption.isDefined) {
            doSomething = doSomething && configureIsolatedNode(node, parentNodeOption.get.getName, ipOption.get, iaasModel, kengine)
          }
        }
    }
    logger.debug("configure isolated nodes : {}", doSomething)
    doSomething

  }

  def configureChildNodes(iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    // count current child for each Parent nodes
    val parents = countChilds(iaasModel)
    var potentialParents = ListBuffer[String]()

    var doSomething = false
    var usedIps = List[String]()
    // filter nodes that are not IaaSNode and are not child of IaaSNode
    iaasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(iaasModel, n) && n.getHost.isEmpty).foreach {
      // select a host for each user node
      node => {
        logger.debug("try to select a parent for {}", node.getName)

        if (potentialParents.isEmpty) {
          potentialParents = lookAtPotentialParents(parents)
        }
        val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
        val parentName = potentialParents(index)
        potentialParents.remove(index)
        kengine.addVariable("nodeName", node.getName)
        kengine.addVariable("parentName", parentName)
        kengine append "addChild {nodeName}@{parentName}"

        // define IP using selecting node to know what is the network used in this machine
        val ipOption = defineIP(node.getName, parentName, iaasModel, kengine, usedIps)
        if (ipOption.isDefined) {
          usedIps = usedIps ++ List[String](ipOption.get)
        }

        // find corresponding Kloud group to update the user group configuration on the kloud
        iaasModel.getGroups.filter(g => KloudModelHelper.isPaaSKloudGroup(iaasModel, g) && g.getSubNodes.find(n => n.getName == node.getName).isDefined).foreach {
          group =>
            kengine.addVariable("groupName", group.getName)
            kengine append "updateDictionary {groupName} {ip='{ip}'}@{nodeName}"
        }

        logger.debug("Add {} as child of {}", node.getName, parentName)
        doSomething = true
      }
    }
    logger.debug("configure child nodes : {}", doSomething)
    doSomething
  }

  def countChilds(kloudModel: ContainerRoot): List[(String, Int)] = {
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

  def addNodes(addedNodes: java.util.List[ContainerNode], parentNodeNameOption: Option[String], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    if (!addedNodes.isEmpty) {
      logger.debug("Try to add all user nodes into the Kloud")

      // create new node using PJavaSENode as type for each user node
      var usedIps = List[String]()
      addedNodes.foreach {
        node =>
          kengine.addVariable("nodeName", node.getName)
          kengine.addVariable("nodeType", node.getTypeDefinition.getName)
          // TODO maybe we need to merge the deploy unit that offer this type if it is not one of our types
          // add node
          logger.debug("addNode {} : {}", node.getName, node.getTypeDefinition.getName)
          kengine append "addNode {nodeName} : {nodeType}"
          // set dictionary attributes of node
          if (node.getDictionary.isDefined) {
            val defaultAttributes = getDefaultNodeAttributes(iaasModel, node.getTypeDefinition.getName)
            node.getDictionary.get.getValues
              .filter(value => defaultAttributes.find(a => a.getName == value.getAttribute.getName) match {
              case Some(attribute) => true
              case None => false
            }).foreach {
              value =>
                kengine.addVariable("attributeName", value.getAttribute.getName)
                kengine.addVariable("attributeValue", value.getValue)
                kengine append "updateDictionary {nodeName} {{attributeName} = '{attributeValue}'}"
            }
          }
          logger.debug("{}", parentNodeNameOption)
          var parentName = ""
          if (parentNodeNameOption.isEmpty) {
            val parents = countChilds(iaasModel)
            var potentialParents = ListBuffer[String]()
            if (potentialParents.isEmpty) {
              potentialParents = lookAtPotentialParents(parents)
            }
            val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
            parentName = potentialParents(index)
            potentialParents.remove(index)
          } else {
            parentName = parentNodeNameOption.get
          }

          kengine.addVariable("parentName", parentName)
          kengine append "addChild {nodeName}@{parentName}"

          val ipOption = defineIP(node.getName, parentName, iaasModel, kengine, usedIps)
          var ip = "127.0.0.1"
          if (ipOption.isDefined) {
            usedIps = usedIps ++ List[String](ipOption.get)
            ip = ipOption.get
          }
          configureIsolatedNode(node, parentName, ip, iaasModel, kengine)
      }
      true
    } else {
      true
    }
  }

  private def configureIsolatedNode(node: ContainerNode, parentNodeName: String, ip: String, model: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    // add the new node on one of the group used by the host
    val groupOption = model.getGroups.find(g => g.getSubNodes.find(n => n.getName == parentNodeName).isDefined)
    if (groupOption.isDefined) {
      kengine addVariable("groupName", groupOption.get.getName)
      kengine append "addToGroup {groupName} {nodeName}"

      // get all fragment properties that exist for the group and that start or end with 'port', we try to specify a port for the node
      var usedPort = Array[Int]()
      if (groupOption.get.getTypeDefinition.getDictionaryType.isDefined) {
        groupOption.get.getTypeDefinition.getDictionaryType.get.getAttributes.filter(a => a.getFragmentDependant && (a.getName.startsWith("port") || a.getName.endsWith("port"))).foreach {
          attribute => {
            kengine addVariable("attributeName", attribute.getName)
            /* Warning This method try severals Socket to determine available port */
            val portNumber = KloudNetworkHelper.selectPortNumber(ip, usedPort)
            usedPort = usedPort ++ Array[Int](portNumber)
            kengine.addVariable("port", portNumber.toString)
            kengine append "updateDictionary {groupName} { {attributeName} = '{port}' }@{nodeName}"
          }
        }
      }
      true
    } else {
      false
    }
  }

  private def listAllIp(model: ContainerRoot): Array[String] = {
    var ips = Array[String]()
    model.getNodes.foreach {
      node =>
        val nodeIps = KevoreePropertyHelper.getStringNetworkProperties(model, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
        ips = ips ++ nodeIps.toArray(new Array[String](nodeIps.size()))
    }
    ips
  }


  private def defineIP(nodeName: String, parentName: String, model: ContainerRoot, kengine: KevScriptEngine, usedIps : List[String]): Option[String] = {
    kengine.addVariable("nodeName", nodeName)
    kengine.addVariable("parentName", parentName)
    // define IP using selecting node to know what is the network used in this machine
    val ipOption = KloudNetworkHelper.selectIP(parentName, model, listAllIp(model) ++ usedIps)
    if (ipOption.isDefined) {
      kengine.addVariable("ipKey", Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
      kengine.addVariable("ip", ipOption.get)
      kengine append "network {nodeName} {'{ipKey}' = '{ip}' }\n"
    } else {
      logger.debug("Unable to select an IP for {}", nodeName)
    }

    logger.debug("IP {} has been selected for the node {} on the host {}", Array(ipOption, nodeName, parentName))
    ipOption
  }

  def lookAtPotentialParents(parents: List[(String, Int)]): ListBuffer[String] = {
    val potentialParents = ListBuffer[String]()
    var min = Int.MaxValue

    parents.foreach {
      parent =>
        if (parent._2 < min) {
          min = parent._2
          potentialParents.clear()
        } else if (parent._2 == min) {
          potentialParents.add(parent._1)
        }
    }
    potentialParents
  }
}
