package org.kevoree.library.socketChannel

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/02/12
 * Time: 12:53
 */

import java.lang.Class
import org.kevoree.framework.AbstractChannelFragment
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

class ChannelClassResolver (nioChannel: AbstractChannelFragment) {
  def resolve (className: String): Class[_] = {
    val model = nioChannel.getModelService.getLastModel
    val currentNode = model.getNodes.find(n => n.getName == nioChannel.getNodeName).get
    var resolvedClass: Class[_] = null
    nioChannel.getBindedPorts.foreach {
      bport =>
        if (resolvedClass == null) {
          currentNode.getComponents.find(c => c.getName == bport.getComponentName) match {
            case Some(component) => {
              val du = component.getTypeDefinition.foundRelevantDeployUnit(currentNode)
              if (du != null) {
                val kcl = nioChannel.getBootStrapperService.getKevoreeClassLoaderHandler.getKevoreeClassLoader(du)
                try {
                  resolvedClass = kcl.loadClass(className)
                  return resolvedClass
                } catch {
                  case _ => //IGNORE
                }
              }
            }
            case _ =>
          }
        }
    }
    if (resolvedClass == null) {
      resolvedClass = nioChannel.getClass.getClassLoader.loadClass(className)
    }
    resolvedClass
  }
}

