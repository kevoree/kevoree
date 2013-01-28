package org.kevoree.library.defaultNodeTypes.jcl.deploy

import command._
import org.kevoree.kompare.JavaSePrimitive
import org.kevoree.{Channel, MBinding, Instance, DeployUnit}
import org.kevoree.api.PrimitiveCommand
import org.kevoree.framework.AbstractNodeType
import java.util

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:02
 */

class CommandMapper {

  var nodeType : AbstractNodeType = null
  def setNodeType(n : AbstractNodeType) { nodeType = n }
  def getNodeType : AbstractNodeType = nodeType
  val toClean = new util.ArrayList[EndAwareCommand]

  def doEnd(){
    import scala.collection.JavaConversions._
    toClean.foreach{
      c => c.doEnd
    }
    toClean.clear()
  }

  def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive, nodeName: String): PrimitiveCommand = {
    p.getPrimitiveType.getName match {
      case JavaSePrimitive.UpdateDictionaryInstance if(p.getRef.asInstanceOf[Instance].getName == nodeName) => SelfDictionaryUpdate(p.getRef.asInstanceOf[Instance],nodeType)
      case JavaSePrimitive.AddDeployUnit => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService)
      case JavaSePrimitive.UpdateDeployUnit => {
        val res = UpdateDeployUnit(p.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService)
        toClean.add(res)
        res
      }
      case JavaSePrimitive.RemoveDeployUnit => {
        val res = RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService)
        toClean.add(res)
        res
      }
      case JavaSePrimitive.AddThirdParty => AddDeployUnit(p.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService)
      case JavaSePrimitive.RemoveThirdParty => RemoveDeployUnit(p.getRef.asInstanceOf[DeployUnit],nodeType.getBootStrapperService)
      case JavaSePrimitive.AddInstance => AddInstance(p.getRef.asInstanceOf[Instance], nodeName, nodeType.getModelService, nodeType.getKevScriptEngineFactory,nodeType.getBootStrapperService)
      case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionary(p.getRef.asInstanceOf[Instance], nodeName)
      case JavaSePrimitive.RemoveInstance => RemoveInstance(p.getRef.asInstanceOf[Instance], nodeName, nodeType.getModelService, nodeType.getKevScriptEngineFactory,nodeType.getBootStrapperService)
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