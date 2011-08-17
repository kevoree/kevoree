/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoreeAdaptation.AdaptationModel
import org.kevoreeAdaptation.AddDeployUnit
import org.kevoreeAdaptation.AddThirdParty
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{DeployUnit, ContainerRoot, TypeDefinition}

object TypeBundleBootstrap {

  def bootstrapTypeBundle(adaptationModel : AdaptationModel,ctx:BundleContext){
    //Add All ThirdParty
    adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[AddThirdParty]).foreach{adaptation=>
      val cmd = AddThirdPartyCommand(ctx,adaptation.asInstanceOf[AddThirdParty].getRef)
      cmd.execute()
    }
    //Add All TypeDefinitionBundle
    adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[AddDeployUnit]).foreach{adaptation=>
      val cmd = AddThirdPartyCommand(ctx,adaptation.asInstanceOf[AddDeployUnit].getRef)
      cmd.execute()
    }

  }

  def deployUnitKey(dp : DeployUnit) : String = {
    dp.getGroupName+dp.getUnitName+dp.getVersion+dp.getName
  }
  
}
