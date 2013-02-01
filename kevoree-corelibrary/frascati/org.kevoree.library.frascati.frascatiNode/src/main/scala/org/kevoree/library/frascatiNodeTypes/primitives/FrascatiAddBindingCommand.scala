package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}
import org.kevoree.{MBinding, ComponentInstance}
import org.kevoree.framework.osgi.KevoreeChannelFragmentActivator
import org.objectweb.fractal.api.Component
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.library.frascatiNodeTypes.KevoreeReflexiveProvidedPort
import org.kevoree.framework.message.PortBindMessage
import org.kevoree.framework.KevoreeChannelFragment
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:16
 */

case class FrascatiAddBindingCommand(binding: MBinding, nodeName: String) extends PrimitiveCommand {
  var logger = LoggerFactory.getLogger(this.getClass);

  def execute(): Boolean = {
    val KevoreeChannelFound = KevoreeDeployManager.bundleMapping.find(map => map.objClassName == binding.getHub.getClass.getName && map.name == binding.getHub.getName) match {
      case None => logger.error("Channel Fragment Mapping not found"); None
      case Some(mapfound) => {
        mapfound match {
          case kb: KevoreeMapping => {
            Some(kb.ref.asInstanceOf[KevoreeChannelFragmentActivator].channelActor)
          }
          case _ => logger.error("Channel Fragment Service not found"); None
        }
      }
    }

    KevoreeDeployManager.bundleMapping.find(map => map.objClassName == binding.getPort.eContainer.getClass.getName && map.name == binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName) match {
      case None => false
      case Some(mapfound) => {
        val c: Component = mapfound.ref.asInstanceOf[Component]
        val componentName = binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName
        val portName = binding.getPort.getPortTypeRef.getName

        //IF PROVIDED PORT
        if (binding.getPort.isProvidedPort) {
          val targetInterface = c.getFcInterface(portName)
          val newWrapperPort = new KevoreeReflexiveProvidedPort(portName, componentName, targetInterface)
          newWrapperPort.start()
          if (newWrapperPort.isInPause) {
            newWrapperPort.resume
          }
          val bindmsg = new PortBindMessage
          bindmsg.setNodeName(nodeName)
          bindmsg.setComponentName(componentName)
          bindmsg.setPortName(portName)
          bindmsg.setProxy(newWrapperPort)
          (KevoreeChannelFound.get.asInstanceOf[KevoreeChannelFragment] !? bindmsg).asInstanceOf[Boolean]
        } else {
          //IF REQUIRED PORT
          if (binding.getPort.isRequiredPort) {
            //TODO
            false
          } else {
            false
          }
        }


      }
    }
  }

  def undo() = {
    //RemoveBindingCommand(c, nodeName).execute()
  }


}
