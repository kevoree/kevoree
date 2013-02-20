package org.kevoree.library.sky.provider.web

import org.slf4j.LoggerFactory
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import util.matching.Regex
import org.kevoree.library.sky.api.helper.KloudModelHelper
import org.json.JSONStringer
import org.kevoree.Group
import org.kevoree.library.sky.provider.api.SubmissionException
import org.kevoree.impl.DefaultKevoreeFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/10/12
 * Time: 14:55
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class PaaSKloudResourceManagerPageGenerator(instance: PaaSKloudResourceManagerPage, pattern: String) extends KloudResourceManagerPageGenerator(instance, pattern) {
  logger = LoggerFactory.getLogger(this.getClass)
  val factory = new DefaultKevoreeFactory


  val initializeUserConfiguRequest = new Regex(pattern + "InitializeUser")
  val rootUserRequest = new Regex(pattern + "(.+)")
  val rootRequest = new Regex(pattern.substring(0, pattern.length - 1))

  def internalProcess(request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse] = {
    case initializeUserConfiguRequest() => initializeUserConfiguration(request, response)
    case rootRequest() => getPaasPage(request, response)
    case rootUserRequest(login) => getPaasUserPage(login, request, response)
  }

  def getPaasPage(request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val htmlContent = HTMLPageBuilder.getPaasPage(pattern, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  def getPaasUserPage(login: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    // looking for the corresponding group
    instance.getModelService.getLastModel.findByPath("groups[" + login + "]", classOf[Group]) match {
      case group: Group =>
      case null => {
        // if it doesn't exist, we create it
        val kengine = instance.getKevScriptEngineFactory.createKevScriptEngine()
        // TODO ?
      }
    }
    // FIXME no security at all here

    val htmlContent = HTMLPageBuilder.getPaasUserPage(login, pattern, instance.getModelService.getLastModel, instance.isPortBinded("delegate"))
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  private def initializeUserConfiguration(request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val jsonresponse = new JSONStringer().`object`()
    val login = request.getResolvedParams.get("login")
    var sshKey: String = request.getResolvedParams.get("sshKey")
    if (sshKey == null) {
      sshKey = ""
    }
    if (login != null) {
      /*if (request.getResolvedParams.get("password") != null) {
        // FIXME check authentication
      }*/
      // look for a group corresponding to this login
      if (KloudModelHelper.isPaaSKloudGroup(instance.getModelService.getLastModel, login)) {
        // if yes then we cannot initialize the user configuration because it is already done
        jsonresponse.key("code").value("1")
      } else {
        // if no then we try to initialize it
        try {
          instance.initialize(login, factory.createContainerRoot)
          jsonresponse.key("code").value("0")
        } catch {
          case e: SubmissionException => logger.error("Unable to initialize the user PaaS: " + login, e);
          jsonresponse.key("code").value("-1").key("message")
            .value("Unable to initialize the user PaaS: " + login + "\nerror: " + e.getMessage)
        }
      }
    }
    response.setStatus(200)
    response.setContent(jsonresponse.endObject().toString)
    response
  }

  /*private def addChild (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    if (request.getMethod.equalsIgnoreCase("GET")) {
      val model = instance.getModelService.getLastModel

      val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudTypeHelper.isPaaSNodeType(model, nt.getName))

      val htmlContent = HTMLPageBuilder.addNodePage(pattern, paasNodeTypes)
      response.setStatus(200)
      response.setContent(htmlContent)
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
  }*/

  /*private def addChildNode (request: KevoreeHttpRequest): String = {
    val kengine = instance.getKevScriptEngineFactory.createKevScriptEngine()
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
            kengine append "addNode {nodeName} : {nodeTypeName}"
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
          } else {
            jsonresponse.key("code").value("-2").key("message").value("The name of the node must be defined")
            mustBeAdded = false
          }
          if (mustBeAdded) {
            try {
              kengine.atomicInterpretDeploy()
              jsonresponse.key("code").value("0")
            } catch {
              case e: Exception => logger.debug("Unable to add a new node", e); jsonresponse.key("code").value("-3").key("message").value("Unable to add a ne node: " + e.getMessage)
            }
          }
        }
      }
    }
    jsonresponse.endObject().toString
  }*/

  /*private def removeChild (login: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val nodeName = request.getResolvedParams.get("name")
    // check if the node is a sub node of the group of the user
    instance.getModelService.getLastModel.getGroups.find(g => g.getName == login) match {
      case None =>
      case Some(group) => {
        group.getSubNodes.find(n => n.getName == nodeName) match {
          case None =>
          case Some(node) => {
            val kengine: KevScriptEngine = instance.getKevScriptEngineFactory.createKevScriptEngine
            kengine append "removeNode " + nodeName
            try {
              kengine.atomicInterpretDeploy()
            } catch {
              case e: KevScriptEngineException => logger.warn("Unable to remove {}", nodeName, e)
            }
          }
        }
      }
    }
    getPaasUserPage(login, request, response)
  }

  private def pushModel (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    response
  }

  private def getNodeTypeList: String = {
    val jsonresponse = new JSONStringer().`object`()
    val model = instance.getModelService.getLastModel
    val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudTypeHelper.isPaaSNodeType(model, nt.getName))

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
  }*/

}
