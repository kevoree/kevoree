/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.{ContainerRoot, TypeDefinition}
import scala.collection.JavaConversions._
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.annotation.{Generate => KGenerate}
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.tools.aether.framework.JCLContextHandler
import org.kevoree.framework.{AbstractNodeType, KevoreeGeneratorHelper}

trait KevoreeReflectiveHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def recCallAnnotedMethod(instance : Object,genVal : String, tclazz: Class[_],context : GeneratorContext, alreadyCall : List[String] = List(), headers : Boolean = false){
    var alreadyCallLocal : List[String] = alreadyCall
    tclazz.getMethods.filterNot(method => alreadyCall.contains(method.getName)).foreach {
      method =>
        method.getAnnotations.foreach {
          annotation =>
            if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
              val generateAnnotation = annotation.asInstanceOf[KGenerate]
              if (generateAnnotation.value == genVal) {
                method.invoke(instance, context.getGenerator)
                if(headers){
                  context h context.getGenerator.getContent
                } else {
                  context b context.getGenerator.getContent
                }

                context.getGenerator.razGen()

                alreadyCallLocal = alreadyCallLocal ++ List(method.getName)

              }
            }
        }
    }
    if(tclazz.getSuperclass != null){
      recCallAnnotedMethod(instance,genVal,tclazz.getSuperclass,context,alreadyCallLocal,headers)
    }
  }

  def createStandaloneInstance(ct: TypeDefinition, nodeName: String,nodeTypeInstance : AbstractNodeType): Object = {
    //CREATE NEW INSTANCE
    var clazzFactory: Class[_] = null

    val nodeHost = ct.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == nodeName).get
    val nodeTypeName = nodeHost.getTypeDefinition
    val genPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct, nodeTypeName.getName)
    val activatorName = ct.getName + "Activator"

    val activatorClassName = genPackage + "." + activatorName
    /*if (bundleContext != null) {
      clazzFactory = bundleContext.getBundle.loadClass(activatorClassName)
    } else {
      clazzFactory = this.getClass.getClassLoader.loadClass(activatorClassName)
    }*/
    
    val  du = ct.foundRelevantDeployUnit(nodeHost)

    clazzFactory = nodeTypeInstance.getBootStrapperService.getKevoreeClassLoaderHandler.getKevoreeClassLoader(du).loadClass(activatorClassName)
    val activatorInstance = clazzFactory.newInstance

    val reflectiveInstanceActor = clazzFactory.getMethod("callFactory").invoke(activatorInstance)
    val clazzActor = reflectiveInstanceActor.getClass

    val reflectiveInstance = clazzActor.getMethods.find(method => {
      method.getName == "getKevoreeComponentType"
    }) match {
      case Some(method) => {
        method.invoke(reflectiveInstanceActor)
      }
      case None => reflectiveInstanceActor
    }

    //REFLEXIVE SET NODENAME
    reflectiveInstance.getClass.getMethods.find(method => method.getName == "setNodeName") match {
      case Some(method) => method.invoke(reflectiveInstance, nodeName)
      case None => logger.error("NodeName not set !")
    }





    //val clazz = reflectiveInstance.getClass


    reflectiveInstance


    //CREATE INSTANCE DICTIONARY
    /*
    val dictionary: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
    if (instance.getTypeDefinition.getDictionaryType != null) {
      if (instance.getTypeDefinition.getDictionaryType.getDefaultValues != null) {
        instance.getTypeDefinition.getDictionaryType.getDefaultValues.foreach {
          dv =>
          dictionary.put(dv.getAttribute.getName, dv.getValue)
        }
      }
    }

    if (instance.getDictionary != null) {
      instance.getDictionary.getValues.foreach {
        v =>
        dictionary.put(v.getAttribute.getName, v.getValue)
      }
    }
    //REFLEXIVE SET DICTIONARY
    clazz.getMethods.find(method => method.getName == "setDictionary") match {
      case Some(method)=> method.invoke(reflectiveInstance, dictionary)
      case None => println("Dictionary not set !")
    }

    //REFLEXIVE SET NAME
    clazz.getMethods.find(method => method.getName == "setName") match {
      case Some(method)=> method.invoke(reflectiveInstance, instance.asInstanceOf[NamedElement].getName)
      case None => println("Dictionary not set !")
    }
    //REFLEXIVE SET NODENAME
    clazz.getMethods.find(method => method.getName == "setNodeName") match {
      case Some(method)=> method.invoke(reflectiveInstance, nodeName)
      case None => println("Dictionary not set !")
    } */


  }

}
