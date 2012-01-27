package org.kevoree.library.sky.provider

import org.kevoree.tools.marShell.KevsEngine
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.{TypeDefinition, KevoreeFactory, Group, ContainerRoot}
import java.io.{ByteArrayOutputStream, InputStreamReader, BufferedReader, OutputStreamWriter}
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}
import java.net._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/01/12
 * Time: 15:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)


  def isASubType (nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  def lookForAGroup (groupName: String, currentModel: ContainerRoot): Boolean = {
    currentModel.getGroups.find(g => g.getName == groupName).isDefined
  }

  def getGroup (groupName: String, currentModel: ContainerRoot): Option[Group] = {
    currentModel.getGroups.find(g => g.getName == groupName)
  }

  def lookForAccessPoint (groupName: String, nodeName: String, currentModel: ContainerRoot): Option[String] = {
    currentModel.getGroups.find(g => g.getName == groupName) match {
      case None => None
      case Some(group) => {
        group.getSubNodes.find(n => n.getName == nodeName) match {
          case None => None
          case Some(node) => {
            KevoreePropertyHelper.getStringPropertyForGroup(currentModel, groupName, "publicURL", true, nodeName)
          }
        }
      }
    }
  }

  def createProxy (groupName: String, nodeName: String, proxyPath: String,
    currentModel: ContainerRoot): Option[ContainerRoot] = {
    val scriptBuilder = new StringBuilder()

    //find Web Server
    currentModel.getNodes.find(n => n.getName == nodeName) match {
      case None => logger.debug("Any proxy can be added because there is no webserver to use"); None
      case Some(node) => node.getComponents
        .find(c => c.getTypeDefinition.getName == "WebServer" || isASubType(c.getTypeDefinition, "WebServer")) match {
        case None => logger.debug("Any proxy can be added because there is no webserver to use"); None
        case Some(component) => {
          val forwardURLOption = buildForwardURL(groupName, nodeName, currentModel)
          if (forwardURLOption.isDefined) {
            scriptBuilder append "tblock {\n"

            scriptBuilder append "addComponent " + groupName + "_proxy@" + nodeName + " : ProxyPage " + "{forward=\"" +
              forwardURLOption.get +
              "\",urlpattern=\"" + proxyPath + "\"}\n"

            // bind Web Server with this proxy
            scriptBuilder append "addChannel channel_" + groupName + "_proxy" + "1 : defMSG\n"
            scriptBuilder append "addChannel channel_" + groupName + "_proxy" + "2 : defMSG\n"
            scriptBuilder append
              "bind " + groupName + "_proxy" + ".request@" + nodeName + " => channel_" + groupName + "_proxy" + "1\n"
            scriptBuilder append
              "bind " + groupName + "_proxy" + ".content@" + nodeName + " => channel_" + groupName + "_proxy" + "2\n"
            scriptBuilder append
              "bind " + component.getName + ".handler@" + nodeName + " => channel_" + groupName + "_proxy" + "1\n"
            scriptBuilder append
              "bind " + component.getName + ".response@" + nodeName + " => channel_" + groupName + "_proxy" + "2\n"

            scriptBuilder append "}"

            logger.debug("Try to apply the script below\n{}", scriptBuilder.toString())

            val kloudModelOption = KevsEngine.executeScript(scriptBuilder.toString(), currentModel)
            if (kloudModelOption.isDefined) {
              val publicURLOption = buildPublicURL(groupName, nodeName, kloudModelOption.get)
              if (publicURLOption.isDefined) {
                scriptBuilder.clear()
                scriptBuilder append "tblock {\n"

                scriptBuilder append
                  "updateDictionary " + groupName + " {publicURL=\"" + publicURLOption.get + "\"}\n"

                scriptBuilder append "}"

                logger.debug("Try to apply the script below\n{}", scriptBuilder.toString())

                KevsEngine.executeScript(scriptBuilder.toString(), kloudModelOption.get)
              } else {
                logger.debug("Unable to build the public URL to {} on {}", groupName, nodeName)
                None
              }
            } else {
              logger.debug("Unable to set the group {} on {}", groupName, nodeName)
              None
            }
          } else {
            logger.debug("Unable to build the forward URL to {} on {}", groupName, nodeName)
            None
          }
        }
      }
    }
  }

  def createGroup (groupName: String, nodeName: String, currentModel: ContainerRoot): Option[ContainerRoot] = {
    val portNumber = selectPortNumber("")
    val defaultPublicURLOption = buildDefaultPublicURL(groupName, nodeName, currentModel, portNumber)
    if (defaultPublicURLOption.isDefined) {
      val scriptBuilder = new StringBuilder()
      scriptBuilder append "tblock {\n"

      scriptBuilder append "addGroup " + groupName + " : KloudResourceManagerGroup\n"

      scriptBuilder append "addToGroup " + groupName + " " + nodeName + "\n"
      scriptBuilder append "updateDictionary " + groupName + " {port=\"" + portNumber + "\"}@" + nodeName + "\n"
      scriptBuilder append
        "updateDictionary " + groupName + " {publicURL=\"" + defaultPublicURLOption.get + "\"}\n"

      scriptBuilder append "}"

      logger.debug("Try to apply the script below\n{}", scriptBuilder.toString())

      KevsEngine.executeScript(scriptBuilder.toString(), currentModel)
    } else {
      logger.debug("Unable to create a group named {}", groupName)
      None
    }
  }

  def localPush (model: ContainerRoot, groupName: String, kloudModel: ContainerRoot) {
    logger.debug("try to push on the group the user model")
    val publicURLOption = KevoreePropertyHelper.getStringPropertyForGroup(kloudModel, groupName, "publicURL")
    if (publicURLOption.isDefined) {
      val url: URL = new URL(publicURLOption.get)
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(3000)
      conn.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(conn.getOutputStream)
      val outStream: ByteArrayOutputStream = new ByteArrayOutputStream
      KevoreeXmiHelper.saveStream(outStream, model)
      outStream.flush()
      wr.write(outStream.toString)
      wr.flush()
      // Get the response
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line: String = rd.readLine
      while (line != null) {
        line = rd.readLine
      }
      wr.close()
      rd.close()
    } else {
      logger.debug("Unable to find the publicURL attribute so we can not send the model")
    }
  }

  def selectPortNumber (address: String): Int = {
    var i = 8000
    if (address != "") {
      var found = false
      while (!found) {
        try {
          val socket = new Socket(address, i)
          socket.close()
          i = i + 1
        } catch {
          case _@e =>
            found = true
        }
      }
    } else {
      var found = false
      while (!found) {
        try {
          val socket = new ServerSocket(i)
          socket.close()
          found = true
        } catch {
          case _@e =>
            i = i + 1
        }
      }
    }
    i
  }

  /**
   * try to get the complete URL to forward data to the KloudResourceManagerGroup
   */
  private def buildForwardURL (groupName: String, nodeName: String, model: ContainerRoot): Option[String] = {
    model.getGroups.find(g => g.getName == groupName) match {
      case None => logger.error("Unable to find group named {}", groupName); None
      case Some(group) => {
        group.getSubNodes.find(n => n.getName == nodeName) match {
          case None => logger.error("Unable to find the node for {}", nodeName); None
          case Some(node) => {
            val optionIpValue = KevoreePropertyHelper
              .getStringNetworkProperty(model, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
            val optionPortValue = KevoreePropertyHelper
              .getIntPropertyForGroup(model, groupName, "port", true, nodeName)
            if (optionPortValue.isDefined) {
              if (optionIpValue.isDefined) {
                Some("http://" + optionIpValue.get + ":" + optionPortValue.get + "/model/current")
              } else {
                Some("http://localhost:" + optionPortValue.get + "/model/current")
              }
            } else {
              logger.debug("Unable to find the port number for {} on {}.", groupName, nodeName)
              None
            }
          }
        }
      }
    }
  }

  private def buildDefaultPublicURL (groupName: String, nodeName: String, model: ContainerRoot,
    portNumber: Int): Option[String] = {
    model.getNodes.find(n => n.getName == nodeName) match {
      case None => logger.error("Unable to find the node for {}", nodeName); None
      case Some(node) => {
        val optionIpValue = KevoreePropertyHelper
          .getStringNetworkProperty(model, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
        if (optionIpValue.isDefined) {
          Some("http://" + optionIpValue.get + ":" + portNumber + "/model/current")
        } else {
          Some("http://localhost:" + portNumber + "/model/current")
        }
      }
    }
  }

  /**
   * try to build the public url used by a KloudResourceManagerGroup to communicate with itself
   */
  private def buildPublicURL (groupName: String, nodeName: String, model: ContainerRoot): Option[String] = {
    // find the webserver to get the port
    // find the IP of the node
    // concatenate IP:port/groupName
    model.getGroups.find(g => g.getName == groupName) match {
      case None => logger.error("Unable to find group named {}", groupName);
      None
      case Some(group) => {
        group.getSubNodes.find(n => n.getName == nodeName) match {
          case None => logger.error("Unable to find the node for {}", nodeName);
          None
          case Some(node) => {
            node.getComponents.find(c => c.getName == groupName + "_proxy") match {
              case None => logger.error("Unable to find component for {}", groupName + "_proxy");
              None
              case Some(component) => {
                val optionIpValue = KevoreePropertyHelper
                  .getStringNetworkProperty(model, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
                val optionPortValue = KevoreePropertyHelper
                  .getIntPropertyForComponent(model, component.getName, "port")
                if (optionPortValue.isDefined) {
                  if (optionIpValue.isDefined) {
                    Some("http://" + optionIpValue.get + ":" + optionPortValue.get + "/" + groupName + "/model/current")
                  } else {
                    Some("http://localhost:" + optionPortValue.get + "/" + groupName + "/model/current")
                  }
                } else {
                  if (optionIpValue.isDefined) {
                    Some("http://" + optionIpValue.get + ":8080/" + groupName + "/model/current")
                  } else {
                    Some("http://localhost:8080/" + groupName + "/model/current")
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}