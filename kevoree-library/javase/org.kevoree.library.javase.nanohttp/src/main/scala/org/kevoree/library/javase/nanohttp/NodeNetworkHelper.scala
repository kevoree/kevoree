package org.kevoree.library.javase.nanohttp

import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import java.net.{SocketException, NetworkInterface}
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.framework.{KevoreePlatformHelper, AbstractGroupType}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 01/03/12
 * Time: 14:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object NodeNetworkHelper {
  private val logger = LoggerFactory.getLogger(getClass)


  def updateModelWithNetworkProperty (group: AbstractGroupType) : Option[ContainerRoot] = {
    val ipObject = group.getDictionary.get("ip")
    if (ipObject != null && ipObject.toString != "" && ipObject.toString != "0.0.0.0") {
      addNetworkProperty(group.getModelService.getLastModel, group.getNodeName, Array[(String, String)]((ipObject.toString, "unknown")), group.getKevScriptEngineFactory)
    } else {
      val addresses = getAddresses
      if (addresses.length > 0) {
        /*val modelOption = */addNetworkProperty(group.getModelService.getLastModel, group.getNodeName, addresses, group.getKevScriptEngineFactory)
        /*if (modelOption.isDefined) {
          group.getModelService.updateModel(modelOption.get)
        }*/
      } else {
        None
      }
    }
  }

  /*def addNetworkProperty (model: ContainerRoot, nodeName: String, ips: Array[(String, String)], kevScriptEngineFactory :  KevScriptEngineFactory): Option[ContainerRoot] = {
    val newModel = (new ModelCloner).clone(model)

    val scriptBuilder = new StringBuilder
    ips.foreach {
      ip =>
        scriptBuilder append
          "network " + nodeName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + ip._1 + "\", \"" + Constants.KEVOREE_PLATFORM_REMOTE_NETWORK_TYPE + "\"=\"" + ip._2 + "\" }\n"
    }

    try {
      val kevScripEngine = kevScriptEngineFactory.createKevScriptEngine (newModel)
      kevScripEngine.append(scriptBuilder.toString())
      Some(kevScripEngine.interpret())
    } catch {
      case _@e => None
    }
  }*/

  def getAddresses: Array[(String, String)] = {
    var addresses = Array[(String, String)]()
    try {
      NetworkInterface.getNetworkInterfaces.foreach {
        networkInterface =>
          if (!networkInterface.isLoopback) {
            networkInterface.getInterfaceAddresses.foreach {
              interfaceAddress =>
                addresses = addresses ++ Array[(String, String)]((interfaceAddress.getAddress.getHostAddress, networkInterface.getDisplayName))
            }
          }
      }
    }
    catch {
      case ex: SocketException => {
        logger.info("Unable to retrieve IP addresses", ex)
      }
    }
    addresses
  }

  def addNetworkProperty (model: ContainerRoot, nodeName: String, ips: Array[(String, String)], kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    ips.foreach {
      ip =>
        KevoreePlatformHelper.updateNodeLinkProp(model, nodeName, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, ip._1, ip._2, 100)
        logger.info("add {} as IP of {}", ip._1, nodeName)
    }
    Some(model)
  }

}
