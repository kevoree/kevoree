package org.kevoree.library.defaultNodeTypes.jcl.deploy

import command._
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.kompare.JavaSePrimitive
import org.kevoree.framework.PrimitiveCommand
import org.kevoree.{Channel, MBinding, Instance, DeployUnit}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:02
 */

class CommandMapper {

  var modelHandlerService: KevoreeModelHandlerService = null

  def setModelHandlerService(m: KevoreeModelHandlerService) {
    modelHandlerService = m
  }

  var kscripEngineFactory: KevScriptEngineFactory = _

  def setKscripEngineFactory(k: KevScriptEngineFactory) {
    kscripEngineFactory = k
  }

  def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive, nodeName: String): PrimitiveCommand = {
    p.getPrimitiveType.getName match {
      case JavaSePrimitive.AddDeployUnit => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.UpdateDeployUnit => UpdateDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.RemoveDeployUnit => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.AddThirdParty => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.RemoveThirdParty => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit])
      case JavaSePrimitive.AddInstance => AddInstance(p.getRef.asInstanceOf[Instance], nodeName, modelHandlerService, kscripEngineFactory)
      case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionary(p.getRef.asInstanceOf[Instance], nodeName)
      case JavaSePrimitive.RemoveInstance => RemoveInstance(p.getRef.asInstanceOf[Instance], nodeName, modelHandlerService, kscripEngineFactory)
      case JavaSePrimitive.StopInstance  => StartStopInstance(p.getRef.asInstanceOf[Instance], nodeName, false)
      case JavaSePrimitive.StartInstance => StartStopInstance(p.getRef.asInstanceOf[Instance], nodeName, true)
      case JavaSePrimitive.AddBinding => AddBindingCommand(p.getRef.asInstanceOf[MBinding], nodeName)
      case JavaSePrimitive.RemoveBinding => RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], nodeName)
      case JavaSePrimitive.AddFragmentBinding => AddFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, nodeName)
      case JavaSePrimitive.RemoveFragmentBinding => RemoveFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, nodeName)
      case JavaSePrimitive.StartThirdParty => NoopCommand()
      case _ => NoopCommand()
    }
  }

}