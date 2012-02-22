package org.kevoree.tools.ui.editor.kloud

import org.kevoree.framework.KevoreeXmiHelper
import java.io._
import org.kevoree.tools.aether.framework.AetherUtil
import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor.KevoreeEditor
import org.kevoree.tools.ui.editor.command.LoadModelCommand

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/02/12
 * Time: 19:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class MiniKloudForm (editor: KevoreeEditor) {
  var logger = LoggerFactory.getLogger(this.getClass)
  private var minicloud: Process = null
  private var bootstrapModel: String = null
  private var platformJAR: File = null
  private var thread: Thread = null
  private var minicloudName: String = null

  def startMiniCloud (): Boolean = {
    if (thread == null) {
      thread = new Thread() {
        override def run () {
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
            if (bootstrapModel == null) {
              platformJAR = AetherUtil.resolveMavenArtifact("org.kevoree.platform.osgi.standalone.gui", "org.kevoree.platform", KevoreeFactory.getVersion, List("http://maven.kevoree.org/release", "http://maven.kevoree.org/snapshots"))
              if (platformJAR != null) {
                buildBootstrapModel()
                logger.debug("trying to start the minicloud")
                minicloud = Runtime.getRuntime.exec(Array[String](java, "-Dnode.gui.config=false", "-Dnode.bootstrap=" + bootstrapModel, "-Dnode.name=" + minicloudName, "-Dnode.log.level=DEBUG", "-jar", platformJAR.getAbsolutePath))


                //LOAD MODEL
                val loadCmd = new LoadModelCommand();
                loadCmd.setKernel(editor.getPanel.getKernel);
                loadCmd.execute(bootstrapModel);
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

  def shutdownMiniCloud (): Boolean = {
    if (thread == null) {
      thread = new Thread() {
        override def run () {
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
          bootstrapModel = null
          thread = null
        }
      }
      thread.start()
      true
    } else {
      false
    }
  }

  private def buildBootstrapModel () {
    editor.getPanel.getKernel.getModelHandler.getActualModel.getNodes.find(n => n.getTypeDefinition.getName == "MiniCloudNode") match {
      case Some(minicloudNode) => {
        logger.debug("start a minicloud with your own minicloud node")
        minicloudName = minicloudNode.getName
        val file = File.createTempFile("editorBootstrapModel", "kev")
        file.deleteOnExit()
        KevoreeXmiHelper.save(file.getAbsolutePath, editor.getPanel.getKernel.getModelHandler.getActualModel);
        bootstrapModel = file.getAbsolutePath
      }
      case None => {
        logger.debug("start a minicloud with an editor node")
        minicloudName = "editor_node"
        val skyModel = editor.getPanel.getKernel.getModelHandler.getActualModel

        val scriptBuilder = new StringBuilder
        scriptBuilder append "tblock {\n"
        scriptBuilder append "merge \"mvn:org.kevoree.library.model/org.kevoree.library.model.sky/" + KevoreeFactory.getVersion + "\"\n"
        scriptBuilder append "merge \"mvn:org.kevoree.library.javase/org.kevoree.library.javase.rest/" + KevoreeFactory.getVersion + "\"\n"
        scriptBuilder append "addNode " + minicloudName + ": MiniCloudNode {role=\"host\", port=\"6001\"}\n"
        scriptBuilder append "addGroup editor_group : RestGroup\n"
        scriptBuilder append "addToGroup editor_group " + minicloudName + "\n"
        scriptBuilder append "updateDictionary editor_group {port=\"6002\"}@" + minicloudName + "\n"
        editor.getPanel.getKernel.getModelHandler.getActualModel.getNodes.foreach {
          node =>
            scriptBuilder append "addChild " + node.getName + "@editor_node"
        }
        scriptBuilder append "}"


        val parser = new KevsParser
        parser.parseScript(scriptBuilder.toString()) match {
          case Some(script) => {
            val result = script.interpret(KevsInterpreterContext(skyModel))
            if (result) {
              val file = File.createTempFile("editorBootstrapModel", "kev")
              file.deleteOnExit()
              KevoreeXmiHelper.save(file.getAbsolutePath, skyModel);
              bootstrapModel = file.getAbsolutePath
            }
          }
          case _ => logger.debug("Unable to apply the script:\n{}", scriptBuilder.toString())
        }
      }
    }
  }

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }
}
