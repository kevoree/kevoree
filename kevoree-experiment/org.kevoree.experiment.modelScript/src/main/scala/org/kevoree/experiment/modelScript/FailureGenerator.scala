package org.kevoree.experiment.modelScript

import java.lang.Math
import scala.collection.JavaConversions._
import org.kevoree.{NodeNetwork, ContainerRoot}
import java.net.URL
import java.io.{InputStreamReader, BufferedReader}
import org.kevoree.framework.{KevoreeXmiHelper, KevoreePlatformHelper}
import org.eclipse.emf.ecore.util.EcoreUtil

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 13:59
 */

class FailureGenerator (ips: List[String]) {

  var removedNodeNetworks: List[NodeNetwork] = List()

  var ip: String = null
  var port: Int = 0

  def doAction (action: String) {
    try {
      val model = loadCurrentModel()
      var currentModel: ContainerRoot = null

      if (action.equals("down") || ((selectRandomlyIntoList(List(true, false)) || removedNodeNetworks.isEmpty) && !action.equals("up"))) {
        currentModel = updateModelAccordingToRemovedNodeNetworks(EcoreUtil.copy(model))
        startNewFailure(currentModel)
      } else {
        stopFailure(model)
        currentModel = updateModelAccordingToRemovedNodeNetworks(EcoreUtil.copy(model))
      }
      save(currentModel)

      println("\nfailures: ")
      removedNodeNetworks.foreach {
        nn =>
          println(nn.getInitBy.getName + " -> " + nn.getTarget.getName)
      }
    }catch {
      case _ @ e => e.printStackTrace()
    }
  }

  def updateModelAccordingToRemovedNodeNetworks (model: ContainerRoot): ContainerRoot = {
    removedNodeNetworks.foreach {
      nn =>
        model.getNodeNetworks.remove(nn)
    }
    //println("number of NodeNetworks after update according to removedNodeNetworks: " + model.getNodeNetworks.size())
    model
  }


  def loadCurrentModel (): ContainerRoot = {
    port = 8000
    ip = selectRandomlyIntoList(ips).asInstanceOf[String]

    val url = "http://" + ip + ":" + port + "/model/current"
    //println("ask model to " + url)

    val model = KevoreeXmiHelper.load(url)

    //println("number of NodeNetworks before update according to removedNodeNetworks: " + model.getNodeNetworks.size())
    model
  }

  def save (model: ContainerRoot) {
    val fileNamePrefix = "model" + System.currentTimeMillis()
    saveModel(fileNamePrefix + ".kev", model)
    saveTopology(fileNamePrefix + ".graphml", model)
  }

  def saveModel (fileName: String, model: ContainerRoot) {
    KevoreeXmiHelper.save(fileName, model)
  }

  def saveTopology (fileName: String, model: ContainerRoot) {
    Kev2GraphML.toGraphMLFile(fileName, model)
  }

  def startNewFailure (model: ContainerRoot) {
    val nodeNetwork = selectRandomlyIntoList(model.getNodeNetworks.toList).asInstanceOf[NodeNetwork]
    val oppositeNodeNetwork = foundOpposite(nodeNetwork, model.getNodeNetworks.toList)
    removedNodeNetworks = removedNodeNetworks ++ List(nodeNetwork, oppositeNodeNetwork)
    sendOrder(buildBaseURL(model, nodeNetwork.getInitBy.getName) + "?down=" + nodeNetwork.getTarget.getName)
    sendOrder(buildBaseURL(model, nodeNetwork.getTarget.getName) + "?down=" + nodeNetwork.getInitBy.getName)
  }

  def stopFailure (model: ContainerRoot) {
    val nodeNetwork = selectRandomlyIntoList(removedNodeNetworks)
    val oppositeNodeNetwork = foundOpposite(nodeNetwork, removedNodeNetworks)
    removedNodeNetworks = removedNodeNetworks -- List(nodeNetwork, oppositeNodeNetwork)
    sendOrder(buildBaseURL(model, nodeNetwork.getInitBy.getName) + "?up=" + nodeNetwork.getTarget.getName)
    sendOrder(buildBaseURL(model, nodeNetwork.getTarget.getName) + "?up=" + nodeNetwork.getInitBy.getName)
  }

  private def foundOpposite (nodeNetwork: NodeNetwork, nodeNetworks : List[NodeNetwork]): NodeNetwork = {
    nodeNetworks.find(nn => nn.getInitBy.getName.equals(nodeNetwork.getTarget.getName) &&
      nn.getTarget.getName.equals(nodeNetwork.getInitBy.getName)) match {
      case Some(nn) => nn
      case None => println("there is no NodeNetwork with " + nodeNetwork.getTarget.getName + " as source and " +
        nodeNetwork.getInitBy.getName + " as target"); null
    }
  }

  private def selectRandomlyIntoList[A] (elements: List[A]): A = {
    val i: Int = (Math.random() * elements.size).asInstanceOf[Int]
    //println("index of the selected element: " + i + " between " + elements.size + " elements")
    elements(i)
  }

  def buildBaseURL (model: ContainerRoot, nodeName: String): String = {
    var ip: String = KevoreePlatformHelper
      .getProperty(model, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    if (ip == null || (ip == "")) {
      ip = "127.0.0.1"
    }
    var port: String = KevoreePlatformHelper
      .getProperty(model, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT)
    if (port == null || (port == "")) {
      port = "8000"
    }
    "http://" + ip + ":" + port.replace("8", "10")
  }

  def sendOrder (urlString: String) {
    val url = new URL(urlString);
    //println("send order: " + urlString)
    val conn = url.openConnection();
    conn.setConnectTimeout(2000);
    //conn.setDoOutput(true);
    /*val wr = new OutputStreamWriter(conn.getOutputStream)
    wr.write("");
    wr.flush();*/

    // Get the response
    val rd = new BufferedReader(new InputStreamReader(conn.getInputStream));
    var line: String = rd.readLine;
    while (line != null) {
      //println("ipReturn" + line)
      //println(line)
      line = rd.readLine
    }
    //wr.close();
    rd.close();
  }
}