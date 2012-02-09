package org.kevoree.library.sky.provider

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.slf4j.{LoggerFactory, Logger}
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, ByteArrayOutputStream}
import org.kevoree.cloner.ModelCloner
import org.kevoree.core.basechecker.RootChecker
import scala.collection.JavaConversions._
import org.kevoree._
import framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}
import java.net.{InetAddress, URLConnection, URL}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/01/12
 * Time: 18:32
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudDeploymentManager {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def isIaaSNode (currentModel: ContainerRoot, groupName: String, nodeName: String): Boolean = {
    currentModel.getGroups.find(g => g.getName == groupName) match {
      case None => false
      case Some(group) =>
        group.getSubNodes.find(n => n.getName == nodeName) match {
          case None => false
          case Some(node) =>
            //            node.eContainer.asInstanceOf[ContainerRoot].getAdaptationPrimitiveTypes.
            node.getTypeDefinition.asInstanceOf[NodeType].getManagedPrimitiveTypes.filter(p => p.getName == "RemoveNode" || p.getName == "AddNode").size == 2
          //            node.getTypeDefinition.getName == "IaaSNode" || KloudHelper.isASubType(node.getTypeDefinition, "IaaSNode")
        }
    }
  }

  def isPaaSNode (currentModel: ContainerRoot, groupName: String, nodeName: String): Boolean = {
    currentModel.getGroups.find(g => g.getName == groupName) match {
      case None => false
      case Some(group) =>
        group.getSubNodes.find(n => n.getName == nodeName) match {
          case None => false
          case Some(node) =>
            node.getTypeDefinition.getName == "PJavaSENode" ||
              KloudHelper.isASubType(node.getTypeDefinition, "PJavaSENode")
        }
    }
  }

  // currently we do not manage Public SSH keys modification
  /**
   * check if a new Deployment is needed.
   * A new deployment is needed if there are new nodes or some nodes have disappeared
   */
  def needsNewDeployment (newModel: ContainerRoot, currentModel: ContainerRoot): Boolean = {
    if (newModel != null && currentModel != null) {
      // check if there is the same number of nodes
      if (newModel.getNodes.size == currentModel.getNodes.size) {
        // for all node, we check if there already exist a equivalent node on the current model
        !newModel.getNodes.forall {
          node => currentModel.getNodes.find(n => n.getName == node.getName) match {
            case None => false
            case Some(n) => true
          }
        }
      } else {
        true
      }
    } else {
      true
    }
  }

  /**
   * compute a new deployment and apply it
   */
  def processDeployment (newModel: ContainerRoot, userModel: ContainerRoot, modelHandlerService: KevoreeModelHandlerService, kevScripEngineFactory: KevScriptEngineFactory, groupName: String) :Option[ContainerRoot] = {
    // check validity of the new model
    val resultOption = check(newModel)
    if (resultOption.isEmpty) {

      // clean newModel and userModel: get only nodes and one group
      val cleanedNewModelOption = cleanUserModel(newModel)
      val cleanedUserModelOption = cleanUserModel(userModel)
      if (cleanedNewModelOption.isDefined && cleanedUserModelOption.isDefined) {

        // compare newModel and userModel to know which nodes must be added or removed
        val comparison = compareModels(cleanedNewModelOption.get, cleanedUserModelOption.get)
        val addedNodes = comparison._1
        val removedNodes = comparison._2

        val uuidModel = modelHandlerService.getLastUUIDModel

        // remove useless nodes on the Kloud model
        var newKloudModelOption = removeNodes(removedNodes, uuidModel.getModel, kevScripEngineFactory)
        if (newKloudModelOption.isDefined) {

          // distribute the new user nodes on the Kloud model
          newKloudModelOption = addNodes(addedNodes, newKloudModelOption.get, kevScripEngineFactory)
          if (newKloudModelOption.isDefined) {

            // add the default group or bind this group with all user nodes
            newKloudModelOption = configureGroup(cleanedNewModelOption.get, newKloudModelOption.get, groupName, kevScripEngineFactory)
            if (newKloudModelOption.isDefined) {

              // update the Kloud model with the result of the distribution
              try {
                modelHandlerService.atomicCompareAndSwapModel(uuidModel, newKloudModelOption.get)

                // deploy the newModel on the user nodes
                updateUserConfiguration(groupName, cleanedNewModelOption.get, newModel, modelHandlerService, kevScripEngineFactory)
                cleanedNewModelOption
              } catch {
                case _@e =>
                  logger.debug("Unable to swap model, maybe because the new model is based on a too old configuration", e)
                None
              }
            } else {
              logger.debug("Unable to configure you access point to your nodes.")
              None
            }
          } else {
            logger.debug("Unable to define the user nodes.")
            None
          }
        } else {
          logger.debug("Unable to define user nodes")
          None
        }
      } else {
        logger.debug("Unable to manipulate user model.")
        None
      }
    } else {
      logger.debug(resultOption.get)
      None
    }
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
      val result = "Unable to deploy this software on the Kloud because there is some constraints violations:\n" +
        violations.mkString("\n")
      logger.debug(result)
      Some(result)

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
   * compare models and built a tuple of sets of added nodes and removed nodes 
   */
  def compareModels (newModel: ContainerRoot, userModel: ContainerRoot): (List[ContainerNode], List[ContainerNode]) = {
    var addedNodes = List[ContainerNode]()
    var removedNodes = List[ContainerNode]()
    userModel.getNodes.foreach {
      userNode =>
        newModel.getNodes.find(node => node.getName == userNode) match {
          case None => {
            logger.debug("{} must be removed from the kloud.", userNode.getName)
            removedNodes = removedNodes ++ List[ContainerNode](userNode)
          }
          case Some(newUserNode) =>
        }
    }
    newModel.getNodes.foreach {
      newUserNode =>
        userModel.getNodes.find(node => node.getName == newUserNode) match {
          case None => {
            logger.debug("{} must be added on the kloud.", newUserNode.getName)
            addedNodes = addedNodes ++ List[ContainerNode](newUserNode)
          }
          case Some(userNode) =>
        }
    }
    (addedNodes, removedNodes)
  }

  def removeNodes (removedNodes: List[ContainerNode], kloudModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    if (!removedNodes.isEmpty) {
      logger.debug("Try to remove useless PaaS nodes into the Kloud")

      // build kevscript to remove useless nodes into the kloud model
      val scriptBuilder = new StringBuilder()
      //      scriptBuilder append "tblock {\n"

      removedNodes.foreach {
        node =>
          kloudModel.getNodes.find(n => n.getHosts.find(host => host.getName == node.getName) match {
            case None => false
            case Some(host) => true
          }) match {
            case None => logger
              .debug("Unable to find the parent of {}. Houston, maybe we have a problem!", node.getName)
            case Some(parent) =>
              scriptBuilder append "removeChild " + node.getName + "@" + parent.getName + "\n"
              scriptBuilder append "removeFromGroup * " + node.getName + "\n"
              scriptBuilder append "removeNode " + node.getName + "\n"
          }

      }

      //      scriptBuilder append "}"

      logger.debug("Try to apply the following script to kloudmodel to add all the user nodes:\n{}",
                    scriptBuilder.toString())

      val kengine = kevScriptEngineFactory.createKevScriptEngine(kloudModel)
      try {
        kengine.append(scriptBuilder.toString())
        Some(kengine.interpret())
      } catch {
        case _@e => {
          logger.debug("KevScript Error : ", e)
          None
        }
      }
    } else {
      Some(kloudModel)
    }
  }

  /**
   * all node are disseminate on parent node
   * A parent node is defined by two adaptation primitives <b>addNode</b> and <b>removeNode</b>
   */
  def addNodes (addedNodes: List[ContainerNode], kloudModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    if (!addedNodes.isEmpty) {
      logger.debug("Try to add all user nodes into the Kloud")

      // build kevscript to add user nodes into the kloud model
      val scriptBuilder = new StringBuilder()
      //      scriptBuilder append "tblock {\n"

      // count current child for each Parent nodes
      val parents = countChilds(kloudModel)

      var min = Int.MaxValue
      var potentialParents = List[String]()

      // create new node using PJavaSENode as type for each user node
      addedNodes.foreach {
        node =>
        // add node
          scriptBuilder append "addNode " + node.getName + " : PJavaSENode "
          // set dictionary attributes of node
          if (node.getDictionary.isDefined) {
            scriptBuilder append "{"
            val defaultAttributes = getDefaultNodeAttributes(kloudModel)
            node.getDictionary.get.getValues
              .filter(value => defaultAttributes.find(a => a.getName == value.getAttribute.getName) match {
              case Some(attribute) => true
              case None => false
            }).foreach {
              value =>
                if (scriptBuilder.last != '{') {
                  scriptBuilder append ", "
                }
                scriptBuilder append
                  value.getAttribute.getName + "=\"" + value.getValue + "\""
            }
            scriptBuilder append "}\n"
          } else {

            scriptBuilder append "\n"
          }

          // select a host for each user node
          if (potentialParents.isEmpty) {
            min = Int.MaxValue

            parents.foreach {
              parent => {
                if (parent._2 < min) {
                  min = parent._2
                }
              }
            }
            parents.foreach {
              parent => {
                if (parent._2 <= min) {
                  potentialParents = potentialParents ++ List(parent._1)
                }
              }
            }
          }
          val index = (java.lang.Math.random() * potentialParents.size).asInstanceOf[Int]
          scriptBuilder append "addChild " + node.getName + "@" + potentialParents.get(index) + "\n"

          // define IP using selecting node to know what it the network used in this machine
          val ipOption = selectIP(potentialParents.get(index), kloudModel)
          if (ipOption.isDefined) {
            scriptBuilder append "network " + node.getName + " {\"KEVOREE.remote.node.ip\" = \"" + ipOption.get + "\" }\n"
          }

          logger.debug("Add {} as child of {}", node.getName, potentialParents.get(index))
          potentialParents = potentialParents -- List(potentialParents.get(index))
      }

      //      scriptBuilder append "}"

      logger.debug("Try to apply the following script to kloudmodel to add all the user nodes:\n{}",
                    scriptBuilder.toString())

      val kengine = kevScriptEngineFactory.createKevScriptEngine(kloudModel)
      try {
        kengine.append(scriptBuilder.toString())
        Some(kengine.interpret())
      } catch {
        case _@e => {
          logger.debug("KevScript Error : ", e)
          None
        }
      }

    } else {
      Some(kloudModel)
    }
  }

  /**
   * configure the default user group into the kloud model and bind all the user nodes on it
   */
  def configureGroup (cleanNewUserModel: ContainerRoot, kloudModel: ContainerRoot, groupName: String, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    logger.debug("Try to add the user nodes on default group {} into the Kloud", groupName)

    // build kevscript to add user nodes into the kloud model
    val scriptBuilder = new StringBuilder()
    //    scriptBuilder append "tblock {\n"

    cleanNewUserModel.getNodes.foreach {
      node =>
        val addressOption = kloudModel.getNodes.find(n => n.getName == node.getName) match {
          case None => None
          case Some(knode) => {
            KevoreePropertyHelper.getStringNetworkProperty(kloudModel, knode.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
          }
        }
        var address = ""
        if (addressOption.isDefined) {
          address = addressOption.get
        }
        scriptBuilder append "addToGroup " + groupName + " " + node.getName + "\n"
        scriptBuilder append "updateDictionary " + groupName + " {port=\"" + KloudHelper.selectPortNumber(address) + "\"}@" + node.getName + "\n"
        if (addressOption.isDefined) {
          scriptBuilder append "updateDictionary " + groupName + " {ip=\"" + addressOption.get + "\"}@" + node.getName + "\n"
          scriptBuilder append "network " + node.getName + " {\"KEVOREE.remote.node.ip\" = \"" + addressOption.get + "\" }\n"
        }
    }


    //    scriptBuilder append "}"

    logger.debug("Try to apply the following script to kloudmodel to add the default group:\n{}", scriptBuilder.toString())

    val kengine = kevScriptEngineFactory.createKevScriptEngine(kloudModel)
    try {
      kengine.append(scriptBuilder.toString())
      Some(kengine.interpret())
    } catch {
      case _@e => {
        logger.debug("KevScript Error : ", e)
        None
      }
    }
  }

  /**
   * Send the user model into the user nodes using the default groups that are set on the cleanModel
   */
  def updateUserConfiguration (groupName: String, cleanModel: ContainerRoot, userModel: ContainerRoot,
    modelHandlerService: KevoreeModelHandlerService, kevScriptEngineFactory: KevScriptEngineFactory): Boolean = {

    val kloudModel = modelHandlerService.getLastModel
    val publicURLOption = KevoreePropertyHelper.getStringPropertyForGroup(kloudModel, groupName, "publicURL", false)
    if (publicURLOption.isDefined) {
      kloudModel.getGroups.find(g => g.getName == groupName) match {
        case None => false
        case Some(group) => {
          // build kevscript to add user nodes into the kloud model
          val scriptBuilder = new StringBuilder()
          //          scriptBuilder append "tblock {\n"

          scriptBuilder append "merge \"mvn:org.kevoree.library.sky/org.kevoree.library.sky.provider/" + KevoreeFactory.getVersion + "\"\n"

          scriptBuilder append
            "addGroup " + groupName + ":" + group.getTypeDefinition.getName + "{publicURL=\"" + publicURLOption.get + "\"}\n"

          group.getSubNodes.filter(n => cleanModel.getNodes.find(sn => sn.getName == n.getName) match {
            case None => false
            case Some(node) => true
          }).foreach {
            node =>
              val portOption = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, groupName, "port", true, node.getName)
              if (portOption.isDefined) {
                cleanModel.getNodes.foreach {
                  node =>
                    scriptBuilder append "addToGroup " + groupName + " " + node.getName + "\n"
                    scriptBuilder append "updateDictionary " + groupName + " {port=\"" + portOption.get + "\"}@" + node.getName + "\n"
                }
              } else {
                logger.debug("Unable to find port property for node {}", node.getName)
                false
              }
          }

          //          scriptBuilder append "}"

          logger.debug("Try to apply the following script to user model to add the default group:\n{}",
                        scriptBuilder.toString())


          val kengine = kevScriptEngineFactory.createKevScriptEngine(userModel)
          val newUserModelOption = (try {
            kengine.append(scriptBuilder.toString())
            Some(kengine.interpret())
          } catch {
            case _@e => {
              logger.debug("KevScript Error : ", e)
              None
            }
          })
          if (newUserModelOption.isDefined) {

            group.getSubNodes.filter(n => cleanModel.getNodes.find(sn => sn.getName == n.getName) match {
              case None => false
              case Some(node) => true
            }).forall {
              subNode =>

                val ipOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, subNode.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
                val portOption = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, group.getName, "port", true, subNode.getName)

                var ip = "127.0.0.1"
                if (ipOption.isDefined && ipOption.get != "") {
                  ip = ipOption.get.toString
                }
                var port = 8000
                if (portOption.isDefined && portOption.get != 0) {
                  port = portOption.get
                }
                val urlString = "http://" + ip + ":" + port + "/model/current"
                sendUserModel(urlString, newUserModelOption.get)
            }
          } else {
            logger.debug("Unable to add group on the user model")
            false
          }
        }
      }
    } else {
      logger.debug("Unable to add group on the user model")
      false
    }
  }

  private def sendUserModel (urlString: String, model: ContainerRoot): Boolean = {
    var isSend = false
    var i = 0
    while (!isSend && i < 20) {
      try {
        logger.debug("try to send user model at {}", urlString)
        val url = new URL(urlString)
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

        isSend = true
      } catch {
        case _@e => i += 1;
      }
      Thread.sleep(5000)
    }
    isSend
  }

  private def countChilds (kloudModel: ContainerRoot): List[(String, Int)] = {
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

  private def getDefaultNodeAttributes (kloudModel: ContainerRoot): List[DictionaryAttribute] = {
    kloudModel.getTypeDefinitions.find(td => td.getName == "PJavaSENode") match {
      case None => List[DictionaryAttribute]()
      case Some(td) =>
        td.getDictionaryType.get.getAttributes
    }
  }

  private def selectIP (parentNodeName: String, kloudModel: ContainerRoot): Option[String] = {
    logger.debug("try to select an IP for a child of {}", parentNodeName)
    kloudModel.getNodes.find(n => n.getName == parentNodeName) match {
      case None => None
      case Some(node) => {
        val subnetOption = KevoreePropertyHelper.getStringPropertyForNode(kloudModel, parentNodeName, "subnet")
        val maskOption = KevoreePropertyHelper.getStringPropertyForNode(kloudModel, parentNodeName, "mask")
        if (subnetOption.isDefined && maskOption.isDefined) {
          Some(lookingForNewIp(List(), subnetOption.get, maskOption.get))
        } else {
          None
        }
      }
    }
  }

  private def lookingForNewIp (ips: List[String], subnet: String, mask: String): String = {
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
            if (!ips.contains(tmpIp)) {
              val inet = InetAddress.getByName(tmpIp)
              if (!inet.isReachable(1000)) {
                newIp = tmpIp
                found = true
              }
            }
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
}