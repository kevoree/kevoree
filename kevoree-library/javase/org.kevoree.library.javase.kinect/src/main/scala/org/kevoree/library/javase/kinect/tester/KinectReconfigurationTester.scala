package org.kevoree.library.javase.kinect.tester

import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.slf4j.{LoggerFactory, Logger}
import scala.Array
import org.kevoree.{Instance, Channel, ComponentInstance, ContainerRoot}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/01/12
 * Time: 14:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KinectReconfigurationTester {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * unbind all component on kinect motor port
   * bind KinectTester identified by componentName
   */
  def bind (model: ContainerRoot, kevEngine: KevScriptEngineFactory, componentName: String,
    nodeName: String): Array[String] = {

    val kinectNameOption = foundKinectName(model, nodeName)
    if (kinectNameOption.isDefined) {
      val scriptBuilder = new StringBuilder()
      //      scriptBuilder append "tblock {\n"

      // bind Web Server with this proxy
      scriptBuilder append "addChannel channel_" + componentName + "_proxy" + "1 : defMSG\n"
      scriptBuilder append "addChannel channel_" + componentName + "_proxy" + "2 : defMSG\n"
      scriptBuilder append
        "bind " + componentName + ".motor@" + nodeName + " => channel_" + componentName + "_proxy" + "1\n"
      scriptBuilder append
        "bind " + kinectNameOption.get + ".motor@" + nodeName + " => channel_" + componentName + "_proxy" + "1\n"

      // unbind already bound components on the motor port
      val previouslyBounds1 = findPreviouslyBounds(model: ContainerRoot, kinectNameOption.get, "motor")
      previouslyBounds1.foreach {
        hubName =>
          scriptBuilder append
            "unbind " + kinectNameOption.get + ".motor@" + nodeName + " => " + hubName + "\n"
      }

      scriptBuilder append "addComponent VideoViwerNew@" + nodeName + " : VideoViewer\n"
      scriptBuilder append "addChannel channel_VideoViewer" + " : defMSG\n"
      scriptBuilder append "bind VideoViwerNew.image@" + nodeName + " => channel_VideoViewer\n"
      scriptBuilder append "bind " + kinectNameOption.get + ".image@" + nodeName + " => channel_VideoViewer\n"

      //      scriptBuilder append "}"

      logger.debug("Try to apply the script below\n{}", scriptBuilder.toString())

      kevEngine.createKevScriptEngine().append(scriptBuilder.toString()).interpretDeploy()

      previouslyBounds1
    } else {
      Array[String]()
    }
  }

  def unbind (model: ContainerRoot, kevEngine: KevScriptEngineFactory, componentName: String, nodeName: String,
    previouslyBounds: Array[String]) {
    val kinectNameOption = foundKinectName(model, nodeName)
    if (kinectNameOption.isDefined) {
      val scriptBuilder = new StringBuilder()
      //      scriptBuilder append "tblock {\n"

      // bind Web Server with this proxy
      scriptBuilder append
        "unbind " + componentName + ".motor@" + nodeName + " => channel_" + componentName + "_proxy" + "1\n"
      scriptBuilder append
        "unbind " + kinectNameOption.get + ".motor@" + nodeName + " => channel_" + componentName + "_proxy" + "1\n"

      scriptBuilder append "removeChannel channel_" + componentName + "_proxy" + "1\n"
      scriptBuilder append "removeChannel channel_" + componentName + "_proxy" + "2\n"

      // rebind the previously bounds component
      previouslyBounds.foreach {
        hubName =>
          scriptBuilder append
            "bind " + kinectNameOption.get + ".motor@" + nodeName + " => " + hubName + "\n"
      }

      scriptBuilder append "unbind VideoViwerNew.image@" + nodeName + " => channel_VideoViewer\n"
      scriptBuilder append "unbind " + kinectNameOption.get + ".image@" + nodeName + " => channel_VideoViewer\n"
      scriptBuilder append "removeComponent VideoViwerNew@" + nodeName + "\n"
      scriptBuilder append "removeChannel channel_VideoViewer" + "\n"


      //      scriptBuilder append "}"

      logger.debug("Try to apply the script below\n{}", scriptBuilder.toString())

      kevEngine.createKevScriptEngine().append(scriptBuilder.toString()).interpretDeploy()
    }
  }

  private def foundKinectName (model: ContainerRoot, nodeName: String): Option[String] = {
    model.getNodes.find(n => n.getName == nodeName) match {
      case None => None
      case Some(node) => {
        node.getComponents.find(c => c.getTypeDefinition.getName == "Kinect") match {
          case None => None
          case Some(component) => Some(component.getName)
        }
      }
    }
  }

  private def foundVideoViwerName (model: ContainerRoot, nodeName: String): Option[String] = {
    model.getNodes.find(n => n.getName == nodeName) match {
      case None => None
      case Some(node) => {
        node.getComponents.find(c => c.getTypeDefinition.getName == "VideoViewerImageRecorder") match {
          case None => None
          case Some(component) => Some(component.getName)
        }
      }
    }
  }

  private def findPreviouslyBounds (model: ContainerRoot, componentName: String,
    portName: String): Array[ /*(*/ String /*, String, String)*/ ] = {
    var hubs = Array[String]()
    model.getMBindings.filter(mb => mb.getPort.getPortTypeRef.getName == portName &&
      mb.getPort.eContainer.asInstanceOf[Instance].getName == componentName).foreach {
      mbinding =>
        hubs = hubs ++ Array[String](mbinding.getHub.getName)
    }
    hubs

    /*var previousBounds = Array[(String, String, String)]()
    hubs.foreach {
      hub =>
        model.getMBindings.filter(mb => mb.getHub == hub && mb.getPort.getPortTypeRef.getName != portName).foreach {
          mbinding =>
            previousBounds = previousBounds ++
              Array[(String, String)](mbinding.getPort.eContainer.asInstanceOf[Instance].getName,
                                       mbinding.getPort.getPortTypeRef.getName)
        }
    }
    previousBounds*/
  }

}