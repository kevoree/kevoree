package org.kevoree.library.monitored

import org.kevoree.api.PrimitiveCommand
import org.kevoree.kompare.JavaSePrimitive
import org.kevoree.framework.event.MonitorEventHandler
import org.kevoree.Instance

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/05/12
 * Time: 20:40
 */

class CommandMapper(nodeType: MonitoredJavaSENode, eventH: MonitorEventHandler) {
  def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive): PrimitiveCommand = {
    p.getPrimitiveType.getName match {
      case JavaSePrimitive.AddInstance => MonitoredAddInstance(eventH, p.getRef.asInstanceOf[Instance], nodeType.getNodeName, nodeType.getModelService, nodeType.getKevScriptEngineFactory, nodeType.getBootStrapperService)
      case _ => nodeType.getSuperPrimitive(p)
    }
  }
}
