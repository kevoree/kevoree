package org.kevoree.library.sky.provider.web

import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import org.kevoree.library.sky.api.helper.KloudHelper
import org.json.JSONStringer
import org.kevoree.framework.Constants
import org.kevoree.api.service.core.script.{KevScriptEngineException, KevScriptEngine}
import java.io.{IOException, FileNotFoundException, ByteArrayOutputStream, InputStream}
import org.slf4j.LoggerFactory
import util.matching.Regex

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 16:32
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class IaaSKloudResourceManagerPageGenerator (instance: IaaSKloudResourceManagerPage, parentNodeName: String, pattern: String) {
  val logger = LoggerFactory.getLogger(this.getClass)

  val rootRequest = new Regex(pattern)
  val NodeSubRequest = new Regex(pattern + "nodes/(.+)/(.+)")
  val NodeHomeRequest = new Regex(pattern + "nodes/(.+)")
  val addChildRequest = new Regex(pattern + "AddChild")
  val removeChildRequest = new Regex(pattern + "RemoveChild")
  val bootstrapCSSRequest = new Regex(pattern + "bootstrap.min.css")
  val bootstrapJSRequest = new Regex(pattern + "bootstrap.min.js")
  val jqueryRequest = new Regex(pattern + "jquery.min.js")
  val jqueryFormRequest = new Regex(pattern + "jquery.form.js")
  val addChildJSRequest = new Regex(pattern + "add_child.js")
  val kevoreePictureRequest = new Regex(pattern + "scaled500.png")
  val addChildCSSRequest = new Regex(pattern + "add_child.css")

  def process (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {

    request.getUrl match {
      case bootstrapCSSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap.min.css")), "text/css")
      case bootstrapJSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap.min.js")), "text/javascript")
      case jqueryRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("jquery.min.js")), "text/javascript")
      case jqueryFormRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("jquery.form.js")), "text/javascript")
      case addChildJSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("add_child.js")), "text/javascript")
      case addChildCSSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("add_child.css")), "text/css")
      case kevoreePictureRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("scaled500.png")), "image/png")
      case _ => {
        request.getUrl match {
          case rootRequest() => sendAdminNodeList(request, response)
          case addChildRequest() => sendAddChildPage(request, response)
          case removeChildRequest() => removeChild(request, response)
          case NodeSubRequest(nodeName, fluxName) => sendNodeFlux(request, response, fluxName, nodeName)
          case NodeHomeRequest(nodeName) => sendNodeHome(request, response, nodeName)
          case _ => sendError(request, response)
        }
      }
    }
  }

  private def sendAdminNodeList (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val htmlContent = VirtualNodeHTMLHelper.exportNodeListAsHTML(pattern, parentNodeName, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  private def sendNodeHome (request: KevoreeHttpRequest, response: KevoreeHttpResponse, nodeName: String): KevoreeHttpResponse = {
    val htmlContent = VirtualNodeHTMLHelper.getNodeHomeAsHTML(pattern, parentNodeName, nodeName, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  private def sendNodeFlux (request: KevoreeHttpRequest, response: KevoreeHttpResponse, fluxName: String, nodeName: String): KevoreeHttpResponse = {
    val htmlContent = VirtualNodeHTMLHelper.getNodeStreamAsHTML(pattern, parentNodeName, nodeName, fluxName, instance.getModelService.getLastModel)
    response.setStatus(200)
    response.setContent(htmlContent)
    response
  }

  private def sendError (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    response.setStatus(400)
    response.setContent("Unknown Request!")
    response
  }

  private def sendFile (request: KevoreeHttpRequest, response: KevoreeHttpResponse, bytes: Array[Byte], contentType: String): KevoreeHttpResponse = {
    response.setStatus(200)
    response.getHeaders.put("Content-Type", contentType)
    response.setRawContent(bytes)
    response
  }

  private def sendAddChildPage (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    if (request.getMethod.equalsIgnoreCase("GET")) {
      val model = instance.getModelService.getLastModel
      model.getNodes.find(n => n.getName == parentNodeName) match {
        case None => {
          response.setStatus(500)
          response.setContent("Unable to find the IaaS node: " + parentNodeName)
        }
        case Some(parent) => {
          val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudHelper.isPaaSNodeType(model, nt.getName) &&
            nt.getDeployUnits.find(dp => dp.getTargetNodeType.find(targetNodeType => KloudHelper.isASubType(parent.getTypeDefinition, targetNodeType.getName)).isDefined).isDefined)

          val htmlContent = VirtualNodeHTMLHelper.exportPaaSNodeList(pattern, paasNodeTypes)
          response.setStatus(200)
          response.setContent(htmlContent)
        }
      }

    } else {
      /*logger.info("blablabla")
              req.getParameterMap.entrySet().foreach {
                p =>
                  logger.warn(p.getKey + " => " + p.getValue)
                  sendAdminNodeList(req, resp)
              }*/
      var jsonString: String = null
      if (request.getResolvedParams.get("request") == "add") {
        jsonString = addChildNode(request)
      } else if (request.getResolvedParams.get("request") == "list") {
        jsonString = createTypeList()
      }
      // interpret json message and build response
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
            kengine.addVariable("parentNodeName", parentNodeName)
            kengine append "addNode {nodeName} : {nodeTypeName}"
            kengine append "addChild {nodeName} @ {parentNodeName}"
            val ipOption = KloudHelper.selectIP(parentNodeName, instance.getModelService.getLastModel)
            if (ipOption.isDefined) {
              kengine.addVariable("ipKey", Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
              kengine.addVariable("ip", ipOption.get)
              kengine append "network {nodeName} {'{ipKey}' = '{ip}' }\n"
            }
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
  }

  private def createTypeList (): String = {
    val jsonresponse = new JSONStringer().`object`()
    val model = instance.getModelService.getLastModel
    model.getNodes.find(n => n.getName == parentNodeName) match {
      case None => {
        null
      }
      case Some(parent) => {
        val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudHelper.isPaaSNodeType(model, nt.getName) &&
          nt.getDeployUnits.find(dp => dp.getTargetNodeType.find(targetNodeType => KloudHelper.isASubType(parent.getTypeDefinition, targetNodeType.getName)).isDefined).isDefined)

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
    val kengine: KevScriptEngine = instance.getKevScriptEngineFactory.createKevScriptEngine
    kengine append "removeNode " + nodeName
    try {
      kengine.atomicInterpretDeploy()
    } catch {
      case e: KevScriptEngineException => logger.warn("Unable to remove {}", nodeName, e)
    }
    sendAdminNodeList(request, response)
  }

  private def getBytesFromStream (stream: InputStream): Array[Byte] = {
    try {
      val writer: ByteArrayOutputStream = new ByteArrayOutputStream
      val bytes: Array[Byte] = new Array[Byte](2048)
      var length: Int = stream.read(bytes)
      while (length != -1) {
        writer.write(bytes, 0, length)
        length = stream.read(bytes)
      }
      writer.flush()
      writer.close()
      return writer.toByteArray
    }
    catch {
      case e: FileNotFoundException => {
        logger.error("Unable to get Bytes from stream", e)
      }
      case e: IOException => {
        logger.error("Unable to get Bytes from file", e)
      }
    }
    new Array[Byte](0)
  }
}
