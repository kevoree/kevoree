package org.kevoree.library.sky.api.http

import org.kevoree.library.webserver.internal.KTinyWebServerInternalServe
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import util.matching.Regex
import java.io.{IOException, FileNotFoundException, InputStream, ByteArrayOutputStream}
import org.kevoree.api.service.core.script.{KevScriptEngineException, KevScriptEngine}
import org.kevoree.library.sky.api.helper.KloudHelper
import org.json.JSONStringer
import org.kevoree.framework.Constants

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/05/12
 * Time: 17:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class IaaSHTTPServer (node: AbstractHostNode) extends Runnable {
  val logger = LoggerFactory.getLogger(this.getClass)
  private var srv: KTinyWebServerInternalServe = null
  private var mainT: Thread = null
  val NodeSubRequest = new Regex("/nodes/(.+)/(.+)")
  val NodeHomeRequest = new Regex("/nodes/(.+)")

  def startServer (port: Int) {
    srv = new KTinyWebServerInternalServe
    val properties: Properties = new Properties
    properties.put("port", new Integer(port))
    properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup")
    srv.arguments = properties
    mainT = new Thread(this)
    mainT.start()
    srv.addServlet("/*", new HttpServlet {
      protected override def service (req: HttpServletRequest, resp: HttpServletResponse) {
        req.getRequestURI match {
          case "/" => sendAdminNodeList(req, resp)
          case "/AddChild" => sendAddChildPage(req, resp)
          case "/RemoveChild" => removeChild(req, resp)
          case "/bootstrap.min.css" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap.min.css")), "text/css")
          case "/bootstrap.min.js" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap.min.js")), "text/javascript")
          case "/jquery.min.js" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("jquery.min.js")), "text/javascript")
          case "/add_child.js" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("add_child.js")), "text/javascript")
          case "/jquery.form.js" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("jquery.form.js")), "text/javascript")
          case "/add_child.css" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("add_child.css")), "text/css")
          case "/scaled500.png" => sendFile(req, resp, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("scaled500.png")), "image/png")
          case NodeSubRequest(nodeName, fluxName) => sendNodeFlux(req, resp, fluxName, nodeName)
          case NodeHomeRequest(nodeName) => sendNodeHome(req, resp, nodeName)
          case _ => sendError(req, resp)
        }
      }
    })
  }

  def stopServer () {
    srv.notifyStop()
    srv.destroyAllServlets()
    mainT.interrupt()
  }

  def run () {
    srv.serve
  }

  private def sendAdminNodeList (req: HttpServletRequest, resp: HttpServletResponse) {
    val htmlContent = VirtualNodeHTMLHelper.exportNodeListAsHTML(node)
    resp.setStatus(200)
    resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
  }

  private def sendNodeHome (req: HttpServletRequest, resp: HttpServletResponse, nodeName: String) {
    val htmlContent = VirtualNodeHTMLHelper.getNodeHomeAsHTML(nodeName, node.getNodeManager)
    resp.setStatus(200)
    resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
  }

  private def sendNodeFlux (req: HttpServletRequest, resp: HttpServletResponse, fluxName: String, nodeName: String) {
    val htmlContent = VirtualNodeHTMLHelper.getNodeStreamAsHTML(nodeName, fluxName, node.getNodeManager)
    resp.setStatus(200)
    resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
  }

  private def sendError (req: HttpServletRequest, resp: HttpServletResponse) {
    resp.setStatus(400)
    resp.getOutputStream.write("Unknown Request!".getBytes("UTF-8"))
  }

  private def sendFile (req: HttpServletRequest, resp: HttpServletResponse, bytes: Array[Byte], contentType: String) {
    resp.setStatus(200)
    resp.setContentType(contentType)
    resp.getOutputStream.write(bytes)
  }

  private def sendAddChildPage (req: HttpServletRequest, resp: HttpServletResponse) {
    if (req.getMethod.equalsIgnoreCase("GET")) {
      val model = node.getModelService.getLastModel
      val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudHelper.isPaaSNodeType(model, nt.getName) &&
        nt.getDeployUnits.find(dp => dp.getTargetNodeType.find(targetNodeType => KloudHelper.isASubType(node.getModelElement.getTypeDefinition, targetNodeType.getName)).isDefined).isDefined)

      val htmlContent = VirtualNodeHTMLHelper.exportPaaSNodeList(paasNodeTypes)
      resp.setStatus(200)
      resp.getOutputStream.write(htmlContent.getBytes("UTF-8"))
    } else {
      /*logger.info("blablabla")
      req.getParameterMap.entrySet().foreach {
        p =>
          logger.warn(p.getKey + " => " + p.getValue)
          sendAdminNodeList(req, resp)
      }*/
      var jsonString: String = null
      if (req.getParameter("request") == "add") {
        jsonString = addChildNode(req)
      } else if (req.getParameter("request") == "list") {
        jsonString = createTypeList()
      }
      // interpret json message and build response
      if (jsonString != null) {
        logger.debug(jsonString)
        resp.setStatus(200)
        resp.getOutputStream.write(jsonString.getBytes("UTF-8"))
      } else {
        resp.setStatus(500)
      }
    }
  }

  private def addChildNode (req: HttpServletRequest): String = {
    val kengine = node.getKevScriptEngineFactory.createKevScriptEngine()
    val jsonresponse = new JSONStringer().`object`()
    if (req.getParameter("type") != null) {
      // find the corresponding node type
      val typeName = req.getParameter("type")
      node.getModelService.getLastModel.getTypeDefinitions.find(td => td.getName == typeName) match {
        case None => jsonresponse.key("code").value("-1").key("message").value("There is no type named " + typeName)
        case Some(nodeType) => {
          var mustBeAdded = true
          if (req.getParameter("name") != null) {
            kengine.addVariable("nodeName", req.getParameter("name"))
            kengine.addVariable("nodeTypeName", typeName)
            kengine.addVariable("parentNodeName", node.getName)
            kengine append "addNode {nodeName} : {nodeTypeName}"
            kengine append "addChild {nodeName} @ {parentNodeName}"
            val ipOption = KloudHelper.selectIP(node.getName, node.getModelService.getLastModel)
            if (ipOption.isDefined) {
              kengine.addVariable("ipKey", Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
              kengine.addVariable("ip", ipOption.get)
              kengine append "network {nodeName} {'{ipKey}' = '{ip}' }\n"
            }
            // check attributes
            if (nodeType.getDictionaryType.isDefined) {
              nodeType.getDictionaryType.get.getAttributes.foreach {
                attribute => {
                  val value = req.getParameter(attribute.getName)
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
    val model = node.getModelService.getLastModel
    val paasNodeTypes = model.getTypeDefinitions.filter(nt => KloudHelper.isPaaSNodeType(model, nt.getName) &&
      nt.getDeployUnits.find(dp => dp.getTargetNodeType.find(targetNodeType => KloudHelper.isASubType(node.getModelElement.getTypeDefinition, targetNodeType.getName)).isDefined).isDefined)

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

  private def removeChild (req: HttpServletRequest, resp: HttpServletResponse) {
    val nodeName = req.getParameter("name")
    val kengine: KevScriptEngine = node.getKevScriptEngineFactory.createKevScriptEngine
    kengine append "removeNode " + nodeName
    try {
      kengine.atomicInterpretDeploy()
    } catch {
      case e: KevScriptEngineException => logger.warn("Unable to remove {}", nodeName, e)
    }
    sendAdminNodeList(req, resp)
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
