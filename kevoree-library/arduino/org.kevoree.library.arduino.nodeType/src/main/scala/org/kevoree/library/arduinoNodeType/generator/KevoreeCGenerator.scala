/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoreeAdaptation.AdaptationModel
import org.kevoreeAdaptation.AddInstance
import org.kevoreeAdaptation.AddType
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._
import org.kevoree.library.arduinoNodeType.{PMemory, ArduinoBoardType}
import org.kevoree._

class KevoreeCGenerator
  extends KevoreeComponentTypeClassGenerator
  with KevoreeCFrameworkGenerator
  with KevoreeChannelTypeClassGenerator
  with KevoreeCRemoteAdminGenerator
  with KevoreeCSchedulerGenerator
  with KevoreePersistenceGenerator {

  def generate(
                adaptModel: AdaptationModel,
                nodeName: String,
                outputDir: String,
                bundleContext: BundleContext,
                boardName: String,
                pmem: PMemory,
                pmax: String
                ) {

    val componentTypes = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddType] && adt.asInstanceOf[AddType].getRef.isInstanceOf[ComponentType])
    val channelTypes = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddType] && adt.asInstanceOf[AddType].getRef.isInstanceOf[ChannelType])
    var ktypes: List[TypeDefinition] = List()
    componentTypes.foreach {
      ctype => ktypes = ktypes ++ List(ctype.asInstanceOf[AddType].getRef)
    }
    channelTypes.foreach {
      ctype => ktypes = ktypes ++ List(ctype.asInstanceOf[AddType].getRef)
    }

    generateKcFrameworkHeaders(ktypes, ArduinoBoardType.getFromTypeName(boardName),pmax)
    generateKcConstMethods(ktypes);
    generateKcFramework

    componentTypes.foreach {
      componentTypeAdaptation =>
        generateComponentType(componentTypeAdaptation.asInstanceOf[AddType].getRef.asInstanceOf[ComponentType], bundleContext, nodeName)
    }
    channelTypes.foreach {
      channelTypeAdaptation =>
        generateChannelType(channelTypeAdaptation.asInstanceOf[AddType].getRef.asInstanceOf[ChannelType], bundleContext, nodeName)
    }





    generateDestroyInstanceMethod(ktypes)
    generateParamMethod(ktypes)

    generateGlobalInstanceFactory(ktypes)
    generateRunInstanceMethod(ktypes)

    val instancesAdaption = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddInstance]).filter(adt => !adt.asInstanceOf[AddInstance].getRef.getTypeDefinition.isInstanceOf[GroupType])
    var instances: List[Instance] = List()
    instancesAdaption.foreach {
      instanceAdaption =>
        instances = instances ++ List(instanceAdaption.asInstanceOf[AddInstance].getRef)
    }

    generatePMemoryPrimitives(pmem);
    generateBindMethod(ktypes)
    generateUnBindMethod(ktypes)
    generatePushToChannelMethod(ktypes)

    generatePeriodicExecutionMethod(ktypes)
    generatePortQueuesSizeMethod(ktypes)
    generateNameToIndexMethod()
    generateCheckForAdminMsg()
    generateConcatKevscriptParser()
    generatePrimitivesPersistence();
    generatePMemInit(pmem);
    generateSavePropertiesMethod(ktypes)
    generateCompressEEPROM()
    generateSaveInstancesBindings(ktypes)
    generateParseCAdminMsg()
    generateSetup(instances, nodeName)
    generateNextExecutionGap(ktypes)
    generateTimeMethods()
    generateLoop
    generateFreeRamMethod()


    //GENERATE OUTPUT FILE
    context.toFile(outputDir, nodeName)

  }
}
