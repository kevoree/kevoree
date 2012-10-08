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
import org.kevoree.tools.aether.framework.AetherUtil
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor.command.LoadModelCommand
import org.kevoree.tools.ui.editor.{UIEventHandler, PositionedEMFHelper, KevoreeEditor}
import org.kevoree.tools.marShell.KevScriptOfflineEngine
import java.net._
import org.kevoree.framework.{KevoreePropertyHelper, KevoreeXmiHelper}
import org.kevoree.{TypeDefinition, ContainerNode, ContainerRoot, KevoreeFactory}
import org.kevoree.tools.modelsync.FakeBootstraperService
import javax.swing.{JOptionPane, ImageIcon, AbstractButton}
import java.awt.Desktop
import scala.Some
import org.kevoree.core.basechecker.RootChecker

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

  val url: URL = this.getClass.getClassLoader.getResource("ajax-loader.gif")
  val iconLoading: ImageIcon = new ImageIcon(url)

  def startMiniCloud(): Boolean = {

    var checker = new RootChecker
    if (!checker.check(editor.getPanel.getKernel.getModelHandler.getActualModel).isEmpty) {
      logger.error("Check found errors, please correct your model")
      JOptionPane.showMessageDialog(null,"Check found errors, please correct your model")
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
            platformJAR = AetherUtil.resolveKevoreeArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", KevoreeFactory.getVersion)
            UIEventHandler.info("org.kevoree.platform.standalone.gui resolved")
            if (platformJAR != null) {
              PositionedEMFHelper.updateModelUIMetaData(editor.getPanel.getKernel)
              val skyModel = buildBootstrapModel
              if (skyModel != null) {
                val file = File.createTempFile("editorBootstrapModel", "kev")
                file.deleteOnExit()

                KevoreeXmiHelper.save(file.getAbsolutePath, skyModel)
                logger.debug("trying to start the minicloud")
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

                for (i <- 0 until 10) {
                  try {
                    new URL("http://localhost:7000/").openConnection().connect()
                  } catch {
                    case _ => Thread.sleep(1000)
                  }
                }



                if (Desktop.isDesktopSupported) {
                  Desktop.getDesktop.browse(new URI("http://localhost:7000/"))
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
    val nodes = editor.getPanel.getKernel.getModelHandler.getActualModel.getNodes.filter(n => n.getTypeDefinition.getName == "JavaSENode" || isASubType(n.getTypeDefinition, "JavaSENode"))
    editor.getPanel.getKernel.getModelHandler.getActualModel.getNodes
      .find(n => n.getTypeDefinition.getName == "MiniCloudNode" && n.getHosts.size == nodes.size - 1 && !n.getHosts.contains(n)) match {
      case Some(minicloudNode) => {
        logger.debug("start a minicloud with your own minicloud node")
        minicloudName = minicloudNode.getName

        editor.getPanel.getKernel.getModelHandler.getActualModel
      }
      case None => {
        logger.debug("start a minicloud with an editor node")
        minicloudName = "editor_node"
        PositionedEMFHelper.updateModelUIMetaData(editor.getPanel.getKernel)
        val skyModel = editor.getPanel.getKernel.getModelHandler.getActualModel


        val kevEngine = new KevScriptOfflineEngine(skyModel, new FakeBootstraperService().getBootstrap)
        kevEngine.addVariable("kevoree.version", KevoreeFactory.getVersion)
        kevEngine.addVariable("minicloudNodeName", minicloudName)
        kevEngine.append("merge 'mvn:org.kevoree.corelibrary.model/org.kevoree.library.model.sky/{kevoree.version}'")
        kevEngine.append("addNode {minicloudNodeName}: MiniCloudNode {}")

        // add all JavaSE (or inherited) user nodes as child of the minicloud node
        nodes.foreach {
          node =>
            kevEngine.addVariable("nodeName", node.getName)
            kevEngine.append("addChild {nodeName}@editor_node")
        }

        var groupName = "editor_group"
        var blackListedPorts = Array[Int](6001, 6002)
        // looking for a group that manage all the user nodes that can be hosted on a minicloud node
        editor.getPanel.getKernel.getModelHandler.getActualModel.getGroups.find(g => g.getSubNodes.size == nodes.size && g.getSubNodes.forall(n => nodes.contains(n))) match {
          case Some(group) => groupName = group.getName
          case None =>
            // add a new group
            kevEngine.append("addGroup editor_group : NanoRestGroup")
            // add all node on the same group
            nodes.foreach {
              node =>
                kevEngine.addVariable("nodeName", node.getName)
                kevEngine.append("addToGroup editor_group {nodeName}")
                // add specific port for each node
                val port = selectPort(blackListedPorts)
                blackListedPorts = blackListedPorts ++ Array[Int](port)
                kevEngine.addVariable("portValue", port.toString)
                kevEngine.append("updateDictionary {groupName} {port='{portValue}'}@{nodeName}")
            }
        }
        kevEngine.addVariable("groupName", groupName)
        kevEngine.append("addToGroup {groupName} {minicloudNodeName}")
        kevEngine.append("updateDictionary {groupName} {port='6002'}@{minicloudNodeName}")

        try {
          kevEngine.interpret()
        } catch {
          case _@e => {
            logger.error("Unable to compute model to deploy on minicloud.", e)
            null
          }
        }
      }
    }
  }

  def isASubType(nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  private def selectPort(blackListedPorts: Array[Int]): Int = {
    // get all port defined on the model
    // concatenate the ports of the model with the blacklisted ones
    val ports = blackListedPorts ++ getAllUsedPorts
    // start to find a port that are not into the previous list and tat are not current in use.
    selectPortNumber("127.0.0.1", ports)
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
            logger.debug("Looking for property 'port' on group {} with node {}", group.getName, node.getName)
            val portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, group.getName, "port", isFragment = true, nodeNameForFragment = node.getName)
            if (portOption.isDefined) {
              ports = ports ++ Array[Int](portOption.get)
            }
        }

    }
    // looking for port on channel
    model.getMBindings.foreach {
      binding =>
        logger.debug("Looking for property 'port' on channel {} with node {}", binding.getHub.getName, binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
        val portOption = KevoreePropertyHelper.getIntPropertyForChannel(model, binding.getHub.getName, "port", isFragment = true, nodeNameForFragment = binding.getPort.eContainer.eContainer
          .asInstanceOf[ContainerNode].getName)
        if (portOption.isDefined) {
          ports = ports ++ Array[Int](portOption.get)
        }

    }
    // looking for port on component
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            logger.debug("Looking for property 'port' on component {}", component.getName)
            val portOption = KevoreePropertyHelper.getIntPropertyForComponent(model, component.getName, "port")
            if (portOption.isDefined) {
              ports = ports ++ Array[Int](portOption.get)
            }
        }

    }

    ports
  }

  private def selectPortNumber(address: String, ports: Array[Int]): Int = {
    var i = 6003
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
