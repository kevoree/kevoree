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
import org.kevoree.library.arduinoNodeType.{PMemory, ArduinoBoardType}

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
                pmem: PMemory
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

    generateKcFrameworkHeaders(ktypes, ArduinoBoardType.getFromTypeName(boardName))
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




    generateGlobalInstanceState
    generateDestroyInstanceMethod(ktypes)
    generateParamMethod(ktypes)
    generateParamsMethod
    generateGlobalInstanceFactory(ktypes)
    generateRunInstanceMethod(ktypes)

    val instancesAdaption = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddInstance])
    var instances: List[Instance] = List()
    instancesAdaption.foreach {
      instanceAdaption =>
        instances = instances ++ List(instanceAdaption.asInstanceOf[AddInstance].getRef)
    }

    generateReadPMemory(pmem);
    generateBindMethod(ktypes)
    generateUnBindMethod(ktypes)

    generatePeriodicExecutionMethod(ktypes)
    generatePortQueuesSizeMethod(ktypes)

    generateNameToIndexMethod()

    generateCheckForAdminMsg()
    generateConcatKevscriptParser()


    generateUDICommandSave();
    generateAINCommandSave();
    generateBICommandSave();
    generateRINCommandSave();
    generateSave2Memory(pmem);
    generateSave2MemoryNoInc(pmem);
    generatePMemInit(pmem);



    generateSavePropertiesMethod(ktypes)
    generateCompressEEPROM()
    generateSaveInstancesBindings(ktypes)

    generateParseCAdminMsg()
    generateScriptFromEEPROM()
    generateSetup(instances, nodeName)


    generateNextExecutionGap(ktypes)
    generateCurrentTimeMethod()
    generateSleepMethod()
    generateLoop


    generateFreeRamMethod()


    //GENERATE OUTPUT FILE
    context.toFile(outputDir, nodeName)

  }
}
