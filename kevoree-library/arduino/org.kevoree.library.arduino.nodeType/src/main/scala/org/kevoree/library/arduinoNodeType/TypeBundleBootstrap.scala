/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoreeAdaptation.AdaptationModel
import org.osgi.framework.BundleContext
import org.kevoree.DeployUnit
import org.kevoree.kompare.JavaSePrimitive

object TypeBundleBootstrap {

  def bootstrapTypeBundle(adaptationModel : AdaptationModel,ctx:BundleContext){
    //Add All ThirdParty
    adaptationModel.getAdaptations.filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.AddThirdParty).foreach{adaptation=>
      val cmd = AddThirdPartyCommand(ctx,adaptation.getRef.asInstanceOf[DeployUnit])
      cmd.execute()
    }
    //Add All TypeDefinitionBundle
    adaptationModel.getAdaptations.filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit).foreach{adaptation=>
      val cmd = AddThirdPartyCommand(ctx,adaptation.getRef.asInstanceOf[DeployUnit])
      cmd.execute()
    }

  }

  def deployUnitKey(dp : DeployUnit) : String = {
    dp.getGroupName+dp.getUnitName+dp.getVersion+dp.getName
  }
  
}
