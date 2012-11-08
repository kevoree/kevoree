package org.kevoree.library.sky.provider.web

import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import org.kevoree.library.sky.api.helper.KloudModelHelper
import org.json.JSONStringer
import org.kevoree.api.service.core.script.KevScriptEngine
import org.slf4j.LoggerFactory
import util.matching.Regex
import org.kevoree.ContainerRoot

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 16:32
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class IaaSKloudResourceManagerPageGenerator (instance: IaaSKloudResourceManagerPage, pattern: String, parentNodeName: String) extends KloudResourceManagerPageGenerator(instance, pattern) {
  logger = LoggerFactory.getLogger(this.getClass)

  val rootRequest1 = new Regex(pattern.substring(0, pattern.length - 1))
  val rootRequest2 = new Regex(pattern)
  val addChildRequest = new Regex(pattern + "AddChild")
  val removeChildRequest = new Regex(pattern + "RemoveChild")
  val NodeSubRequest = new Regex(pattern + "nodes/(.+)/(.+)")
  // TODO maybe remove nodes on regex
  val NodeHomeRequest = new Regex(pattern + "nodes/(.+)") // TODO maybe remove nodes on regex

  def internalProcess (request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse] = {
    case rootRequest1() => getIaasPage(request, response)
    case rootRequest2() => getIaasPage(request, response)
    case addChildRequest() => addChild(request, response)
    case removeChildRequest() => removeChild(request, response)
    case NodeSubRequest(nodeName, fluxName) => getNodeLogPage(request, response, fluxName, nodeName)
    case NodeHomeRequest(nodeName) => getNodePage(request, response, nodeName)
  }

  private def getNodeLogPage (request: KevoreeHttpRequest, response: KevoreeHttpResponse, fluxName: String, nodeName: String): KevoreeHttpResponse = {
    val htmlContent = HTMLPageBuilder.getNodeLogPage(pattern, parentNodeName, nodeName, fluxName, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  private def getNodePage (request: KevoreeHttpRequest, response: KevoreeHttpResponse, nodeName: String): KevoreeHttpResponse = {
    val htmlContent = HTMLPageBuilder.getNodePage(pattern, parentNodeName, nodeName, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }


  private def getIaasPage (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val htmlContent = HTMLPageBuilder.getIaasPage(pattern, parentNodeName, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  private def addChild (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    if (request.getMethod.equalsIgnoreCase("GET")) {
      val model = instance.getModelService.getLastModel
      model.getNodes.find(n => n.getName == parentNodeName) match {
        case None => {
          response.setStatus(500)
          response.setContent("Unable to find the IaaS node: " + parentNodeName)
        }
        case Some(parent) => {
          val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudModelHelper.isPaaSNodeType(model, nt.getName) &&
            nt.getDeployUnits.find(dp => dp.getTargetNodeType.find(targetNodeType => KloudModelHelper.isASubType(parent.getTypeDefinition, targetNodeType.getName)).isDefined).isDefined)

          val htmlContent = HTMLPageBuilder.addNodePage(pattern, paasNodeTypes)
          response.setStatus(200)
          response.setContent(htmlContent)
        }
      }
    } else {
      var jsonString: String = null
      if (request.getResolvedParams.get("request") == "add") {
        jsonString = addChildNode(request)
      } else if (request.getResolvedParams.get("request") == "list") {
        jsonString = getNodeTypeList
      }
      if (jsonString != null) {
        logger.debug(jsonString)
        response.setStatus(200)
        response.setContent(jsonString)
      } else {
        response.setStatus(500)
      }
    }
    response
  }

  private def addChildNode (request: KevoreeHttpRequest): String = {
    val kengine = instance.getKevScriptEngineFactory.createKevScriptEngine(initializeModel(instance.getModelService.getLastModel))
    val jsonresponse = new JSONStringer().`object`()
    if (request.getResolvedParams.get("type") != null) {
      // find the corresponding node type
      val typeName = request.getResolvedParams.get("type")
      instance.getModelService.getLastModel.getTypeDefinitions.find(td => td.getName == typeName) match {
        case None => jsonresponse.key("code").value("-1").key("message").value("There is no type named " + typeName)
        case Some(nodeType) => {
          var mustBeAdded = true
          if (request.getResolvedParams.get("name") != null) {
            kengine.addVariable("nodeName", request.getResolvedParams.get("name"))
            kengine.addVariable("nodeTypeName", typeName)
            kengine.addVariable("parentNodeName", parentNodeName)
            kengine append "addNode {nodeName} : {nodeTypeName}"
            //            kengine append "addChild {nodeName} @ {parentNodeName}"
            //            val ipOption = KloudNetworkHelper.selectIP(parentNodeName, instance.getModelService.getLastModel)
            //            if (ipOption.isDefined) {
            //              kengine.addVariable("ipKey", Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
            //              kengine.addVariable("ip", ipOption.get)
            //              kengine append "network {nodeName} {'{ipKey}' = '{ip}' }\n"
            //            }
            // check attributes
            if (nodeType.getDictionaryType.isDefined) {
              nodeType.getDictionaryType.get.getAttributes.foreach {
                attribute => {
                  val value = request.getResolvedParams.get(attribute.getName)
                  if (value == null && !attribute.getOptional) {
                    nodeType.getDictionaryType.get.getDefaultValues.find(defaulValue => defaulValue.getAttribute.getName == attribute.getName) match {
                      case None => jsonresponse.key("code").value("-1").key("message").value("The attribute " + attribute.getName + " must be defined")
                      case Some(defaultValue) => {
                        kengine addVariable("attributeName", attribute.getName)
                        kengine addVariable("defaultValue", defaultValue.getValue)
                        kengine append "updateDictionary {nodeName} { {attributeName} = '{defaultValue}' }"
                      }
                    }
                  } else if (value != null) {
                    kengine addVariable("attributeName", attribute.getName)
                    kengine addVariable("value", value)
                    kengine append "updateDictionary {nodeName} { {attributeName} = '{value}' }"
                  }
                }
              }
            }
            try {
              instance.addToNode(kengine.interpret(), parentNodeName)
              jsonresponse.key("code").value("0")
            } catch {
              case e: Throwable => logger.warn("Unable to add the child", e); jsonresponse.key("code").value("-2").key("message").value(e.getMessage)
            }
          } else {
            jsonresponse.key("code").value("-2").key("message").value("The name of the node must be defined")
            mustBeAdded = false
          }
          /*if (mustBeAdded) {
            try {
              kengine.atomicInterpretDeploy()
              jsonresponse.key("code").value("0")
            } catch {
              case e: Exception => logger.debug("Unable to add a new node", e); jsonresponse.key("code").value("-3").key("message").value("Unable to add a ne node: " + e.getMessage)
            }
          }*/
        }
      }
    }
    jsonresponse.endObject().toString
  }

  private def getNodeTypeList: String = {
    val jsonresponse = new JSONStringer().`object`()
    val model = instance.getModelService.getLastModel
    model.getNodes.find(n => n.getName == parentNodeName) match {
      case None => {
        null
      }
      case Some(parent) => {
        val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudModelHelper.isPaaSNodeType(model, nt.getName) &&
          nt.getDeployUnits.find(dp => dp.getTargetNodeType.find(targetNodeType => KloudModelHelper.isASubType(parent.getTypeDefinition, targetNodeType.getName)).isDefined).isDefined)

        var types = List[String]()
        jsonresponse.key("request").value("list")

        paasNodeTypes.foreach {
          nodeType =>
            var msg = new JSONStringer().`object`().key("name").value("name").key("optional").value(false).endObject()
            var attributes = List[String](msg.toString)
            if (nodeType.getDictionaryType.isDefined) {
              nodeType.getDictionaryType.get.getAttributes.foreach {
                attribute => {
                  msg = new JSONStringer().`object`().key("name").value(attribute.getName).key("optional").value(attribute.getOptional)
                  if (attribute.getDatatype.startsWith("enum")) {
                    val vals = attribute.getDatatype.substring("enum=".length).split(",")
                    msg.key("values").value(vals)
                  }

                  val defaultValue = nodeType.getDictionaryType.get.getDefaultValues.find(v => v.getAttribute.getName == attribute.getName) match {
                    case None => ""
                    case Some(v) => v.getValue
                  }
                  msg.key("defaultValue").value(defaultValue)
                  msg.endObject()
                  attributes = attributes ++ List[String](msg.toString)
                }
              }
            }
            jsonresponse.key(nodeType.getName).value(attributes.toArray)
            types = types ++ List[String](nodeType.getName)

        }
        jsonresponse.key("types").value(types.toArray).endObject()
        logger.debug(types.mkString(", "))
        jsonresponse.toString
      }
    }
  }

  private def removeChild (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val nodeName = request.getResolvedParams.get("name")
    val kengine: KevScriptEngine = instance.getKevScriptEngineFactory.createKevScriptEngine(initializeModel(instance.getModelService.getLastModel))

    kengine addVariable("nodeName", nodeName)
    kengine append "addNode {nodeName} : JavaSENode"
    /*kengine append "removeNode " + nodeName
    try {
      kengine.atomicInterpretDeploy()
    } catch {
      case e: KevScriptEngineException => logger.warn("Unable to remove {}", nodeName, e)
    }*/
    try {
      instance.remove(kengine.interpret())
    } catch {
      case e: Throwable => logger.warn("Unable to remove the child", e)
    }
    request.setUrl(pattern)
    getIaasPage(request, response)
  }

  private def initializeModel (currentModel: ContainerRoot): ContainerRoot = {
    val kengine: KevScriptEngine = instance.getKevScriptEngineFactory.createKevScriptEngine(currentModel)
    currentModel.getNodes.foreach {
      node =>
        kengine addVariable("nodeName", node.getName)
        kengine append "removeNode {nodeName}"
    }
    currentModel.getHubs.foreach {
      channel =>
        kengine addVariable("channelName", channel.getName)
        kengine append "removeChannel {channelName}"
    }
    currentModel.getGroups.foreach {
      group =>
        kengine addVariable("groupName", group.getName)
        kengine append "removeGroup {groupName}"
    }

    kengine.interpret()
  }
}
