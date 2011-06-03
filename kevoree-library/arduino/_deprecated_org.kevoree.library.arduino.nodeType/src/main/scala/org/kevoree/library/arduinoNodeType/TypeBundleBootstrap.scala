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

object TypeBundleBootstrap {

  def bootstrapTypeBundle(adaptationModel : AdaptationModel,ctx:BundleContext){
    //Add All ThirdParty

    adaptationModel.getAdaptations.foreach({
      adapt=> println(adapt)
    })


    adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[AddThirdParty]).foreach{adaptation=>
      AddThirdPartyCommand(ctx,adaptation.asInstanceOf[AddThirdParty].getRef).execute
    }
    //Add All TypeDefinitionBundle
    adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[AddDeployUnit]).foreach{adaptation=>
      AddThirdPartyCommand(ctx,adaptation.asInstanceOf[AddDeployUnit].getRef).execute
    }
  }
  
}
