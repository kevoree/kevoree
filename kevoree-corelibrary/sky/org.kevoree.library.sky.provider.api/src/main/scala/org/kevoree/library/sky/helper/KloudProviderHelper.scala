package org.kevoree.library.sky.helper

import org.kevoree.ContainerRoot
import org.kevoree.framework.{NetworkHelper, Constants, KevoreePropertyHelper, KevoreeXmiHelper}
import java.net.{URLConnection, URL}
import java.io._
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.library.sky.api.helper.{KloudModelHelper, KloudNetworkHelper}
import collection.mutable.ListBuffer

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 01/11/12
 * Time: 18:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object KloudProviderHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)


  def pullModel (urlPath: String): ContainerRoot = {
    try {
      val url: URL = new URL(urlPath)
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(2000)
      val inputStream: InputStream = conn.getInputStream
      KevoreeXmiHelper.loadStream(inputStream)
    }
    catch {
      case e: IOException => {
        null
      }
    }
  }

  def sendModel (model: ContainerRoot, urlPath: String): Boolean = {
    logger.debug("send model on {}", urlPath)
    try {
      val outStream: ByteArrayOutputStream = new ByteArrayOutputStream
      KevoreeXmiHelper.saveStream(outStream, model)
      outStream.flush()
      val url: URL = new URL(urlPath)
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(3000)
      conn.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(conn.getOutputStream)
      wr.write(outStream.toString)
      wr.flush()
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line: String = rd.readLine
      while (line != null) {
        line = rd.readLine
      }
      wr.close()
      rd.close()
      true
    }
    catch {
      case e: Exception => {
        false
      }
    }
  }

  def getMasterIP_PORT (masterProp: String): java.util.List[String] = {
    val result = new java.util.ArrayList[String]()
    masterProp.split(",").foreach(ips => {
      val vals = ips.split("=")
      if (vals.size == 2) {
        result.add(vals(1))
      }
    })
    result
  }


  /*def appendCreateGroupScript (kloudModel: ContainerRoot, login: String, nodeName: String, kevScriptEngine: KevScriptEngine, sshKey: String = "", storage: Boolean = false) {
    val ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(kloudModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP))
    var ip = "127.0.0.1"
    if (ipOption.isDefined) {
      ip = ipOption.get
    }
    /* Warning This method try severals Socket to determine available port */
    val portNumber = KloudNetworkHelper.selectPortNumber(6000, ip, ListBuffer[Int]())
    kevScriptEngine.addVariable("groupName", login)
    kevScriptEngine.addVariable("nodeName", nodeName)
    kevScriptEngine.addVariable("port", portNumber.toString)
    kevScriptEngine.addVariable("ip", ip)
    if (storage) {
      kevScriptEngine.addVariable("groupType", "KloudPaaSNanoGroupStateFull")
    } else {
      kevScriptEngine.addVariable("groupType", "KloudPaaSNanoGroup")
    }

    kevScriptEngine append "addGroup {groupName} : KloudPaaSNanoGroup {masterNode='{nodeName}={ip}:{port}'}"
    if (sshKey != null && sshKey != "") {
      kevScriptEngine.addVariable("sshKey", sshKey)
      kevScriptEngine append "updateDictionary {groupName} {SSH_Public_Key='{sshKey}'}"
    }
    kevScriptEngine append "addToGroup {groupName} {nodeName}"
    kevScriptEngine append "updateDictionary {groupName} {port='{port}', ip='{ip}'}@{nodeName}"
  }*/

  def selectIaaSNodeAsAMaster(model : ContainerRoot) {
    val iaasNodes = model.getNodes.filter(n => KloudModelHelper.isIaaSNode(model, n))

  }

}
