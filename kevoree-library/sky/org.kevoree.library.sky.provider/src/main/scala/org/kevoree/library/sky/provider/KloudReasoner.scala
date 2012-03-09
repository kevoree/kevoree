package org.kevoree.library.sky.provider

import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.{ContainerNode, KevoreeFactory, ContainerRoot}
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import scala.collection.JavaConversions._
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/02/12
 * Time: 17:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudReasoner {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

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

  def createProxy (groupName: String, nodeName: String, proxyPath: String, kloudModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {

    val scriptBuilder = new StringBuilder()
    //find Web Server
    kloudModel.getNodes.find(n => n.getName == nodeName) match {
      case None => logger.debug("Any proxy can be added because there is no webserver to use"); None
      case Some(node) => node.getComponents.find(c => c.getTypeDefinition.getName == "WebServer" || KloudHelper.isASubType(c.getTypeDefinition, "WebServer")) match {
        case None => logger.debug("Any proxy can be added because there is no webserver to use"); None
        case Some(component) => {
          val ipOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
          val portOption = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, groupName, "port", true, nodeName)
          if (ipOption.isDefined && portOption.isDefined) {
            val requestChannelNameOption = KloudHelper.findChannel(component.getName, "handler", node.getName, kloudModel)
            val responseChannelNameOption = KloudHelper.findChannel(component.getName, "response", node.getName, kloudModel)
            if (requestChannelNameOption.isDefined && responseChannelNameOption.isDefined) {
              scriptBuilder append "merge \"mvn:org.kevoree.library.javase/org.kevoree.library.javase.webserver.components/" + KevoreeFactory.getVersion + "\"\n"
              scriptBuilder append
                "addComponent " + groupName + "_proxy@" + nodeName + " : ProxyPage " + "{forward=\"" + "http://" + ipOption.get + ":" + portOption.get + "/model/current" + "\",urlpattern=\"" +
                  proxyPath + "\"}\n"

              // bind Web Server with this proxy
              scriptBuilder append "bind " + groupName + "_proxy" + ".request@" + nodeName + " => " + requestChannelNameOption.get + "\n"
              scriptBuilder append "bind " + groupName + "_proxy" + ".content@" + nodeName + " => " + responseChannelNameOption.get + "\n"


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
              logger.debug("Unable to build the forward URL to {} on {}", groupName, nodeName)
              None
            }
          } else {
            logger.debug("Unable to find IP and port data to {} on {}", groupName, nodeName)
            None
          }
        }
      }
    }
  }

  def removeProxy (groupName: String, nodeName: String, modelHandlerService: KevoreeModelHandlerService, kevScriptEngineFactory: KevScriptEngineFactory, nbTry: Int): Boolean = {
    val scriptBuilder = new StringBuilder()
    val uuidModel = modelHandlerService.getLastUUIDModel

    val requestChannelNameOption = KloudHelper.findChannel(groupName + "_proxy", "request", nodeName, uuidModel.getModel)
    val responseChannelNameOption = KloudHelper.findChannel(groupName + "_proxy", "content", nodeName, uuidModel.getModel)

    // bind Web Server with this proxy
    scriptBuilder append "unbind " + groupName + "_proxy" + ".request@" + nodeName + " => " + requestChannelNameOption.get + "\n"
    scriptBuilder append "unbind " + groupName + "_proxy" + ".content@" + nodeName + " => " + responseChannelNameOption.get + "\n"


    scriptBuilder append "removeComponent " + groupName + "_proxy @ " + nodeName + "\n"

    val kengine = kevScriptEngineFactory.createKevScriptEngine(uuidModel.getModel)
    try {
      kengine.append(scriptBuilder.toString())
      modelHandlerService.atomicCompareAndSwapModel(uuidModel, kengine.interpret())
      true
    } catch {
      case _@e => {
        if (nbTry == 0) {
          logger.debug("Unable to remove the component {}_proxy: ", groupName, e)
          false
        } else {
          removeProxy(groupName, nodeName, modelHandlerService, kevScriptEngineFactory, nbTry - 1)
        }
      }
    }
  }

  def createGroup (groupName: String, nodeName: String, kloudModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory, sshKey: String = "", displayIP: String): Option[ContainerRoot] = {
    kloudModel.getGroups.find(g => g.getName == groupName && g.getTypeDefinition.getName == "KloudPaaSNanoGroup") match {
      case Some(g) => Some(kloudModel)
      case None => {

        val ipOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
        val portNumber = if (ipOption.isDefined) {
          KloudHelper.selectPortNumber(ipOption.get, Array[Int]())
        } else {
          KloudHelper.selectPortNumber("", Array[Int]())
        }
        val scriptBuilder = new StringBuilder()

        scriptBuilder append
          "addGroup " + groupName + " : KloudPaaSNanoGroup {SSH_Public_Key=\"" + sshKey + "\", masterNode=\"http://" + ipOption.get + ":" + portNumber + "/model/current\", displayIP=\"" + displayIP +
            "\"}\n"

        scriptBuilder append "addToGroup " + groupName + " " + nodeName + "\n"

        var updateDictionary = "updateDictionary " + groupName + " {port=\"" + portNumber + "\"}@" + nodeName + "\n"
        if (ipOption.isDefined) {
          updateDictionary = "updateDictionary " + groupName + " {port=\"" + portNumber + "\", ip=\"" + ipOption.get + "\"}@" + nodeName + "\n"
        }
        scriptBuilder append updateDictionary

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
    }
  }

  def removeGroup (groupName: String, modelHandlerService: KevoreeModelHandlerService, kevScriptEngineFactory: KevScriptEngineFactory, nbTry: Int): Boolean = {
    val scriptBuilder = new StringBuilder()
    val uuidModel = modelHandlerService.getLastUUIDModel
    scriptBuilder append "removeGroup " + groupName + "\n"

    val kengine = kevScriptEngineFactory.createKevScriptEngine(uuidModel.getModel)
    try {
      kengine.append(scriptBuilder.toString())
      modelHandlerService.atomicCompareAndSwapModel(uuidModel, kengine.interpret())
      true
    } catch {
      case _@e => {
        if (nbTry == 0) {
          logger.debug("Unable to remove the group {}: ", groupName, e)
          false
        } else {
          removeGroup(groupName, modelHandlerService, kevScriptEngineFactory, nbTry - 1)
        }
      }
    }
  }

  /**
   * compare models and built a tuple of sets of added nodes and removed nodes
   */
  def getAddAndRemove (newModel: ContainerRoot, userModel: ContainerRoot): (List[ContainerNode], List[ContainerNode]) = {
    var addedNodes = List[ContainerNode]()
    var removedNodes = List[ContainerNode]()
    userModel.getNodes.foreach {
      userNode =>
        newModel.getNodes.find(node => node.getName == userNode.getName) match {
          case None => {
            logger.debug("{} must be removed from the kloud.", userNode.getName)
            removedNodes = removedNodes ++ List[ContainerNode](userNode)
          }
          case Some(newUserNode) =>
        }
    }
    newModel.getNodes.foreach {
      newUserNode =>
        userModel.getNodes.find(node => node.getName == newUserNode.getName) match {
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
  def addNodes (groupName: String, addedNodes: List[ContainerNode], kloudModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    if (!addedNodes.isEmpty) {
      logger.debug("Try to add all user nodes into the Kloud")

      // build kevscript to add user nodes into the kloud model
      val scriptBuilder = new StringBuilder()

      // create new node using PJavaSENode as type for each user node
      addedNodes.foreach {
        node =>
        // add node
          scriptBuilder append "addNode " + node.getName + " : PJavaSENode "
          // set dictionary attributes of node
          if (node.getDictionary.isDefined) {
            scriptBuilder append "{"
            val defaultAttributes = KloudHelper.getDefaultNodeAttributes(kloudModel, "PJavaSENode")
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
      }

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

  def configureChildNodes (kloudModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    // count current child for each Parent nodes
    val parents = KloudHelper.countChilds(kloudModel)

    var min = Int.MaxValue
    var potentialParents = List[String]()

    val scriptBuilder = new StringBuilder()

    // filter nodes that are not IaaSNode and are not child of IaaSNode
    kloudModel.getNodes.filter(n => (n.getTypeDefinition.getName == "PJavaSENode" || KloudHelper.isASubType(n.getTypeDefinition, "PJavaSENode"))
      && kloudModel.getNodes.forall(parent => !parent.getHosts.contains(n))).foreach {
      node => {
        logger.debug("try to select a parent for {}", node.getName)
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
        scriptBuilder append "addChild " + node.getName + "@" + potentialParents(index) + "\n"

        // define IP using selecting node to know what it the network used in this machine
        val ipOption = KloudHelper.selectIP(potentialParents(index), kloudModel)
        if (ipOption.isDefined) {
          scriptBuilder append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + ipOption.get + "\" }\n"
        } else {
          logger.debug("Unable to select an IP for {}", node.getName)
        }

        logger.debug("Add {} as child of {}", node.getName, potentialParents(index))
        potentialParents = potentialParents.filterNot(p => p == potentialParents(index))
        //        isEmpty = false
      }
    }

    if (scriptBuilder.toString() != "") {
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
      logger.debug("No child to add")
      None
    }
  }

  /**
   * configure the default user group into the kloud model and bind all the user nodes on it
   */
  def configureGroup (userModel: ContainerRoot, kloudModel: ContainerRoot, groupName: String, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    logger.debug("Try to add the user nodes on default group {} into the Kloud", groupName)
    kloudModel.getGroups.find(g => g.getName == groupName) match {
      case None => logger.error("Unable to find the group {}", groupName); None //must never appear
      case Some(group) =>
        if (group.getSubNodes.size - 1 /*due to master node*/ == userModel.getNodes.size && group.getTypeDefinition.getName == "KloudPaaSNanoGroup") {
          Some(kloudModel)
        } else {
          // build kevscript to add user nodes into the kloud model
          val scriptBuilder = new StringBuilder()

          var ports = Array[Int]()

          userModel.getNodes.foreach {
            node =>
              group.getSubNodes.find(n => n.getName == node.getName) match {
                case Some(n) =>
                case None => {
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
                  val port = KloudHelper.selectPortNumber(address, ports)
                  ports = ports ++ Array[Int](port)

                  scriptBuilder append "addToGroup " + groupName + " " + node.getName + "\n"
                  scriptBuilder append "updateDictionary " + groupName + " {port=\"" + port + "\"}@" + node.getName + "\n"
                  if (addressOption.isDefined) {
                    scriptBuilder append "updateDictionary " + groupName + " {ip=\"" + addressOption.get + "\"}@" + node.getName + "\n"
                    scriptBuilder append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + addressOption.get + "\"}\n"
                  }
                }
              }
          }

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
    }
  }

  /**
   * compute a new deployment and apply it
   */
  def processDeployment (newModel: ContainerRoot, userModel: ContainerRoot, modelHandlerService: KevoreeModelHandlerService, kevScripEngineFactory: KevScriptEngineFactory,
    groupName: String): Boolean = {
    // check validity of the new model
    val resultOption = KloudHelper.check(newModel)
    if (resultOption.isEmpty) {

      // clean newModel and userModel: get only nodes and one group
      val cleanedNewModelOption = KloudHelper.cleanUserModel(newModel)
      val cleanedUserModelOption = KloudHelper.cleanUserModel(userModel)
      if (cleanedNewModelOption.isDefined && cleanedUserModelOption.isDefined) {

        // compare newModel and userModel to know which nodes must be added or removed
        val comparison = getAddAndRemove(cleanedNewModelOption.get, cleanedUserModelOption.get)
        val addedNodes = comparison._1
        val removedNodes = comparison._2

        val uuidModel = modelHandlerService.getLastUUIDModel

        // remove useless nodes on the Kloud model
        var newKloudModelOption = removeNodes(removedNodes, uuidModel.getModel, kevScripEngineFactory)
        if (newKloudModelOption.isDefined) {

          // distribute the new user nodes on the Kloud model
          newKloudModelOption = addNodes(groupName, addedNodes, newKloudModelOption.get, kevScripEngineFactory)
          if (newKloudModelOption.isDefined) {

            // add the default group or bind this group with all user nodes
            newKloudModelOption = configureGroup(cleanedNewModelOption.get, newKloudModelOption.get, groupName, kevScripEngineFactory)
            if (newKloudModelOption.isDefined) {

              // update the Kloud model with the result of the distribution
              try {
                modelHandlerService.atomicCompareAndSwapModel(uuidModel, newKloudModelOption.get)
                true
              } catch {
                case _@e =>
                  logger.debug("Unable to swap model, maybe because the new model is based on a too old configuration", e)
                  false
              }
            } else {
              logger.debug("Unable to configure you access point to your nodes.")
              false
            }
          } else {
            logger.debug("Unable to define the user nodes.")
            false
          }
        } else {
          logger.debug("Unable to define user nodes")
          false
        }
      } else {
        logger.debug("Unable to manipulate user model.")
        false
      }
    } else {
      logger.debug(resultOption.get)
      false
    }
  }

  def updateUserConfiguration (groupName: String, userModel: ContainerRoot, modelHandlerService: KevoreeModelHandlerService,
    kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {
    // FIXME add IP for each node
    val isUpToDate = userModel.getGroups.find(g => g.getName == groupName) match {
      case None => false
      case Some(group) =>
        if (group.getSubNodes.size == userModel.getNodes.size && group.getTypeDefinition.getName == "KloudPaaSNanoGroup") {
          true
        } else {
          false
        }
    }

    if (!isUpToDate) {
      // build kevscript to add user nodes into the kloud model
      val scriptBuilder = new StringBuilder()


      val kloudModel = modelHandlerService.getLastModel
      kloudModel.getGroups.find(g => g.getName == groupName) match {
        // && g.getSubNodes.size == userModel.getNodes.size && g.getTypeDefinition.getName == "KloudPaaSNanoGroup"
        case None => logger.error("Unable to find the group on the Kloud model"); None // must never appear
        case Some(group) => {
          userModel.getGroups.find(g => g.getName == groupName) match {
            case Some(g) => {
              userModel.getNodes.foreach {
                node =>
                  g.getSubNodes.find(n => n.getName == node.getName) match {
                    case Some(n) =>
                    case None => {
                      val portOption = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, groupName, "port", true, node.getName)
                      if (portOption.isDefined) {
                        scriptBuilder append "addToGroup " + groupName + " " + node.getName + "\n"
                        scriptBuilder append "updateDictionary " + groupName + " {port=\"" + portOption.get + "\"}@" + node.getName + "\n"
                      }/* else {
                        logger.debug("Unable to find port property for node {}", node.getName)
                      }*/
                      // set IP of user nodes if needed
                      val displayIPOption = KevoreePropertyHelper.getBooleanPropertyForGroup(kloudModel, groupName, "displayIP")
                      val addressOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
                      if (displayIPOption.isDefined && displayIPOption.get && addressOption.isDefined) {
                        scriptBuilder append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + addressOption.get + "\" }\n"
                      }
                    }
                  }
              }
            }
            case None => {
              scriptBuilder append "merge \"mvn:org.kevoree.library.sky/org.kevoree.library.sky.provider/" + KevoreeFactory.getVersion + "\"\n"

              // build url address to connect to the master node
              /*val masterNodeOption = KevoreePropertyHelper.getStringPropertyForGroup(kloudModel, groupName, "masterNode")
              var masterNode = "http://127.0.0.1:8000/model/current"
              if (masterNodeOption.isDefined) {
                masterNode = masterNodeOption.get
              }

              val displayIPOption = KevoreePropertyHelper.getStringPropertyForGroup(kloudModel, groupName, "masterNode")
              var displayIP = "http://127.0.0.1:8000/model/current"
              if (displayIPOption.isDefined) {
                displayIP = displayIPOption.get
              }*/

              scriptBuilder append "addGroup " + groupName + " : KloudPaaSNanoGroup"

              // set dictionary attributes of node
              if (group.getDictionary.isDefined) {
                scriptBuilder append "{"

                group.getDictionary.get.getValues.filter(v => v.getTargetNode.isEmpty).foreach {
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

              userModel.getNodes.foreach {
                node =>
                  val portOption = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, groupName, "port", true, node.getName)
                  if (portOption.isDefined) {
                    scriptBuilder append "addToGroup " + groupName + " " + node.getName + "\n"
                    scriptBuilder append "updateDictionary " + groupName + " {port=\"" + portOption.get + "\"}@" + node.getName + "\n"
                  } /*else {
                    logger.debug("Unable to find port property for node {}", node.getName)
                  }*/
                  // set IP of user nodes if needed
                  val displayIPOption = KevoreePropertyHelper.getBooleanPropertyForGroup(kloudModel, groupName, "displayIP")
                  val addressOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
                  if (displayIPOption.isDefined && addressOption.isDefined) {
                    scriptBuilder append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + addressOption.get + "\" }\n"
                  }
              }
            }
          }

          /*userModel.getNodes.foreach {
            node =>
              group.getSubNodes.find(n => n.getName == node.getName) match {
                case Some(n) =>
                case None => {
                  val portOption = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, groupName, "port", true, node.getName)
                  if (portOption.isDefined) {
                    scriptBuilder append "addToGroup " + groupName + " " + node.getName + "\n"
                    scriptBuilder append "updateDictionary " + groupName + " {port=\"" + portOption.get + "\"}@" + node.getName + "\n"
                  } else {
                    logger.debug("Unable to find port property for node {}", node.getName)
                  }
                  // set IP of user nodes if needed
                  val displayIPOption = KevoreePropertyHelper.getBooleanPropertyForGroup(kloudModel, groupName, "displayIP")
                  val addressOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
                  if (displayIPOption.isDefined && addressOption.isDefined) {
                    scriptBuilder append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + addressOption.get + "\" }\n"
                  }
                }
              }
          }*/

          val kengine = kevScriptEngineFactory.createKevScriptEngine(userModel)
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
      }
    } else {
      Some(userModel)
    }
  }

  /**
   * Send the user model into the user nodes using the default groups that are set on the cleanModel
   */
  def sendUserConfiguration (groupName: String, userModel: ContainerRoot, modelHandlerService: KevoreeModelHandlerService, kevScriptEngineFactory: KevScriptEngineFactory, nodeName: String) {
    //    val newUserModelOption = updateUserConfiguration(groupName, userModel, modelHandlerService, kevScriptEngineFactory)
    val newUserModelOption = Some(userModel)
    if (newUserModelOption.isDefined) {
      val kloudModel = modelHandlerService.getLastModel
      kloudModel.getGroups.find(g => g.getName == groupName) match {
        case None =>
        case Some(group) => {
          group.getSubNodes.filter(n => userModel.getNodes.find(sn => sn.getName == n.getName) match {
            case None => false
            case Some(node) => true
          }).foreach {
            subNode =>
              val ips: java.util.List[String] = KevoreePropertyHelper.getStringNetworkProperties(kloudModel, subNode.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
              val portOption: Option[Integer] = KevoreePropertyHelper.getIntPropertyForGroup(kloudModel, groupName, "port", true, subNode.getName)
              var PORT: Int = 8000
              if (portOption.isDefined) {
                PORT = portOption.get
              }

              var param = "";
              if (!nodeName.equals("")) {
                param = "?nodesrc=" + nodeName;
              }

              ips.forall {
                ip =>
                  logger.debug("try to send model on url=>{}", "http://" + ip + ":" + PORT + "/model/current" + param)
                  !KloudHelper.sendUserModel("http://" + ip + ":" + PORT + "/model/current" + param, newUserModelOption.get, 1)
              }
          }
        }
      }
    } else {
      logger.debug("Unable to update the user configuration")
    }
  }
}
