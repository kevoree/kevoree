package org.kevoree.library.sky.provider

import org.kevoree.{ComponentInstance, DictionaryAttribute, ContainerNode, ContainerRoot}
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.framework.{Constants, KevoreePropertyHelper}
import org.kevoree.library.sky.api.helper.{KloudModelHelper, KloudNetworkHelper}
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/11/12
 * Time: 11:44
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object PaaSKloudReasoner {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def appendCreatePaaSManagerScript (iaasModel: ContainerRoot, id: String, nodeName: String, kloudManagerName: String, kloudManagerNodeName: String, portName: String, kengine: KevScriptEngine) {
    kengine.addVariable("componentName", id + "Manager")
    kengine.addVariable("nodeName", nodeName)
    kengine append "addComponent {componentName}@{nodeName} : PaaSManager"
    val channelOption = findChannel(kloudManagerName, portName, kloudManagerNodeName, iaasModel)
    if (channelOption.isEmpty) {
      kengine.addVariable("channelName", "channel" + System.currentTimeMillis())
      kengine.addVariable("channelType", "SocketChannel")
      kengine.addVariable("kloudManagerName", kloudManagerName)
      kengine.addVariable("portName", portName)
      kengine.addVariable("kloudManagerNodeName", kloudManagerNodeName)

      kengine append "addChannel {channelName} : {channelType}" // FIXME channel type and dictionary
      kengine append "bind {kloudManagerName}.{portName}@{kloudManagerNodeName} => {channelName}"
    } else {
      kengine.addVariable("channelName", channelOption.get)
    }
    kengine append "bind {componentName}.submit@{nodeName} => {channelName}"
  }

  def findChannel (componentName: String, portName: String, nodeName: String, model: ContainerRoot): Option[String] = {
    model.getMBindings.find(b => b.getPort.getPortTypeRef.getName == portName && b.getPort.eContainer.asInstanceOf[ComponentInstance].getName == componentName &&
      b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == nodeName) match {
      case None => None
      case Some(binding) => {
        Some(binding.getHub.getName)
      }
    }
  }

  def appendCreateGroupScript (iaasModel: ContainerRoot, id: String, nodeName: String, paasModel: ContainerRoot, kengine: KevScriptEngine) {
    paasModel.getGroups.find(g => g.getName == id) match {
      case None => {
        // if the paasModel doesn't contain a Kloud group, then we add a default one
        val ipOption = KevoreePropertyHelper.getStringNetworkProperty(iaasModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
        var ip = "127.0.0.1"
        if (ipOption.isDefined) {
          ip = ipOption.get
        }
        /* Warning This method try severals Socket to determine available port */
        val portNumber = KloudNetworkHelper.selectPortNumber(ip, Array[Int]())
        kengine.addVariable("groupName", id)
        kengine.addVariable("nodeName", nodeName)
        kengine.addVariable("port", portNumber.toString)
        kengine.addVariable("ip", ip)
        kengine.addVariable("groupType", "KloudPaaSNanoGroup")
        kengine append "addGroup {groupName} : KloudPaaSNanoGroup {masterNode='{nodeName}={ip}:{port}'}"
        kengine append "addToGroup {groupName} {nodeName}"
        kengine append "updateDictionary {groupName} {port='{port}', ip='{ip}'}@{nodeName}"
      }
      case Some(group) => {
        val ipOption = KevoreePropertyHelper.getStringNetworkProperty(iaasModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
        var ip = "127.0.0.1"
        if (ipOption.isDefined) {
          ip = ipOption.get
        }
        /* Warning This method try severals Socket to determine available port */
        val portNumber = KloudNetworkHelper.selectPortNumber(ip, Array[Int]())
        kengine.addVariable("groupName", id)
        kengine.addVariable("nodeName", nodeName)
        kengine.addVariable("port", portNumber.toString)
        kengine.addVariable("ip", ip)
        kengine.addVariable("groupType", group.getTypeDefinition.getName)

        kengine append "addGroup {groupName} : {groupType}"
        kengine append "addToGroup {groupName} {nodeName}"
        kengine append "updateDictionary {groupName} {port='{port}', ip='{ip}'}@{nodeName}"

        /*if (group.getDictionary.isDefined) {
          //            scriptBuilder append "{"
          val defaultAttributes = getDefaultNodeAttributes(iaasModel, group.getTypeDefinition.getName)
          group.getDictionary.get.getValues
            .filter(value => value.getAttribute.getName != "ip" && value.getAttribute.getName != "port" && defaultAttributes.find(a => a.getName == value.getAttribute.getName).isDefined).foreach {
            value =>
              kengine.addVariable("attributeName", value.getAttribute.getName)
              kengine.addVariable("attributeValue", value.getValue)
              kengine append "updateDictionary {groupName} {{attributeName} = '{attributeValue}'}"
          }
        }*/
      }
    }

  }


  def selectIaaSNodeAsMaster (model: ContainerRoot): String = {
    val iaasNodes = model.getNodes.filter(n => KloudModelHelper.isIaaSNode(model, n.getName))

    var minNbSlaves = Int.MaxValue
    var iaasNode: ContainerNode = null
    iaasNodes.foreach {
      node => {
        val nbSlaves = countSlaves(node.getName, model)
        if (minNbSlaves > nbSlaves) {
          minNbSlaves = nbSlaves
          iaasNode = node
        }
      }
    }
    iaasNode.getName
  }

  def countSlaves (nodeName: String, iaasModel: ContainerRoot): Int = {
    iaasModel.getNodes.find(n => n.getName == nodeName) match {
      case None => logger.warn("The node {} doesn't exist !", nodeName); Int.MaxValue
      case Some(node) => {
        // TODO replace when the nature will be added and managed on the model
        //        node.getComponents.filter(c => KloudModelHelper.isASubType(c.getTypeDefinition, "")).size
        node.getComponents.filter(c => KloudModelHelper.isASubType(c.getTypeDefinition, "PaaSManager")).size
      }
    }
  }


  /**
   * all node are disseminate on parent node
   * A parent node is defined by two adaptation primitives <b>addNode</b> and <b>removeNode</b>
   */
  def addNodes (addedNodes: java.util.List[ContainerNode], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    if (!addedNodes.isEmpty) {
      logger.debug("Try to add all user nodes into the Kloud")

      // create new node using PJavaSENode as type for each user node
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
            //            scriptBuilder append "{"
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
      }
      true
    } else {
      true
    }
  }

  def removeNodes (removedNodes: java.util.List[ContainerNode], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    if (!removedNodes.isEmpty) {
      logger.debug("Try to remove useless PaaS nodes into the Kloud")

      // build kevscript to remove useless nodes into the kloud model
      removedNodes.foreach {
        node =>
          iaasModel.getNodes.find(n => n.getHosts.find(host => host.getName == node.getName) match {
            case None => false
            case Some(host) => true
          }) match {
            case None => logger
              .debug("Unable to find the parent of {}. Houston, maybe we have a problem!", node.getName)
            case Some(parent) =>
              kengine.addVariable("nodeName", node.getName)
              //              kengine.addVariable("parentNodeName", parent.getName)
              //              kengine append "removeChild {nodeName}@{parentNodeName}"
              //              kengine append "removeFromGroup * {nodeName}"
              kengine append "removeNode {nodeName}"
          }

      }
      true
    } else {
      true
    }
  }

  def getNodesToRemove (iaasModel: ContainerRoot, paasModel: ContainerRoot): java.util.List[ContainerNode] = {
    var removedNodes = List[ContainerNode]()
    iaasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(iaasModel, n.getName)).foreach {
      paasNode =>
        paasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(paasModel, n.getName)).find(node => node.getName == paasNode.getName) match {
          case None => {
            logger.debug("{} must be removed from the kloud.", paasNode.getName)
            removedNodes = removedNodes ++ List[ContainerNode](paasNode)
          }
          case Some(newUserNode) =>
        }
    }
    removedNodes
  }

  def getNodesToAdd (iaasModel: ContainerRoot, paasModel: ContainerRoot): java.util.List[ContainerNode] = {
    var nodesToAdd = List[ContainerNode]()
    paasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(paasModel, n.getName)).foreach {
      paasNode =>
      // the kloud platform must use PJavaSeNode or a subtype of it so JavaSeNode will not be instanciated on the Kloud
        iaasModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(iaasModel, n.getName)).find(node => node.getName == paasNode.getName) match {
          case None => {
            logger.debug("{} must be added on the kloud.", paasNode.getName)
            nodesToAdd = nodesToAdd ++ List[ContainerNode](paasNode)
          }
          case Some(userNode) =>
        }
    }
    nodesToAdd
  }


  def getDefaultNodeAttributes (iaasModel: ContainerRoot, typeDefName: String): List[DictionaryAttribute] = {
    iaasModel.getTypeDefinitions.find(td => td.getName == typeDefName) match {
      case None => List[DictionaryAttribute]()
      case Some(td) =>
        td.getDictionaryType.get.getAttributes
    }
  }

}
