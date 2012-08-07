package org.kevoree.library.sky.provider

import org.slf4j.{LoggerFactory, Logger}
import java.net._
import org.kevoree._
import cloner.ModelCloner
import core.basechecker.RootChecker
import framework.{NetworkHelper, KevoreeXmiHelper, KevoreePropertyHelper}
import scala.collection.JavaConversions._
import java.io._


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

  def isUserModel (potentialUserModel: ContainerRoot, groupName: String, fragmentHostName: String): Boolean = {
    val foundGroupSelf = potentialUserModel.getGroups.find(g => g.getName == groupName).isDefined
    val foundHost = potentialUserModel.getNodes.find(n => n.getName == fragmentHostName).isDefined

    (foundGroupSelf && !foundHost)
  }

  def isUserModel (potentialUserModel: ContainerRoot): Boolean = {
    getKloudUserGroup(potentialUserModel).isDefined
  }

  def getKloudUserGroup (userModel: ContainerRoot): Option[String] = {
    val potentialKloudUserNodes = userModel.getNodes.filter(n => (n.getTypeDefinition.getName == "PJavaSENode" || KloudHelper.isASubType(n.getTypeDefinition, "PJavaSENode")))
    val potentialKloudUserGroups = userModel.getGroups.find(g => g.getSubNodes.size >= potentialKloudUserNodes.size)
    potentialKloudUserGroups.find(g => (g.getTypeDefinition.getName == "KloudPaaSNanoGroup" || KloudHelper.isASubType(g.getTypeDefinition, "KloudPaaSNanoGroup")) &&
      g.getSubNodes.forall(n => potentialKloudUserNodes.contains(n))) match {
      case None => None
      case Some(group) => Some(group.getName)
    }
  }

  def isIaaSNode (currentModel: ContainerRoot, groupName: String, nodeName: String): Boolean = {
    currentModel.getNodes/*getGroups.find(g => g.getName == groupName) match {
      case None => logger.debug("There is no group named {}", groupName); false
      case Some(group) =>
        group.getSubNodes*/.find(n => n.getName == nodeName) match {
          case None => logger.debug("There is no node named {}", nodeName); false
          case Some(node) =>
            node.getTypeDefinition.asInstanceOf[NodeType].getManagedPrimitiveTypes.filter(p => p.getName == "RemoveNode" || p.getName == "AddNode").size == 2
        }
//    }
  }

  def isPaaSNode (currentModel: ContainerRoot/*, groupName: String*/, nodeName: String): Boolean = {
    currentModel.getNodes/*Groups.find(g => g.getName == groupName) match {
      case None => false
      case Some(group) =>
        group.getSubNodes*/.find(n => n.getName == nodeName) match {
          case None => false
          case Some(node) =>
            node.getTypeDefinition.getName == "PJavaSENode" ||
              KloudHelper.isASubType(node.getTypeDefinition, "PJavaSENode")
        }
//    }
  }

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

  def selectPortNumber (address: String, ports: Array[Int]): Int = {
    var i = 8000
    if (address != "") {
      var found = false
      while (!found) {
        if (!ports.contains(i)) {
          try {
            val socket = new Socket()
            socket.connect(new InetSocketAddress(address, i), 1000)
            socket.close()
            i = i + 1
          } catch {
            case _@e =>
              found = true
          }
        } else {
          i = i + 1
        }
      }
    } else {
      var found = false
      while (!found) {
        if (!ports.contains(i)) {
          try {
            val socket = new ServerSocket(i)
            socket.close()
            found = true
          } catch {
            case _@e =>
              i = i + 1
          }
        } else {
          i = i + 1
        }
      }
    }
    i
  }

  def selectIP (parentNodeName: String, kloudModel: ContainerRoot): Option[String] = {
    logger.debug("try to select an IP for a child of {}", parentNodeName)
    kloudModel.getNodes.find(n => n.getName == parentNodeName) match {
      case None => None
      case Some(node) => {
        val subnetOption = KevoreePropertyHelper.getStringPropertyForNode(kloudModel, parentNodeName, "subnet")
        val maskOption = KevoreePropertyHelper.getStringPropertyForNode(kloudModel, parentNodeName, "mask")
        if (subnetOption.isDefined && maskOption.isDefined) {
          Some(lookingForNewIp(/*List(),*/ subnetOption.get, maskOption.get))
        } else {
          Some("127.0.0.1")
        }
      }
    }
  }

  private def lookingForNewIp (/*ips: List[String],*/ subnet: String, mask: String): String = {
    var newIp = subnet
    val ipBlock = subnet.split("\\.")
    var i = Integer.parseInt(ipBlock(0))
    var j = Integer.parseInt(ipBlock(1))
    var k = Integer.parseInt(ipBlock(2))
    var l = Integer.parseInt(ipBlock(3)) + 2

    var found = false
    while (i < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
      while (j < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
        while (k < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
          while (l < 255 && checkMask(i, j, k, l, subnet, mask) && !found) {
            val tmpIp = i + "." + j + "." + k + "." + l
//            if (!ips.contains(tmpIp)) {
              if (!NetworkHelper.isAccessible(tmpIp)) {
                /*
              val inet = InetAddress.getByName(tmpIp)
              if (!inet.isReachable(1000)) {*/
                newIp = tmpIp
                found = true
              }/* else {
                ips = ips ++ List[String](tmpIp)
              }*/
//            }
            l += 1
          }
          l = 1
          k += 1
        }
        k = 1
        j += 1
      }
      j = 1
      i += 1
    }
    newIp
  }

  private def checkMask (i: Int, j: Int, k: Int, l: Int, subnet: String, mask: String): Boolean = {
    val maskInt = ~((1 << (32 - Integer.parseInt(mask))) - 1)
    val ipBytes = InetAddress.getByName(i + "." + j + "." + k + "." + l).getAddress
    val subnetBytes = InetAddress.getByName(subnet).getAddress
    val subnetInt = (subnetBytes(0) << 24) | (subnetBytes(1) << 16) | (subnetBytes(2) << 8) | (subnetBytes(3) << 0)
    val ipInt = (ipBytes(0) << 24) | (ipBytes(1) << 16) | (ipBytes(2) << 8) | (ipBytes(3) << 0)
    (subnetInt & maskInt) == (ipInt & maskInt)
  }

  def countChilds (kloudModel: ContainerRoot): List[(String, Int)] = {
    var counts = List[(String, Int)]()
    kloudModel.getNodes.filter {
      node =>
        val nodeType: NodeType = node.getTypeDefinition.asInstanceOf[NodeType]
        nodeType.getManagedPrimitiveTypes.filter(primitive => primitive.getName.toLowerCase == "addnode"
          || primitive.getName.toLowerCase == "removenode").size == 2
    }.foreach {
      node =>
        counts = counts ++ List[(String, Int)]((node.getName, node.getHosts.size))
    }
    counts
  }

  def findChannel (componentName: String, portName: String, nodeName: String, currentModel: ContainerRoot): Option[String] = {
    currentModel.getMBindings.find(b => b.getPort.getPortTypeRef.getName == portName && b.getPort.eContainer.asInstanceOf[ComponentInstance].getName == componentName &&
      b.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == nodeName) match {
      case None => None
      case Some(binding) => {
        Some(binding.getHub.getName)
      }
    }
  }

  def getDefaultNodeAttributes (kloudModel: ContainerRoot, typeDefName: String): List[DictionaryAttribute] = {
    kloudModel.getTypeDefinitions.find(td => td.getName == typeDefName) match {
      case None => List[DictionaryAttribute]()
      case Some(td) =>
        td.getDictionaryType.get.getAttributes
    }
  }

  /**
   * get clean model with only nodes and without components, channels and groups
   */
  def cleanUserModel (model: ContainerRoot): Option[ContainerRoot] = {
    val cloner = new ModelCloner
    val cleanModel = cloner.clone(model)

    cleanModel.removeAllGroups()
    cleanModel.removeAllHubs()
    cleanModel.removeAllMBindings()
    cleanModel.getNodes.foreach {
      node =>
        node.removeAllComponents()
    }
    cleanModel.getNodes.filter(node =>
      model.getNodes.find(parent => parent.getHosts.contains(node)) match {
        case None => false
        case Some(n) => true
      }).foreach {
      node =>
        cleanModel.removeNodes(node)
    }

    Some(cleanModel)
  }

  /**
   * check if the model is valid
   */
  def check (model: ContainerRoot): Option[String] = {
    val checker: RootChecker = new RootChecker
    val violations = checker.check(model)
    if (violations.isEmpty) {
      None
    } else {
      val resultBuilder = new StringBuilder
      resultBuilder append "Unable to deploy this software on the Kloud because there is some constraints violations:\n"
      violations.foreach {
        violation =>
          resultBuilder append violation.getMessage
          resultBuilder append "\n"
      }
      Some(resultBuilder.toString())

    }
  }

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

}