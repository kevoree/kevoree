package org.kevoree.library.javase.gossiperNetty.group

import org.kevoree.ContainerRoot
import java.net.{URLConnection, URL}
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}
import java.io._
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/04/12
 * Time: 17:22
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class HTTPClient(groupName : String) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def push (model: ContainerRoot, targetNodeName: String) {
    val ipOption = KevoreePropertyHelper.getStringNetworkProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var IP = "127.0.0.1"
    if (ipOption.isDefined) {
      IP = ipOption.get
    }
    val portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, groupName, "port", true, targetNodeName)
    var PORT: Int = 8000
    if (portOption.isDefined) {
      PORT = portOption.get
    }

    logger.debug("url=>" + "http://" + IP + ":" + PORT + "/model/current")

    if (!sendModel(model, "http://" + IP + ":" + PORT + "/model/current")) {
      logger.debug("Unable to push a model on " + targetNodeName)
    }
  }

  def pull (model: ContainerRoot, targetNodeName: String): ContainerRoot = {
    var localhost: String = "localhost"
    var port: Int = 8000
    try {
      val addressOption = KevoreePropertyHelper.getStringNetworkProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
      if (addressOption.isDefined) {
        localhost = addressOption.get
      }
      val portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, groupName, "port", true, targetNodeName)
      if (portOption.isDefined) {
        port = portOption.get
      }
    }
    catch {
      case e: IOException => {
        logger.error("Unable to getAddress or Port of " + targetNodeName, e)
      }
    }
    logger.debug("Pulling model " + targetNodeName + " " + "http://" + localhost + ":" + port + "/model/current")


    try {
      val url: URL = new URL("http://" + localhost + ":" + port + "/model/current")
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(2000)
      val inputStream: InputStream = conn.getInputStream
      KevoreeXmiHelper.loadStream(inputStream)
    }
    catch {
      case e: IOException => {
        logger.error("error while pulling model for name " + targetNodeName, e)
        null
      }
    }


  }

  private def sendModel (model: ContainerRoot, urlPath: String): Boolean = {
    try {
      val outStream = new ByteArrayOutputStream
      KevoreeXmiHelper.saveStream(outStream, model)
      outStream.flush()
      val url = new URL(urlPath)
      val conn = url.openConnection
      conn.setConnectTimeout(3000)
      conn.setDoOutput(true)
      val wr = new OutputStreamWriter(conn.getOutputStream)
      wr.write(outStream.toString)
      wr.flush()
      val rd = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line = rd.readLine
      while (line != null) {
        line = rd.readLine
      }
      wr.close()
      rd.close()
      true
    } catch {
      case e: Exception => {
        false
      }
    }
  }

}
