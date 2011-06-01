/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.Instance
import org.kevoree.TypeDefinition
import org.kevoreeAdaptation.AdaptationModel
import org.kevoreeAdaptation.AddInstance
import org.kevoreeAdaptation.AddType
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._

class KevoreeCGenerator 
extends KevoreeComponentTypeClassGenerator
   with KevoreeCFrameworkGenerator
   with KevoreeChannelTypeClassGenerator
   with KevoreeCRemoteAdminGenerator {

  def generate(adaptModel: AdaptationModel, nodeName: String,outputDir : String,bundleContext : BundleContext)= {    
    
    val componentTypes = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddType] && adt.asInstanceOf[AddType].getRef.isInstanceOf[ComponentType] )
    val channelTypes = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddType] && adt.asInstanceOf[AddType].getRef.isInstanceOf[ChannelType] )
    var ktypes : List[TypeDefinition] = List()
    componentTypes.foreach{ctype=> ktypes = ktypes ++ List(ctype.asInstanceOf[AddType].getRef)}
    channelTypes.foreach{ctype=> ktypes = ktypes ++ List(ctype.asInstanceOf[AddType].getRef)}
    
    generateKcFrameworkHeaders(ktypes)
    generateKcConstMethods(ktypes);
    generateKcFramework

    componentTypes.foreach{componentTypeAdaptation =>
      generateComponentType(componentTypeAdaptation.asInstanceOf[AddType].getRef.asInstanceOf[ComponentType],bundleContext)
    }
    channelTypes.foreach{channelTypeAdaptation =>
      generateChannelType(channelTypeAdaptation.asInstanceOf[AddType].getRef.asInstanceOf[ChannelType],bundleContext)
    }
    
    

    
    generateGlobalInstanceState
    generateDestroyInstanceMethod(ktypes)
    generateParamMethod(ktypes)
    generateParamsMethod
    generateGlobalInstanceFactory(ktypes)
    generateRunInstanceMethod(ktypes)
    
    val instancesAdaption = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddInstance] )
    var instances : List[Instance] = List()
    instancesAdaption.foreach{instanceAdaption=>
      instances = instances ++ List(instanceAdaption.asInstanceOf[AddInstance].getRef)
    }
    
    generateBindMethod(ktypes)
    generateUnBindMethod(ktypes)
    
    generatePeriodicExecutionMethod(ktypes)
    generatePortQueuesSizeMethod(ktypes)
    
    generateNameToIndexMethod()
    
    generateCheckForAdminMsg()
    generateConcatKevscriptParser()
    
    generateSetup(instances,nodeName)
    generateLoop
    
    
    generateFreeRamMethod
    
    
    //GENERATE OUTPUT FILE
    context.toFile(outputDir, nodeName)
                       
  }
}
