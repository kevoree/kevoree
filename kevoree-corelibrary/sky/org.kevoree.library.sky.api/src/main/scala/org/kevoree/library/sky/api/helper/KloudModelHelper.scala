package org.kevoree.library.sky.api.helper

import org.slf4j.{LoggerFactory, Logger}
import org.kevoree._
import core.basechecker.RootChecker
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

  def isPaaSModel (potentialPaaSModel: ContainerRoot, groupName: String, fragmentHostName: String): Boolean = {
    val foundGroupSelf = potentialPaaSModel.getGroups.find(g => g.getName == groupName).isDefined
    val foundHost = potentialPaaSModel.getNodes.find(n => n.getName == fragmentHostName).isDefined

    (foundGroupSelf && !foundHost)
  }

  def isPaaSModel (potentialUserModel: ContainerRoot): Boolean = {
    getPaaSKloudGroup(potentialUserModel).isDefined
  }

  def getPaaSKloudGroups (model: ContainerRoot): List[Group] = {
    val potentialKloudUserNodes = model.getNodes.filter(n => isPaaSNode(model, n.getName))
    logger.debug(potentialKloudUserNodes.mkString(", "))
    // FIXME replace when nature will be added and managed
//    model.getGroups.filter(g => (g.getTypeDefinition.getName == "PaaSGroup" || KloudTypeHelper.isASubType(g.getTypeDefinition, "PaaSGroup")) &&
      model.getGroups.filter(g => (g.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(g.getTypeDefinition, "KloudPaaSNanoGroup")) &&
      g.getSubNodes.forall(n => potentialKloudUserNodes.contains(n)))
  }

  def getPaaSKloudGroup (userModel: ContainerRoot): Option[String] = {
    val potentialKloudUserNodes = userModel.getNodes.filter(n => isPaaSNode(userModel, n.getName))
    val potentialKloudUserGroups = userModel.getGroups.find(g => g.getSubNodes.size >= potentialKloudUserNodes.size)
    // FIXME replace when nature will be added and managed
//    potentialKloudUserGroups.find(g => (g.getTypeDefinition.getName == "PaaSGroup" || KloudTypeHelper.isASubType(g.getTypeDefinition, "PaaSGroup")) &&
      potentialKloudUserGroups.find(g => (g.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(g.getTypeDefinition, "KloudPaaSNanoGroup")) &&
      g.getSubNodes.forall(n => potentialKloudUserNodes.contains(n))) match {
      case None => None
      case Some(group) => Some(group.getName)
    }
  }

  def isIaaSNode (currentModel: ContainerRoot, groupName: String, nodeName: String): Boolean = {
    currentModel.getNodes.find(n => n.getName == nodeName) match {
      case None => logger.debug("There is no node named {}", nodeName); false
      case Some(node) =>
        node.getTypeDefinition.asInstanceOf[NodeType].getManagedPrimitiveTypes.filter(p => p.getName == "RemoveNode" || p.getName == "AddNode").size == 2
    }
  }

  def isPaaSNode (currentModel: ContainerRoot /*, groupName: String*/ , nodeName: String): Boolean = {
    currentModel.getNodes.find(n => n.getName == nodeName) match {
      case None => false
      case Some(node) =>
        node.getTypeDefinition.getName == "PJavaSENode" ||
          isASubType(node.getTypeDefinition, "PJavaSENode")
    }
  }

  def isPaaSNodeType (currentModel: ContainerRoot, nodeTypeName: String): Boolean = {
    currentModel.getTypeDefinitions.find(n => n.getName == nodeTypeName) match {
      case None => false
      case Some(nodeType) =>
        nodeType.getName == "PJavaSENode" ||
          isASubType(nodeType, "PJavaSENode")
    }
  }

  def isPaaSKloudGroup (kloudModel: ContainerRoot, groupName: String): Boolean = {
    kloudModel.getGroups.find(g => g.getName == groupName &&
      (g.getTypeDefinition.getName == "KloudPaaSNanoGroup" || isASubType(g.getTypeDefinition, "KloudPaaSNanoGroup")))
      .isDefined
  }

  def isASubType (nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  def getGroup (groupName: String, currentModel: ContainerRoot): Option[Group] = {
    currentModel.getGroups.find(g => g.getName == groupName)
  }

/*  def lookForAGroup (groupName: String, currentModel: ContainerRoot): Boolean = {
    currentModel.getGroups.find(g => g.getName == groupName).isDefined
  }*/
  
  /**
   * check if the model is valid
   */
  def check (model: ContainerRoot): Option[String] = {
    val checker: RootChecker = new RootChecker
    val violations = checker.check(model)
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