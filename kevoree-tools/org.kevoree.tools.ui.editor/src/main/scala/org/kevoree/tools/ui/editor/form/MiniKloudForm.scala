package org.kevoree.tools.ui.editor.form

import com.explodingpixels.macwidgets.HudWindow
import javax.swing._
import java.awt.{FlowLayout, BorderLayout}
import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.framework.KevoreeXmiHelper
import com.explodingpixels.macwidgets.plaf.{HudCheckBoxUI, HudButtonUI, HudLabelUI}
import java.io._
import org.kevoree.tools.aether.framework.AetherUtil
import org.kevoree.{KevoreeFactory, ContainerRoot}
import java.util.jar.{JarEntry, JarFile}
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.ui.editor.KevoreeEditor
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.cloner.ModelCloner
import org.slf4j.LoggerFactory
import java.net.{URLConnection, URL}

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
  private var address: String = null


  val newPopup = new HudWindow("Submit a new model on a minicloud")
  newPopup.getJDialog.setSize(400, 75)
  newPopup.getJDialog.setLocationRelativeTo(editor.getPanel)
  val layoutPopup = new JPanel()
  layoutPopup.setOpaque(false)
  layoutPopup.setLayout(new BorderLayout())

  val layoutPopupTop = new JPanel()
  layoutPopupTop.setOpaque(false)


  // define the buttons available to submit something on Kloud
  val btSubmit = new JButton("Submit model")
  btSubmit.setUI(new HudButtonUI)
  btSubmit.addActionListener(new ActionListener {
    def actionPerformed (p1: ActionEvent) {
      new Thread() {
        override def run () {
          logger.debug("sending a model on a local minicloud")
          // shutdown the current minicloud if the updateCheckBox is not selected
          if (minicloud != null) {
            logger.debug("stopping minicloud")
            shutdownMiniCloud()
          }
          // create a new one
          if (minicloud == null) {
            logger.debug("starting minicloud")
            val java = getJava

            // build default model of the minicloud
            if (bootstrapModel == null) {
              platformJAR = AetherUtil.resolveMavenArtifact("org.kevoree.platform.osgi.standalone", "org.kevoree.platform", KevoreeFactory.getVersion,List("http://maven.kevoree.org/release", "http://maven.kevoree.org/snapshots"))
              if (platformJAR != null) {
                buildBootstrapModel()
              }

            }
            logger.debug("trying to start the minicloud")
            minicloud = Runtime.getRuntime.exec(Array[String](java, "-Dnode.bootstrap=" + bootstrapModel, "-Dnode.name=editor_node", "-Dnode.log.level=DEBUG", "-jar", platformJAR.getAbsolutePath))

          }

          if (minicloud != null) {
            // define a new model based on the current one of the editor
            // adding the minicloud config on this model
            val cloner = new ModelCloner
            val modelOption = buildMiniCloudModel(cloner.clone(editor.getPanel.getKernel.getModelHandler.getActualModel))
            if (modelOption.isDefined) {
              // send the current model to this new minicloud
              sendModel(address, modelOption.get)
            }
          } else {
            logger.debug("Unable to locate a minicloud")
          }
        }
      }.start()

    }
  })

  val btRelease = new JButton("Release model")
  btRelease.setUI(new HudButtonUI)
  btRelease.addActionListener(new ActionListener {
    def actionPerformed (p1: ActionEvent) {
      // shutdown the current minicloud
      if (minicloud != null) {
        new Thread() {
          override def run () {
            logger.debug("trying to shutdown the minicloud")
            shutdownMiniCloud()
          }
        }.start()
      }
    }
  })

  val submissionLayout = new JPanel(new FlowLayout(FlowLayout.CENTER))
  submissionLayout.add(btSubmit)
  submissionLayout.add(btRelease)
  submissionLayout.setOpaque(false)


  layoutPopup.add(layoutPopupTop, BorderLayout.NORTH)
  layoutPopup.add(submissionLayout, BorderLayout.SOUTH)
  newPopup.getContentPane.add(layoutPopup)

  def display () {
    newPopup.getJDialog.setVisible(true)
  }

  def hide () {
    newPopup.getJDialog.setVisible(false)
  }

  private def shutdownMiniCloud () {
    val watchdog = new KillWatchDog(minicloud, 20000)
    logger.debug("send shutdown command")
    minicloud.getOutputStream.write("shutdown\n".getBytes)
    minicloud.getOutputStream.flush()

    watchdog.start()
    minicloud.waitFor()
    watchdog.stop()
    logger.debug("minicloud shutted down")
    minicloud = null
  }

  private def buildBootstrapModel () {

    val skyModelFile = AetherUtil
      .resolveMavenArtifact("org.kevoree.library.model.sky", "org.kevoree.library.model", KevoreeFactory.getVersion, List("http://maven.kevoree.org/release", "http://maven.kevoree.org/snapshots"))
    if (skyModelFile != null) {
      val jar = new JarFile(skyModelFile)
      val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
      val skyModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry))

      val scriptBuilder = new StringBuilder
      scriptBuilder append "tblock {\n"
      scriptBuilder append "addNode editor_node : MiniCloudNode {role=\"host\", port=\"6001\"}\n"
      scriptBuilder append "addGroup editor_group : RestGroup\n"
      scriptBuilder append "addToGroup editor_group editor_node\n"
      scriptBuilder append "updateDictionary editor_group {port=\"6002\"}@editor_node\n"
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
            address = "http://localhost:6002/model/current"
          }
        }
        case _ => logger.debug("Unable to apply the script:\n{}", scriptBuilder.toString())
      }
    } else {
      logger.debug("Unable to get the sky modeljar")
    }
  }

  private def buildMiniCloudModel (model: ContainerRoot): Option[ContainerRoot] = {
    val scriptBuilder = new StringBuilder
    scriptBuilder append "tblock {\n"
    scriptBuilder append "merge \"mvn:org.kevoree.library.sky/org.kevoree.library.sky.minicloud/" + KevoreeFactory.getVersion + "\"\n"
    scriptBuilder append "addNode editor_node : MiniCloudNode {role=\"host\", port=\"6001\"}\n"
    scriptBuilder append "addGroup editor_group : RestGroup\n"
    scriptBuilder append "addToGroup editor_group editor_node\n"
    scriptBuilder append "updateDictionary editor_group {port=\"6002\"}@editor_node\n"
    model.getNodes.foreach {
      node =>
        scriptBuilder append "addChild " + node.getName + "@editor_node"
    }
    scriptBuilder append "}"
    val parser = new KevsParser
    parser.parseScript(scriptBuilder.toString()) match {
      case Some(script) => {
        val result = script.interpret(KevsInterpreterContext(model))
        if (result) {
          Some(model)
        } else {
          logger.debug("Unable to apply the script:\n{}", scriptBuilder.toString())
          None
        }
      }
      case _ => logger.debug("Unable to apply the script:\n{}", scriptBuilder.toString()); None
    }
  }

  private def sendModel (address: String, model: ContainerRoot): Boolean = {
    try {
      logger.debug("url=>{}", address)
      val url = new URL(address)
      val connection = connectURL(url)
      connection.setConnectTimeout(3000)
      connection.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(getOutputStream(connection))
      wr.write(KevoreeXmiHelper.saveToString(model, false))
      wr.flush()
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream))
      var line: String = rd.readLine
      val response = new StringBuilder
      while (line != null) {
        response append line + "\n"
        line = rd.readLine
      }
      wr.close()
      rd.close()
      // look the answer to know if the model has been correctly sent
      if (response.toString().contains("<ack nodeName=")) {
        logger.debug("model sent")
        true
      } else {
        logger.debug("model not sent")
        false
      }
    } catch {
      case _@e => logger.error("Unable to send a model", e); false
    }
  }
  
  private def connectURL(url : URL) : URLConnection = {
    try {
      url.openConnection()
    } catch {
      case _@e => connectURL(url)
    }
  }
  
  private def getOutputStream(connection : URLConnection) : OutputStream = {
    try {
      connection.getOutputStream
    } catch {
      case _@e => Thread.sleep(2000);getOutputStream(connection)
    }
  }

  private def getJava: String = {
    val java_home: String = System.getProperty("java.home")
    java_home + File.separator + "bin" + File.separator + "java"
  }
}
