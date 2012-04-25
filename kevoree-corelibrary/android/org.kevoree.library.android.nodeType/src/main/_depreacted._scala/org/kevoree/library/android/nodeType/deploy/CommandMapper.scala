package org.kevoree.library.android.nodeType.deploy

import command._
import org.kevoree.kompare.JavaSePrimitive
import org.kevoree.{Channel, MBinding, Instance, DeployUnit}
import org.kevoree.api.PrimitiveCommand
import org.kevoree.framework.AbstractNodeType

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:02
 */

class CommandMapper {
  /*
  var modelHandlerService: KevoreeModelHandlerService = null

  def setModelHandlerService(m: KevoreeModelHandlerService) {
    modelHandlerService = m
  }

  var kscripEngineFactory: KevScriptEngineFactory = _

  def setKscripEngineFactory(k: KevScriptEngineFactory) {
    kscripEngineFactory = k
  }*/

  var nodeType: AbstractNodeType = null

  def setNodeType(n: AbstractNodeType) {
    nodeType = n
  }

  def getNodeType: AbstractNodeType = nodeType


  def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive, nodeName: String): PrimitiveCommand = {
    p.getPrimitiveType.getName match {
      case JavaSePrimitive.AddDeployUnit => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit], nodeType.getBootStrapperService)
      case JavaSePrimitive.UpdateDeployUnit => UpdateDeployUnit(p.getRef.asInstanceOf[DeployUnit], nodeType.getBootStrapperService)
      case JavaSePrimitive.RemoveDeployUnit => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit], nodeType.getBootStrapperService)
      case JavaSePrimitive.AddThirdParty => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit], nodeType.getBootStrapperService)
      case JavaSePrimitive.RemoveThirdParty => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit], nodeType.getBootStrapperService)
      case JavaSePrimitive.AddInstance => AddInstance(p.getRef.asInstanceOf[Instance], nodeName, nodeType.getModelService, nodeType.getKevScriptEngineFactory, nodeType.getBootStrapperService)
      case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionary(p.getRef.asInstanceOf[Instance], nodeName)
      case JavaSePrimitive.RemoveInstance => RemoveInstance(p.getRef.asInstanceOf[Instance], nodeName, nodeType.getModelService, nodeType.getKevScriptEngineFactory, nodeType.getBootStrapperService)
      case JavaSePrimitive.StopInstance => StartStopInstance(p.getRef.asInstanceOf[Instance], nodeName, false)
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