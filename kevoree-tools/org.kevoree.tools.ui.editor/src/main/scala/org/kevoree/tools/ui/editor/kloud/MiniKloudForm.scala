/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.kloud

import java.io._
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor.command.{AetherResolver, LoadModelCommand}
import org.kevoree.tools.ui.editor.{UIEventHandler, PositionedEMFHelper}
import org.kevoree.tools.marShell.KevScriptOfflineEngine
import java.net._
import org.kevoree.framework.{KevoreePropertyHelper, KevoreeXmiHelper}
import org.kevoree._
import org.kevoree.tools.modelsync.FakeBootstraperService
import javax.swing.{JOptionPane, ImageIcon, AbstractButton}
import java.awt.Desktop
import org.kevoree.core.basechecker.RootChecker
import scala.Some
import scala.collection.JavaConversions._
import tools.ui.editor.{KevoreeEditor, ModelHelper}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/02/12
 * Time: 19:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class MiniKloudForm(editor: KevoreeEditor, button: AbstractButton) {
  var logger = LoggerFactory.getLogger(this.getClass)
  private var minicloud: Process = null
  private var platformJAR: File = null
  private var thread: Thread = null
  private var minicloudName: String = null
  private val firstPortToUse = 8000

  val url: URL = this.getClass.getClassLoader.getResource("ajax-loader.gif")
  val iconLoading: ImageIcon = new ImageIcon(url)

  def startMiniCloud(): Boolean = {

    val checker = new RootChecker
    if (!checker.check(editor.getPanel.getKernel.getModelHandler.getActualModel).isEmpty) {
      logger.error("Check found errors, please correct your model")
      JOptionPane.showMessageDialog(null, "Check found errors, please correct your model")
      return false
    }

    if (thread == null) {
      thread = new Thread() {
        override def run() {

          val previousIcon = button.getIcon
          button.setIcon(iconLoading)
          button.setDisabledIcon(iconLoading)
          button.setEnabled(true)

          logger.debug("sending a model on a local minicloud")
          var exitValue = -1
          try {
            exitValue = minicloud.exitValue()
          } catch {
            case _@e =>
          }
          // create a new one
          if (minicloud == null || exitValue != -1) {
            logger.debug("starting minicloud")
            val java = getJava

            // build default model of the minicloud
            UIEventHandler.info("Download org.kevoree.platform.standalone.gui")
            platformJAR = AetherResolver.resolveKev("org.kevoree.platform.standalone.gui", "org.kevoree.platform", ModelHelper.kevoreeFactory.getVersion)
            UIEventHandler.info("org.kevoree.platform.standalone.gui resolved")
            if (platformJAR != null) {
              PositionedEMFHelper.updateModelUIMetaData(editor.getPanel.getKernel)
              val skyModel = buildBootstrapModel
              if (skyModel != null) {
                val file = File.createTempFile("editorBootstrapModel", "kev")
                file.deleteOnExit()

                KevoreeXmiHelper.instance$.save(file.getAbsolutePath, skyModel)
                logger.debug("trying to start the minicloud: {}", minicloudName)
                minicloud = Runtime.getRuntime
                  .exec(Array[String](java, "-Dnode.gui.config=false", "-Dnode.bootstrap=" + file.getAbsolutePath, "-Dnode.name=" + minicloudName, "-Dkevoree.log.level=INFO", "-jar",
                  platformJAR.getAbsolutePath))

                //LOAD MODEL
                val loadCmd = new LoadModelCommand()
                loadCmd.setKernel(editor.getPanel.getKernel)
                loadCmd.execute(skyModel)

                monitorMiniCloud()
                button.setEnabled(true)
                button.setIcon(previousIcon)
                button.setDisabledIcon(previousIcon)
                UIEventHandler.info("MiniKloud node Started !")

                skyModel.findByPath("nodes[" + minicloudName + "]/components[" + "webServer" + "]", classOf[ComponentInstance]) match {
                  case null =>
                  case component: ComponentInstance => {
                    val portOption = KevoreePropertyHelper.instance$.getProperty(component, "port", false, "")
                    if (portOption != null) {
                      for (i <- 0 until 10) {
                        try {
                          logger.debug("checking: http://localhost:{}/", portOption)
                          new URL("http://localhost:" + portOption + "/").openConnection().connect()
                        } catch {
                          case _: Throwable => Thread.sleep(5000)
                        }
                      }

                      if (Desktop.isDesktopSupported) {
                        logger.info("starting miniKloud web page: http://localhost:{}/", portOption)
                        Desktop.getDesktop.browse(new URI("http://localhost:" + portOption + "/"))
                      } else {
                        logger.warn("Our desktop is not support so we are not able to open the web page: http://localhost:{}/", portOption)
                      }
                    }
                  }
                }
              }
            }

          }
          thread = null
        }
      }
      thread.start()
      true
    } else {
      false
    }

  }

  def shutdownMiniCloud(): Boolean = {
    if (thread == null) {
      thread = new Thread() {
        override def run() {
          var exitValue = -1
          try {
            exitValue = minicloud.exitValue()
          } catch {
            case _@e =>
          }
          // create a new one
          if (minicloud != null && exitValue == -1) {
            minicloud.destroy()
          }
          logger.debug("minicloud shutted down")
          minicloud = null
          minicloudName = null
          thread = null
          button.setEnabled(false)
          UIEventHandler.info("MiniKloud killed !")
        }
      }
      thread.start()
      true
    } else {
      false
    }
  }

  private def monitorMiniCloud() {
    new Thread() {
      override def run() {
        minicloud.waitFor()
        logger.debug("minicloud shutted down")
        minicloud = null
        minicloudName = null
        thread = null
        button.setEnabled(false)
      }
    }.start()
  }

  private def buildBootstrapModel: ContainerRoot = {
    PositionedEMFHelper.updateModelUIMetaData(editor.getPanel.getKernel)
    val skyModel = editor.getPanel.getKernel.getModelHandler.getActualModel

    val kevEngine = new KevScriptOfflineEngine(skyModel, new FakeBootstraperService().getBootstrap)

    var blackListedPorts = Array[Int](8000)

    val nodes = editor.getPanel.getKernel.getModelHandler.getActualModel.getNodes.filter(n => n.getTypeDefinition.getName == "PJavaSENode" || isASubType(n.getTypeDefinition, "PJavaSENode"))
    editor.getPanel.getKernel.getModelHandler.getActualModel.getNodes
      .find(n => n.getTypeDefinition.getName == "MiniCloudNode" /* && n.getHosts.size == nodes.size - 1 && n.getHost.isEmpty*/) match {
      case Some(minicloudNode) => {
        logger.debug("starting a minicloud with your own minicloud node")
        minicloudName = minicloudNode.getName

        editor.getPanel.getKernel.getModelHandler.getActualModel
      }
      case None => {
        logger.debug("starting a minicloud with an editor node")
        minicloudName = "editor_node"
        kevEngine.addVariable("minicloudNodeName", minicloudName)
        kevEngine.addVariable("kevoree.version", ModelHelper.kevoreeFactory.getVersion)

        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.sky/org.kevoree.library.sky.minicloud/{kevoree.version}'")
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.webserver.api/{kevoree.version}'")
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.webserver.tjws/{kevoree.version}'")
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.sky/org.kevoree.library.sky.provider/{kevoree.version}'")
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.sky/org.kevoree.library.sky.provider.web/{kevoree.version}'")
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.basicGossiper/{kevoree.version}'")
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.defaultChannels/{kevoree.version}'")

        kevEngine.append("addNode {minicloudNodeName}: MiniCloudNode {logLevel = 'INFO'}")

        val port = selectPort(firstPortToUse, blackListedPorts)
        blackListedPorts = blackListedPorts ++ Array[Int](port)
        kevEngine.addVariable("portValue", port.toString)

        // add LogBack component to be able to manage log levels
        kevEngine.append("addComponent LogBackConfigurator@editor_node : LogBackConfigurator")

        // add the web page to manage the miniKloud
        kevEngine.append("addComponent webServer@{minicloudNodeName} : KTinyWebServer {port = '{portValue}', timeout = '10000'}")
        kevEngine.append("addComponent iaasPage@{minicloudNodeName} : IaaSKloudResourceManagerPage { urlpattern='/'}")
        kevEngine.append("addComponent iaasManager@{minicloudNodeName} : IaaSKloudManager")

        kevEngine.append("addChannel requestChannel : defMSG")
        kevEngine.append("addChannel responseChannel : defMSG")
        kevEngine.append("addChannel iaasDelegateChannel : defSERVICE")

        kevEngine.append("bind webServer.handler@{minicloudNodeName} => requestChannel")
        kevEngine.append("bind iaasPage.request@{minicloudNodeName} => requestChannel")

        kevEngine.append("bind webServer.response@{minicloudNodeName} => responseChannel")
        kevEngine.append("bind iaasPage.content@{minicloudNodeName} => responseChannel")

        kevEngine.append("bind iaasManager.submit@{minicloudNodeName} => iaasDelegateChannel")
        kevEngine.append("bind iaasPage.delegate@{minicloudNodeName} => iaasDelegateChannel")
      }
    }

    kevEngine.addVariable("minicloudNodeName", minicloudName)

    var groupName = "editor_group"

    // looking for a group that manage all the user nodes that can be hosted on a minicloud node
    editor.getPanel.getKernel.getModelHandler.getActualModel.getGroups.find(g =>
      (g.getSubNodes.size == nodes.size || (g.getSubNodes.size() == nodes.size + 1 && g.findSubNodesByID(minicloudName) != null))
        && g.getSubNodes.forall(n => n.getName == minicloudName || nodes.contains(n))) match {
      case Some(group) => groupName = group.getName; kevEngine.addVariable("groupName", groupName)
      case None => {
        groupName = "editor_group"
        val groupAlreadyExist = editor.getPanel.getKernel.getModelHandler.getActualModel.findGroupsByID(groupName) match {
          case group: Group => true
          case null => {
            // add a new group
            kevEngine.append("addGroup editor_group : BasicGossiperGroup")
            false
          }
        }
        kevEngine.addVariable("groupName", groupName)
        // add all node on the same group
        nodes.foreach {
          node =>
            if (!groupAlreadyExist || editor.getPanel.getKernel.getModelHandler.getActualModel.findGroupsByID(groupName).findSubNodesByID(node.getName) == null) {
              kevEngine.addVariable("nodeName", node.getName)
              kevEngine.append("addToGroup {groupName} {nodeName}")
              // add specific port for each node
              val port = selectPort(firstPortToUse, blackListedPorts)
              blackListedPorts = blackListedPorts ++ Array[Int](port)
              kevEngine.addVariable("portValue", port.toString)
              kevEngine.append("updateDictionary {groupName} {port='{portValue}'}@{nodeName}")
            }
        }
      }
    }

    if (editor.getPanel.getKernel.getModelHandler.getActualModel.findGroupsByID(groupName) == null || editor.getPanel.getKernel.getModelHandler.getActualModel.findGroupsByID(groupName).findSubNodesByID(minicloudName) == null) {
      kevEngine.addVariable("portValue", selectPort(firstPortToUse, blackListedPorts) + "")
      kevEngine.append("addToGroup {groupName} {minicloudNodeName}")
      kevEngine.append("updateDictionary {groupName} {port='{portValue}'}@{minicloudNodeName}")

    }


    val minicloudNode = editor.getPanel.getKernel.getModelHandler.getActualModel.findNodesByID(minicloudName)
    // add all JavaSE (or inherited) user nodes as child of the minicloud node
    nodes.foreach {
      node =>
        if (minicloudNode == null || minicloudNode.findHostsByID(node.getName) == null) {
          kevEngine.addVariable("nodeName", node.getName)
          kevEngine.append("addChild {nodeName}@editor_node")
        }
    }
    try {
      kevEngine.interpret()
    } catch {
      case _@e => {
        logger.error("Unable to compute model to deploy on minicloud.", e)
        null
      }
    }
  }

  def isASubType(nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  private def selectPort(firstPort: Int, blackListedPorts: Array[Int]): Int = {
    // get all port defined on the model
    // concatenate the ports of the model with the blacklisted ones
    val ports = blackListedPorts ++ getAllUsedPorts
    // start to find a port that are not into the previous list and tat are not current in use.
    selectPortNumber(firstPort, "127.0.0.1", ports)
  }


  private def getAllUsedPorts: Array[Int] = {
    // get the model
    val model = editor.getPanel.getKernel.getModelHandler.getActualModel
    var ports = Array[Int]()
    // looking for port on groups
    model.getGroups.foreach {
      group =>
        model.getNodes.foreach {
          node =>
            logger.debug("Looking for property 'port' on group {} with node {}", Array[String](group.getName, node.getName))

            val portOption = KevoreePropertyHelper.instance$.getProperty(group, "port", true, node.getName)
            if (portOption != null) {
              ports = ports ++ Array[Int](Integer.parseInt(portOption))
            }
        }

    }
    // looking for port on channel
    model.getMBindings.foreach {
      binding =>
        logger.debug("Looking for property 'port' on channel {} with node {}", Array[String](binding.getHub.getName, binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName))
        val portOption = KevoreePropertyHelper.instance$.getProperty(binding.getHub, "port", true, binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
        if (portOption != null) {
          ports = ports ++ Array[Int](Integer.parseInt(portOption))
        }

    }
    // looking for port on component
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            logger.debug("Looking for property 'port' on component {}", component.getName)
            val portOption = KevoreePropertyHelper.instance$.getProperty(component, "port", false, "")
            if (portOption != null) {
              ports = ports ++ Array[Int](Integer.parseInt(portOption))
            }
        }

    }

    ports
  }

  private def selectPortNumber(firstPort: Int, address: String, ports: Array[Int]): Int = {
    var i = firstPort
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

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }
}
