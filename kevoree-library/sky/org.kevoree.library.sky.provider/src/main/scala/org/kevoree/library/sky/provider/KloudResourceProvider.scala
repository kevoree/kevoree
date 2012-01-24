package org.kevoree.library.sky.provider

import org.kevoree.core.basechecker.RootChecker
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.JavaConversions._
import org.kevoree.cloner.ModelCloner
import org.kevoree.api.service.core.handler.{UUIDModel, KevoreeModelHandlerService}
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import java.net.URL
import org.kevoree.tools.marShell.KevsEngine
import java.io._
import org.kevoree.framework.{Constants, KevoreePropertyHelper}
import org.kevoree._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/01/12
 * Time: 15:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudResourceProvider {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * check if the model is valid
   */
  @Deprecated
  def check (model: ContainerRoot): Option[String] = {
    val checker: RootChecker = new RootChecker
    val violations = checker.check(model)
    if (violations.isEmpty) {
      None
    } else {
      val result = "Unable to deploy this software on the Kloud because there is some constraints violations:\n" +
        violations.mkString("\n")
      logger.error(result)
      Some(result)

    }
  }

  /**
   * get clean model with only nodes and without components, channels
   * only one group is kept and this group is the default group automatically added
   */
  @Deprecated
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

    val scriptBuilder = new StringBuilder()
    scriptBuilder append "tblock {\n"
    // add a group to link all platform between them (if there is not)
    scriptBuilder append
      "merge \"mvn:org.kevoree.library.javase/org.kevoree.library.javase.rest/" + KevoreeFactory.getVersion + "\"\n"

    val number = (java.lang.Math.random() * (Integer.MAX_VALUE - 1)).asInstanceOf[Int]
    scriptBuilder append "addGroup sync" + number + " : RestGroup\n"

    model.getNodes.foreach {
      node =>
        scriptBuilder append "addToGroup sync" + number + " " + node.getName + "\n"
        scriptBuilder append "updateDictionary sync" + number + " {port=\"8000\"}@" + node.getName + "\n"
    }

    scriptBuilder append "}"

    logger.debug("Try to apply the following script to usermodel:\n{}", scriptBuilder.toString())

    KevsEngine.executeScript(scriptBuilder.toString(), cleanModel)
  }

  @Deprecated
  private def getDefaultNodeAttributes (kloudModel: ContainerRoot): List[DictionaryAttribute] = {
    kloudModel.getTypeDefinitions.find(td => td.getName == "PJavaSENode") match {
      case None => List[DictionaryAttribute]()
      case Some(td) =>
        td.getDictionaryType.get.getAttributes
    }
  }

  /**
   * Distribute the model into the kloudModel
   * all node are disseminate on parent node
   * the groups that are into the model are replicated on the kloudModel
   * A parent node is defined by two adaptation primitives <b>addNode</b> and <b>removeNode</b>
   */
  @Deprecated
  def distribute (model: ContainerRoot, kloudModel: ContainerRoot): Option[ContainerRoot] = {
    logger.debug("Try to distribute all the user nodes into the Kloud")

    // build kevscript to apply user model into the kloud model
    val scriptBuilder = new StringBuilder()
    scriptBuilder append "tblock {\n"

    // count current child for each Parent nodes
    val parents = countChilds(kloudModel)

    var min = Int.MaxValue
    var potentialParents = List[String]()

    // create new node using PJavaSENode as type for each user node
    model.getNodes.foreach {
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

        logger.debug("Add {} as child of {}", node.getName, potentialParents.get(index))
        potentialParents = potentialParents -- List(potentialParents.get(index))
    }
    // copy the default user group into the kloudModel
    model.getGroups.foreach {
      group =>
        scriptBuilder append "addGroup " + group.getName + " : " + group.getTypeDefinition.getName + "\n"
        group.getSubNodes.foreach {
          subNode =>
            scriptBuilder append "addToGroup " + group.getName + " " + subNode.getName + "\n"
            scriptBuilder append "updateDictionary " + group.getName + " {port=\"8000\"}@" + subNode.getName + "\n"
        }
    }

    scriptBuilder append "}"

    logger.debug("Try to apply the following script to kloudmodel to distribute all the user nodes:\n{}",
                  scriptBuilder.toString())

    KevsEngine.executeScript(scriptBuilder.toString(), kloudModel)
  }

  /**
   * Update the current kloud configuration using UUIDModel to ensure that updates are based on last version of the current model
   */
  @Deprecated
  def update (uuidModel: UUIDModel, model: ContainerRoot, modelHandlerService: KevoreeModelHandlerService): Boolean = {
    try {
      modelHandlerService.atomicCompareAndSwapModel(uuidModel, model)
      true
    } catch {
      case _@e =>
        logger.debug("Unable to swap model, maybe because the new model is based on an old configuration", e);
        false
    }
  }

  def addProxy (globalModel: ContainerRoot, userModel: ContainerRoot, modelHandlerService: KevoreeModelHandlerService,
    kevScriptEngineFactory: KevScriptEngineFactory, nodeName: String, login: String): Boolean = {
    // TODO maybe to refactor

    logger.debug("Try to add proxy between user model and the Kloud access point")
    // define the web address where the group is available
    val webAddress = userModel.getGroups
      .find(group => group.getName.startsWith("sync") && group.getSubNodes.size == userModel.getNodes.size) match {
      case Some(group) => globalModel.getGroups.find(g => g.getName == group.getName) match {
        case Some(g) =>
          val optionPort = KevoreePropertyHelper
            .getIntPropertyForGroup(globalModel, g.getName, "port", true, userModel.getNodes(0).getName)
          if (optionPort.isDefined && optionPort.get != 0) {
            val optionAddress = KevoreePropertyHelper
              .getPropertyForNode(globalModel, userModel.getNodes(0).getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
            if (optionAddress.isDefined && optionAddress.get != "") {
              Some(optionAddress.get + ":" + optionPort.get)
            } else {
              Some("127.0.0.1:" + optionPort.get)
            }
          } else {
            None
          }
        case None => None
      }
      case None => None
    }
    if (webAddress.isDefined) {
      // add a proxy page to allow the user to get and update the configuration of its nodes
      val scriptBuilder = new StringBuilder()
      scriptBuilder append "tblock {"
      // merge the needed library to add a proxy page
      scriptBuilder append
        "merge  mvn:org.kevoree.library.javase/org.kevoree.library.javase.webserver.components/" +
          KevoreeFactory.getVersion
      scriptBuilder append "addComponent Proxy" + login + ": ProxyPage"

      scriptBuilder append
        "UpdateDictionary Proxy" + login + " {urlPattern=\"configuration/" + login + "\", forward=\"" + webAddress +
          "\"}"

      scriptBuilder append "}"
      try {
        val newGlobalModel = kevScriptEngineFactory.createKevScriptEngine().append(scriptBuilder.toString()).interpret()
        modelHandlerService.updateModel(newGlobalModel)
        true
      } catch {
        case _@e => logger
          .error("Unable to apply KevScript to add a group that manage the overall software.", e);
        false
      }
    } else {
      false
    }
  }

  /**
   * Send the user model into the user nodes using the default groups that are set on the cleanModel
   */
  @Deprecated
  def updateUserConfiguration (cleanModel: ContainerRoot, model: ContainerRoot,
    modelHandlerService: KevoreeModelHandlerService): Boolean = {
    val kloudModel = modelHandlerService.getLastModel
    cleanModel.getGroups.forall {
      group =>
        group.getSubNodes.forall {
          subNode =>
            val ipOption = KevoreePropertyHelper
              .getPropertyForNode(kloudModel, subNode.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
            val portOption = KevoreePropertyHelper
              .getIntPropertyForGroup(cleanModel, group.getName, "port", true, subNode.getName)

            var ip = "127.0.0.1"
            if (ipOption.isDefined && ipOption.get != "") {
              ip = ipOption.get.toString
            }
            var port = 8000
            if (portOption.isDefined && portOption.get != 0) {
              port = portOption.get
            }
            val urlString = "http://" + ip + ":" + port + "/model/current"
            sendUserModel(urlString, model)
        }
    }
  }

  @Deprecated
  private def sendUserModel (urlString: String, model: ContainerRoot): Boolean = {
    val outStream = new ByteArrayOutputStream
    val url = new URL(urlString);
    logger.debug("send new model to " + urlString)
    val conn = url.openConnection();
    conn.setConnectTimeout(2000);
    conn.setDoOutput(true);
    val wr = new OutputStreamWriter(conn.getOutputStream)
    wr.write(outStream.toString);
    wr.flush();

    // Get the response
    val rd = new BufferedReader(new InputStreamReader(conn.getInputStream));
    var line: String = rd.readLine;
    while (line != null) {
      line = rd.readLine
    }
    wr.close();
    rd.close();

    logger.debug("model sent to " + urlString)
    true
  }

  @Deprecated
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


}