package org.kevoree.library.sky.provider

import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.{ContainerNode, KevoreeFactory, ContainerRoot}
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.{Constants, KevoreePropertyHelper}
import org.kevoree.api.service.core.script.{KevScriptEngine, KevScriptEngineFactory}

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

  def appendScriptToCleanupIaaSModelFromUser (kengine: KevScriptEngine, login: String, currentIaaSModel: ContainerRoot) {
    currentIaaSModel.getGroups.find(g => g.getName == login) match {
      case None => logger.warn("No Group found, nothing to cleanup")
      case Some(userGroup) => {
        userGroup.getSubNodes.foreach {
          sub =>
            kengine.append("removeNode " + sub.getName)
        }
        kengine.append("removeGroup " + userGroup.getName)
      }
    }
  }


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

    // val scriptBuilder = new StringBuilder()
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


              val kengine = kevScriptEngineFactory.createKevScriptEngine(kloudModel)
              kengine.addVariable("kevVersion", KevoreeFactory.getVersion)
              kengine.append("merge \"mvn:org.kevoree.library.javase/org.kevoree.library.javase.webserver.components/{kevVersion}")
              kengine.addVariable("groupName", groupName)
              kengine.addVariable("nodeName", nodeName)
              kengine.addVariable("ip", ipOption.get)
              kengine.addVariable("port", portOption.get.toString)
              kengine.addVariable("proxyPath", proxyPath)
              kengine.append("addComponent {groupName}_proxy@{nodeName} : ProxyPage " + "{forward=\"" + "http://{ip}:{port}/model/current" + "\",urlpattern=\"{proxyPath}\"}")

              // bind Web Server with this proxy
              kengine.addVariable("responseChannelNameOption", responseChannelNameOption.get)
              kengine.addVariable("requestChannelNameOption", requestChannelNameOption.get)
              kengine.append("bind {groupName}_proxy.request@{nodeName} => {requestChannelNameOption}")
              kengine.append("bind {groupName}_proxy.content@{nodeName} => {responseChannelNameOption}")

              try {
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

  def appendCreateGroupScript (kloudModel: ContainerRoot, login: String, nodeName: String, kevScriptEngine: KevScriptEngine, sshKey: String = "") {
    val ipOption = KevoreePropertyHelper.getStringNetworkProperty(kloudModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var ip = "127.0.0.1"
    if (ipOption.isDefined) {
      ip = ipOption.get
    }
    /* Warning This method try severals Socket to determine available port */
    val portNumber = KloudHelper.selectPortNumber(ip, Array[Int]())
    kevScriptEngine.addVariable("groupName", login)
    kevScriptEngine.addVariable("nodeName", nodeName)
    kevScriptEngine.addVariable("ip", ip)
    kevScriptEngine.addVariable("port", portNumber.toString)

    kevScriptEngine append "addGroup {groupName} : KloudPaaSNanoGroup {masterNode='{nodeName}={ip}:{port}'}"
    if (sshKey != null) {
      kevScriptEngine append "updateDictionary {groupName} {SSH_Public_Key='" + sshKey + "'}"
    }
    kevScriptEngine append "addToGroup {groupName} {nodeName}"
    kevScriptEngine append "updateDictionary {groupName} {port='{port}', ip='{ip}'}@{nodeName}"

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

  def removeNodes (removedNodes: List[ContainerNode], kloudModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
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
              kengine append "removeChild " + node.getName + "@" + parent.getName
              kengine append "removeFromGroup * " + node.getName
              kengine append "removeNode " + node.getName
          }

      }
      true
    } else {
      true
    }
  }

  /**
   * all node are disseminate on parent node
   * A parent node is defined by two adaptation primitives <b>addNode</b> and <b>removeNode</b>
   */
  def addNodes (groupName: String, addedNodes: List[ContainerNode], kloudModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
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
            scriptBuilder append "}"
          }
          kengine append scriptBuilder.toString()
      }
      true

    } else {
      true
    }
  }

  def configureChildNodes (kloudModel: ContainerRoot, kengine: KevScriptEngine): Boolean = {
    // count current child for each Parent nodes
    val parents = KloudHelper.countChilds(kloudModel)

    var min = Int.MaxValue
    var potentialParents = List[String]()

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
        kengine append "addChild " + node.getName + "@" + potentialParents(index) + "\n"

        // define IP using selecting node to know what it the network used in this machine
        val ipOption = KloudHelper.selectIP(potentialParents(index), kloudModel)
        if (ipOption.isDefined) {
          kengine append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + ipOption.get + "\" }\n"
        } else {
          logger.debug("Unable to select an IP for {}", node.getName)
        }

        logger.debug("Add {} as child of {}", node.getName, potentialParents(index))
        potentialParents = potentialParents.filterNot(p => p == potentialParents(index))
        //        isEmpty = false
      }
    }
    true
  }

  /**
   * configure the default user group into the kloud model and bind all the user nodes on it
   */
  def configureGroup (userModel: ContainerRoot, kloudModel: ContainerRoot, groupName: String, kengine: KevScriptEngine): Boolean = {
    logger.debug("Try to add the user nodes on default group {} into the Kloud", groupName)
    kloudModel.getGroups.find(g => g.getName == groupName) match {
      case None => logger.error("Unable to find the group {}", groupName); false //must never appear
      case Some(group) =>
        if (group.getSubNodes.size - 1 /*due to master node*/ == userModel.getNodes.size && group.getTypeDefinition.getName == "KloudPaaSNanoGroup") {
          true
        } else {
          // build kevscript to add user nodes into the kloud model
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
                  /* Warning This method try severals Socket to determine available port */
                  val port = KloudHelper.selectPortNumber(address, ports)
                  ports = ports ++ Array[Int](port)

                  kengine append "addToGroup " + groupName + " " + node.getName
                  kengine append "updateDictionary " + groupName + " {port=\"" + port + "\"}@" + node.getName
                  if (addressOption.isDefined) {
                    kengine append "updateDictionary " + groupName + " {ip=\"" + addressOption.get + "\"}@" + node.getName
                    kengine append "network " + node.getName + " {\"" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "\" = \"" + addressOption.get + "\"}"
                  }
                }
              }
          }
          true
        }
    }
  }

  /**
   * compute a new deployment and apply it
   */
  def processDeployment (newModel: ContainerRoot, userModel: ContainerRoot, kloudModel: ContainerRoot, kengine: KevScriptEngine, groupName: String): Boolean = {

    // compare newModel and userModel to know which nodes must be added or removed
    val comparison = getAddAndRemove(newModel, userModel)
    val addedNodes = comparison._1
    val removedNodes = comparison._2

    // remove useless nodes on the Kloud model
    if (removeNodes(removedNodes, kloudModel, kengine)) {

      // distribute the new user nodes on the Kloud model
      if (addNodes(groupName, addedNodes, kloudModel, kengine)) {

        // add the default group or bind this group with all user nodes
        configureGroup(newModel, kloudModel, groupName, kengine)
      } else {
        logger.debug("Unable to define the user nodes.")
        false
      }
    } else {
      logger.debug("Unable to define user nodes")
      false
    }
  }

  def updateUserConfiguration (login: String, userModel: ContainerRoot, iaasModel: ContainerRoot, kevScriptEngineFactory: KevScriptEngineFactory): Option[ContainerRoot] = {

    val kengine = kevScriptEngineFactory.createKevScriptEngine(userModel)
    kengine.addVariable("kevVersion", KevoreeFactory.getVersion)
    kengine.addVariable("groupName", login)
    val groupType = "KloudPaaSNanoGroup"
    kengine.addVariable("groupType", groupType)

    //CHECK TYPPE
    if (userModel.getTypeDefinitions.find(td => td.getName == groupType).isEmpty) {
      kengine append "merge \"mvn:org.kevoree.library.sky/org.kevoree.library.sky.provider/{kevVersion}\""
    }
    kengine.append("addGroup {groupName} : {groupType}")
    //FOUND IAAS GROUP
    iaasModel.getGroups.find(g => g.getName == login) match {
      case Some(iaasPreviousGroup) => {
        //COPY NON FRAGMENT DEPENDANT PROPERTY
        iaasPreviousGroup.getDictionary.get.getValues.filter(v => v.getTargetNode.isEmpty).foreach {
          value =>
            kengine.append("updateDictionary {groupName} { " + value.getAttribute.getName + "='" + value.getValue + "' }")
        }
        userModel.getNodes.foreach {
          node =>
            kengine.addVariable("nodeName", node.getName)
            kengine.append("addToGroup {groupName} {nodeName}")
            val portOption = KevoreePropertyHelper.getIntPropertyForGroup(iaasModel, login, "port", true, node.getName)
            if (portOption.isDefined) {
              kengine append "updateDictionary {groupName} {port='" + portOption.get + "'}@{nodeName}"
            } else {
              kengine append "updateDictionary {groupName} {port='8000'}@{nodeName}" // this value will be override later by kloud update
            }
            // set IP of user nodes if needed
            val displayIPOption = KevoreePropertyHelper.getBooleanPropertyForGroup(iaasModel, login, "displayIP")
            val addressOption = KevoreePropertyHelper.getStringNetworkProperty(iaasModel, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
            if (displayIPOption.isDefined && addressOption.isDefined) {
              kengine append "network {nodeName} {'" + Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP + "' = '" + addressOption.get + "' }"
            }
        }
        try {
          Some(kengine.interpret())
        } catch {
          case _@e => logger.warn("Error while updating user model configuration"); None
        }

      }
      case None => None
    }
  }
}
