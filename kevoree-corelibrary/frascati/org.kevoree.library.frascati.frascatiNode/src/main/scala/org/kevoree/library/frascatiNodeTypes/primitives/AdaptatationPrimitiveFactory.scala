package org.kevoree.library.frascatiNodeTypes.primitives

;


import org.kevoreeAdaptation.AdaptationPrimitive

import org.kevoree.library.frascatiNodeTypes.FrascatiNode
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.kompare.JavaSePrimitive
import org.ow2.frascati.FraSCAti
import actors.Actor
import org.kevoree.{MBinding, ComponentInstance, DeployUnit}
import scala.collection.JavaConversions._


class AdaptatationPrimitiveFactory(frascati: FraSCAti, node: FrascatiNode, topKCL: KevoreeJarClassLoader, targetRuntime: Actor) {

  def getPrimitive(adaptationPrimitive: AdaptationPrimitive): org.kevoree.api.PrimitiveCommand = {
    adaptationPrimitive.getPrimitiveType.getName match {
      case JavaSePrimitive.AddDeployUnit => FrascatiAddDedployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit], node.getBootStrapperService, topKCL)
      case JavaSePrimitive.AddThirdParty => FrascatiAddDedployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit], node.getBootStrapperService, topKCL)
      case JavaSePrimitive.RemoveDeployUnit => FrascatiRemoveDeployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit], node.getBootStrapperService, topKCL)
      case JavaSePrimitive.RemoveThirdParty => FrascatiRemoveDeployUnit(adaptationPrimitive.getRef.asInstanceOf[DeployUnit], node.getBootStrapperService, topKCL)
      case JavaSePrimitive.RemoveInstance if (isFrascatiManeged(adaptationPrimitive.getRef)) => FrascatiRemoveInstance(adaptationPrimitive, frascati);
      case JavaSePrimitive.AddInstance if (isFrascatiManeged(adaptationPrimitive.getRef)) => RuntimeCommandWrapper(FrascatiAddInstance(adaptationPrimitive, frascati, node.getNodeName, node.getBootStrapperService), targetRuntime)
      case JavaSePrimitive.StartInstance if (isFrascatiManeged(adaptationPrimitive.getRef)) => FrascatiStartInstance(adaptationPrimitive, frascati);
      case JavaSePrimitive.StopInstance if (isFrascatiManeged(adaptationPrimitive.getRef)) => FrascatiStopInstance(adaptationPrimitive, frascati);
      case JavaSePrimitive.UpdateDictionaryInstance if (isFrascatiManeged(adaptationPrimitive.getRef)) => RuntimeCommandWrapper(FrascatiUpdateDictionaryInstance(adaptationPrimitive, frascati), targetRuntime);
      case JavaSePrimitive.AddBinding if (isFrascatiManeged(adaptationPrimitive.getRef)) => RuntimeCommandWrapper(FrascatiAddBindingCommand(adaptationPrimitive.getRef.asInstanceOf[MBinding],node.getNodeName),targetRuntime)
      case _ => node.getSuperPrimitive(adaptationPrimitive)
    }
    /*
          case "AddBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.MBinding] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
          eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
          e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);

      }
      case "UpdateBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.MBinding] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
          eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
          e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }

      case "RemoveBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.MBinding] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.MBinding].getPort.
          eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e =>
          e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "AddFragmentBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Channel] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
          e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new AddFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);

      }
      case "RemoveFragmentBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Channel] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
          e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new RemoveFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }
      case "UpdateFragmentBinding" => {
        if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.Channel] && adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Channel].getTypeDefinition.getDeployUnits.forall(e =>
          e.getTargetNodeType.get.getName.equals(classOf[FrascatiNode].getSimpleName()))) {
          new UpdateFragmentBinding(adaptationPrimitive);
        } else
          node.getSuperPrimitive(adaptationPrimitive);
      }

     */
  }

  def isFrascatiManeged(obj: Any): Boolean = {
    obj match {
      case binding: org.kevoree.MBinding => {
        binding.getPort.eContainer.asInstanceOf[ComponentInstance].getTypeDefinition.getDeployUnits.forall(e => e.getTargetNodeType.getName.equals(classOf[FrascatiNode].getSimpleName))
      }
      case instance: org.kevoree.Instance => {
        instance.getTypeDefinition.getDeployUnits.forall(e => e.getTargetNodeType.getName.equals(classOf[FrascatiNode].getSimpleName))
      }
      case _ => false
    }
  }

}