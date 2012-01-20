package org.kevoree.library.sky.provider

import org.kevoree.api.service.core.handler.{KevoreeModelHandlerService, UUIDModel}
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.{TypeDefinition, ContainerRoot}
import org.kevoree.framework.{Constants, KevoreePropertyHelper}
import java.net.URL
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, ByteArrayOutputStream}

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
            node.getTypeDefinition.getName == "IaaSNode" || isASubType(node.getTypeDefinition, "IaaSNode")
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
            node.getTypeDefinition.getName == "PJavaSENode" || isASubType(node.getTypeDefinition, "PJavaSENode")
        }
    }
  }

  private def isASubType (nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
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
  def processDeployment (userModel: ContainerRoot /*, login: String*/ , modelHandlerService: KevoreeModelHandlerService,
    kevScripEngineFactory: KevScriptEngineFactory, groupName: String, nodeName: String) {

    val result = KloudResourceProvider.check(userModel)
    if (result.isEmpty) {
      val cleanModelOption = KloudResourceProvider.cleanUserModel(userModel)
      if (cleanModelOption.isDefined) {
        val cleanModel = cleanModelOption.get
        val uuidModel = modelHandlerService.getLastUUIDModel
        val newGlobalModelOption = KloudResourceProvider.distribute(userModel, uuidModel.getModel)
        if (newGlobalModelOption.isDefined) {
          val newGlobalModel = newGlobalModelOption.get
          var ok = KloudResourceProvider.update(uuidModel, newGlobalModel, modelHandlerService)
          if (ok) {
            ok = KloudResourceProvider.updateUserConfiguration(cleanModel, userModel, modelHandlerService)
          } else {
            logger.debug("Unable to update the system to deploy your software.")
          }
        } else {
          logger.debug("Unable to deploy your nodes on the Kloud.")
        }
      } else {
        logger.debug("Unable to apply KevScript to add a group that manage the overall software.")
      }
    } else {
      logger.debug("Unable to validate the model:\n{}", result.get)
    }
  }

  def processDeployment(newModel : ContainerRoot, userModel : ContainerRoot, modelHandlerService: KevoreeModelHandlerService,
    kevScripEngineFactory: KevScriptEngineFactory, groupName: String, nodeName: String) {
    // make
  }

  /**
   * Send the user model into the user nodes using the default groups that are set on the cleanModel
   */
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
              .getIntPropertyForGroup(cleanModel, group.getName, "port", subNode.getName)

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

}