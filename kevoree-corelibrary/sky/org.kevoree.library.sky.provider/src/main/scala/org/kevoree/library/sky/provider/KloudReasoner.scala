package org.kevoree.library.sky.provider

import org.kevoree.{DictionaryAttribute, ContainerNode, ContainerRoot}
import org.kevoree.library.sky.api.helper.KloudModelHelper
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.JavaConversions._
import org.kevoree.api.service.core.script.KevScriptEngine


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/02/12
 * Time: 17:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KloudReasoner {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getNodesToRemove(currentModel: ContainerRoot, newModel: ContainerRoot): java.util.List[ContainerNode] = {
    var removedNodes = List[ContainerNode]()
    currentModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(currentModel, n)).foreach {
      paasNode =>
        newModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(newModel, n)).find(node => node.getName == paasNode.getName) match {
          case None => {
            logger.debug("{} must be removed from the kloud.", paasNode.getName)
            removedNodes = removedNodes ++ List[ContainerNode](paasNode)
          }
          case Some(newUserNode) =>
        }
    }
    removedNodes
  }

  def getNodesToAdd(currentModel: ContainerRoot, newModel: ContainerRoot): java.util.List[ContainerNode] = {
    var nodesToAdd = List[ContainerNode]()
    newModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(newModel, n)).foreach {
      paasNode =>
      // the kloud platform must use PJavaSeNode or a subtype of it so JavaSeNode will not be instanciated on the Kloud
        currentModel.getNodes.filter(n => KloudModelHelper.isPaaSNode(currentModel, n)).find(node => node.getName == paasNode.getName) match {
          case None => {
            logger.debug("{} must be added on the kloud.", paasNode.getName)
            nodesToAdd = nodesToAdd ++ List[ContainerNode](paasNode)
          }
          case Some(userNode) =>
        }
    }
    nodesToAdd
  }

  def getDefaultNodeAttributes(iaasModel: ContainerRoot, typeDefName: String): List[DictionaryAttribute] = {
    iaasModel.getTypeDefinitions.find(td => td.getName == typeDefName) match {
      case None => List[DictionaryAttribute]()
      case Some(td) =>
        td.getDictionaryType.getAttributes.toList
    }
  }

  def removeNodes(removedNodes: java.util.List[ContainerNode], iaasModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
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
              kengine append "removeNode {nodeName}"
          }

      }
      true
    } else {
      true
    }
  }

}