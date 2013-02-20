package org.kevoree.library.sky.api.helper

import org.slf4j.{LoggerFactory, Logger}
import org.kevoree._
import core.basechecker.RootChecker
import library.sky.api.checker.NodeNameKloudChecker
import scala.collection.JavaConversions._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/01/12
 * Time: 15:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudModelHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def isPaaSModel(potentialPaaSModel: ContainerRoot, groupName: String, fragmentHostName: String): Boolean = {
    val foundGroupSelf = potentialPaaSModel.findByPath("groups[" + groupName + "]", classOf[Group]) != null
    val foundHost = potentialPaaSModel.findByPath("nodes[" + fragmentHostName + "]", classOf[ContainerNode]) != null

    (foundGroupSelf && !foundHost)
  }

  def isPaaSModel(potentialUserModel: ContainerRoot): Boolean = {
    getPaaSKloudGroup(potentialUserModel).isDefined
  }

  def getPaaSKloudGroups(model: ContainerRoot): java.util.List[Group] = {
    val potentialKloudUserNodes = model.getNodes.filter(n => isPaaSNode(model, n))
    logger.debug(potentialKloudUserNodes.mkString(", "))
    // FIXME replace when nature will be added and managed
    //    model.getGroups.filter(g => (g.getTypeDefinition.getName == "PaaSGroup" || KloudTypeHelper.isASubType(g.getTypeDefinition, "PaaSGroup")) &&
    model.getGroups.filter(g => (g.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(g.getTypeDefinition, "KloudPaaSNanoGroup")) &&
      g.getSubNodes.forall(n => potentialKloudUserNodes.contains(n)))
  }

  def getPaaSKloudGroup(userModel: ContainerRoot): Option[String] = {
    val potentialKloudUserNodes = userModel.getNodes.filter(n => isPaaSNode(userModel, n))
    val potentialKloudUserGroups = userModel.getGroups.find(g => g.getSubNodes.size >= potentialKloudUserNodes.size)
    // FIXME replace when nature will be added and managed
    //    potentialKloudUserGroups.find(g => (g.getTypeDefinition.getName == "PaaSGroup" || KloudTypeHelper.isASubType(g.getTypeDefinition, "PaaSGroup")) &&
    potentialKloudUserGroups.find(g => (g.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(g.getTypeDefinition, "KloudPaaSNanoGroup")) &&
      g.getSubNodes.forall(n => potentialKloudUserNodes.contains(n))) match {
      case None => None
      case Some(group) => Some(group.getName)
    }
  }

  def isIaaSNode(currentModel: ContainerRoot, node: ContainerNode): Boolean = {
    // FIXME replace when nature will be added and managed
    node.getTypeDefinition.asInstanceOf[NodeType].getManagedPrimitiveTypes.filter(p => p.getName == "RemoveNode" || p.getName == "AddNode").size == 2
  }

  def isIaaSNode(currentModel: ContainerRoot, nodeName: String): Boolean = {
    // FIXME replace when nature will be added and managed
    currentModel.findByPath("nodes[" + nodeName + "]", classOf[ContainerNode]) match {
      case null => logger.debug("There is no node named {}", nodeName); false
      case node: ContainerNode =>
        node.getTypeDefinition.asInstanceOf[NodeType].getManagedPrimitiveTypes.filter(p => p.getName == "RemoveNode" || p.getName == "AddNode").size == 2
    }
  }

  def isPaaSNode(currentModel: ContainerRoot, node: ContainerNode): Boolean = {
    // FIXME replace when nature will be added and managed
    node.getTypeDefinition.getName == "PJavaSENode" || isASubType(node.getTypeDefinition, "PJavaSENode")
  }

  def isPaaSNode(currentModel: ContainerRoot, nodeName: String): Boolean = {
    // FIXME replace when nature will be added and managed
    currentModel.findByPath("nodes[" + nodeName + "]", classOf[ContainerNode]) match {
      case null => logger.debug("There is no node named {}", nodeName); false
      case node: ContainerNode =>
        node.getTypeDefinition.getName == "PJavaSENode" || isASubType(node.getTypeDefinition, "PJavaSENode")
    }
  }

  def isPaaSNodeType(currentModel: ContainerRoot, nodeType: TypeDefinition): Boolean = {
    // FIXME replace when nature will be added and managed
    nodeType.getName == "PJavaSENode" || isASubType(nodeType, "PJavaSENode")
  }

  def isPaaSKloudGroup(kloudModel: ContainerRoot, group: Group): Boolean = {
    // FIXME replace when nature will be added and managed
    group.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(group.getTypeDefinition, "KloudPaaSNanoGroup")
  }

  def isPaaSKloudGroup(kloudModel: ContainerRoot, groupName: String): Boolean = {
    // FIXME replace when nature will be added and managed
    kloudModel.getGroups.find(group => group.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(group.getTypeDefinition, "KloudPaaSNanoGroup")).isDefined
  }

  def isASubType(typeDefinition: TypeDefinition, typeName: String): Boolean = {
    typeDefinition.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)).isDefined
  }

  def getGroup(groupName: String, currentModel: ContainerRoot): Group = {
    currentModel.findByPath("groups[" + groupName + "]", classOf[Group])
  }

  /**
   * check if the model is valid
   */
  def check(id: String, model: ContainerRoot): Option[String] = {
    val checker = new RootChecker
    val kloudChecker = new NodeNameKloudChecker
    kloudChecker.setId(id)
    val violations = checker.check(model) ++ kloudChecker.check(model)
    if (violations.isEmpty) {
      None
    } else {
      val resultBuilder = new StringBuilder
      resultBuilder append "Unable to deploy this software on the Kloud because there is some constraints violations:\n"
      violations.foreach {
        violation =>
          resultBuilder append violation.getMessage
          resultBuilder append "\n"
      }
      Some(resultBuilder.toString())

    }
  }
}