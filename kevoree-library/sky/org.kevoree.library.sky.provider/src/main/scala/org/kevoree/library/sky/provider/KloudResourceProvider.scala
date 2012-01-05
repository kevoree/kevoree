package org.kevoree.library.sky.provider

import org.kevoree.core.basechecker.RootChecker
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.JavaConversions._
import org.kevoree.cloner.ModelCloner
import org.kevoree.{NodeType, ContainerNode, KevoreeFactory, ContainerRoot}
import org.kevoree.api.service.core.handler.{UUIDModel, KevoreeModelHandlerService}
import org.kevoree.api.service.core.script.KevScriptEngineFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/01/12
 * Time: 15:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudResourceProvider {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)


  def check (model: ContainerRoot): String = {
    val checker: RootChecker = new RootChecker
    val violations = checker.check(model)
    if (violations.isEmpty) {
      ""
    } else {
      val result = "Unable to deploy this software on the Kloud because there is some constraints violations:\n" +
        violations.mkString("\n")
      logger.error(result)
      result

    }
  }

  def setForKloud (model: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): ContainerRoot = {
    model.getGroups
      .find(group => group.getSubNodes.size == model.getNodes.size) match {
      case None =>
        val scriptBuilder = new StringBuilder()
        scriptBuilder append "tblock {"
        // add a group to link all platform between them (if there is not)
        scriptBuilder append
          "merge  mvn:org.kevoree.library.javase/org.kevoree.library.javase.rest/" + KevoreeFactory.getVersion
        scriptBuilder append "addGroup sync : RestGroup"

        model.getNodes.foreach {
          node =>
            val number = java.lang.Math.random()
            scriptBuilder append "addToGroup sync" + number + " " + node.getName
            scriptBuilder append "UpdateDictionary sync" + number + " {port=\"8000\"}@" + node.getName
        }

        scriptBuilder append "}"
        try {
          kevScriptEngineFactory.createKevScriptEngine().append(scriptBuilder.toString()).interpret()
        } catch {
          case _@e => logger
            .error("Unable to apply KevScript to add a group that manage the overall software.", e);
          null
        }
      case Some(group) => model
    }
  }

  def distribute (model: ContainerRoot, login: String, uuidModel: UUIDModel): ContainerRoot = {
    // get the current configuration of the kloud
    val newModel = uuidModel.getModel

    // build a temporary model that will merge with the overall configuration of the Kloud
    // this temporary model contains all the nodes that must be created
    val cloner = new ModelCloner
    val modelTmp = cloner.clone(model)
    // we need to keep all group to know the group that we can use to update the config of the nodes after created these nodes
    //    modelTmp.removeAllGroups()
    modelTmp.removeAllHubs()
    modelTmp.removeAllMBindings()
    modelTmp.getNodes.foreach {
      node =>
        node.removeAllComponents()
    }
    modelTmp.getNodes.filter(node =>
      model.getNodes.find(parent => parent.getHosts.contains(node)) match {
        case None => false
        case Some(n) => true
      }).foreach {
      node =>
        modelTmp.removeNodes(node)
    }
    selectHosts(modelTmp, newModel)

    newModel
  }

  def update (uuidModel: UUIDModel, model: ContainerRoot, modelHandlerService: KevoreeModelHandlerService): Boolean = {
    try {
      modelHandlerService.atomicCompareAndSwapModel(uuidModel, model)
      true
    } catch {
      case _@e =>
        logger.debug("Unable to swap model, maybe because the new model is based on old configuration", e); false
    }
  }

  private def selectHosts (modelTmp: ContainerRoot, model: ContainerRoot) {
    val availableHosts = model.getNodes.filter {
      node =>
        val ntype: NodeType = node.getTypeDefinition.asInstanceOf[NodeType]
        ntype.getManagedPrimitiveTypes.filter(primitive => primitive.getName.toLowerCase == "addnode"
          || primitive.getName.toLowerCase == "removenode").size == 2
    }

    var min = Int.MaxValue
    var potentialParents = List[ContainerNode]()
    modelTmp.getNodes.foreach {
      node =>
        if (potentialParents.isEmpty) {
          min = Int.MaxValue

          availableHosts.foreach {
            parent => {
              if (parent.getHosts.size < min) {
                min = parent.getHosts.size
              }
            }
          }
          availableHosts.foreach {
            parent => {
              if (parent.getHosts.size < min) {
                potentialParents = potentialParents ++ List(parent)
              }
            }
          }
        }
        val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
        potentialParents.get(index).addHosts(node)
        potentialParents = potentialParents -- List(potentialParents.get(index))
    }
  }

}