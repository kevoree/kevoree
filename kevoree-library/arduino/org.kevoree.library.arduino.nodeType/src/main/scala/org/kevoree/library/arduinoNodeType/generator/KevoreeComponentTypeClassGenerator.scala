/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._
import org.kevoree.annotation.{Generate => KGenerate}
import org.slf4j.{LoggerFactory, Logger}

trait KevoreeComponentTypeClassGenerator extends KevoreeCAbstractGenerator with KevoreeReflectiveHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def generateComponentType(ct:ComponentType,bundleContext : BundleContext,nodeName:String) = {
    
    val instance = createStandaloneInstance(ct,bundleContext,nodeName)
    val clazz = instance.getClass
    
    //GENERATE CLASS HEADER
    context b "class "+ct.getName+" : public KevoreeType {"
    context b " public : "
    
    var nextExecutionMustBeInit = false
    if(ct.getDictionaryType.isDefined){
      if(ct.getDictionaryType.get.getAttributes.exists(att => att.getName == "period")){
        context b "unsigned long nextExecution;"
        nextExecutionMustBeInit = true;
      }
    }
    
    //INVOKE CLASS HEADER
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "classheader"){
            val localContext = new StringBuffer
            method.invoke(instance, localContext)
            context b localContext.toString
          }
        }
      }
    }
    //INVOKE GLOABL HEADER
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "header"){
            val localContext = new StringBuffer
            method.invoke(instance, localContext)
            context h localContext.toString
          }
        }
      }
    }   
    
    
    ct.getProvided.foreach{ providedPort => //GENERATE PROVIDED PORT QUEUES
      context b "QueueList<kmessage> * "+providedPort.getName+";"
    }
    ct.getRequired.foreach{ requiredPort =>
      context b "kbinding * "+requiredPort.getName+";" //GENERATE REQUIRED PORT QUEUES
    }
     
    //GENERATE DICTIONARY VALUES POINTERS
    if(ct.getDictionaryType.isDefined){
      ct.getDictionaryType.get.getAttributes.foreach{ attribute =>
        context b "char "+attribute.getName+"[20];"
      }
    }

    context b "void init(){" //GENERATE INIT METHOD
    ct.getProvided.foreach{ providedPort =>
      context b providedPort.getName+" = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));"
      context b "if("+providedPort.getName+"){"
      context b "   memset("+providedPort.getName+", 0, sizeof(QueueList<kmessage>));"
      context b "}"
    }
    ct.getRequired.foreach{ requiredPort =>
      context b requiredPort.getName+" = (kbinding*) malloc(sizeof(kbinding));"
      context b "if("+requiredPort.getName+"){"
      context b "   memset("+requiredPort.getName+", 0, sizeof(kbinding));"
      context b "}"
    }

    //USER INIT
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "classinit"){
            val localContext = new StringBuffer
            method.invoke(instance, localContext)
            context b localContext.toString
          }
        }
      }
    }
    if(nextExecutionMustBeInit){context b "nextExecution = millis();"}
    context b "}" //END INIT METHOD
    
    
    context b "void destroy(){" //GENERATE DESTROY METHOD
    ct.getProvided.foreach{ providedPort =>
      context b "free("+providedPort.getName+");"
    }
    ct.getRequired.foreach{ requiredPort =>
      context b "free("+requiredPort.getName+");"
    }

    //USER DESTROY
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "classdestroy"){
            val localContext = new StringBuffer
            method.invoke(instance, localContext)
            context b localContext.toString
          }
        }
      }
    }
    context b "}" //END DESTROY METHOD
    
    
    

    
    context b "void runInstance(){" //GENERATE SPECIFIQUE RUN METHOD
    ct.getProvided.foreach{ providedPort =>
      context b "if(!"+providedPort.getName+"->isEmpty()){"
      context b "kmessage * msg = &("+providedPort.getName+"->pop());"
      context b ct.getName+"::"+providedPort.getName+"_pport(msg);"
      context b "}"
    }
    // CALL PERIODIC CODE
    
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "periodic"){
            val localContext = new StringBuffer
            method.invoke(instance, localContext)
            context b localContext.toString
          }
        }
      }
    }
    
    if(nextExecutionMustBeInit){
      context b "nextExecution += atol(period);"
    }
    
    context b "}" //END RUN METHOD

    ct.getRequired.foreach{ requiredPort =>
      context b "void "+requiredPort.getName+"_rport(kmessage * msg){"
      context b "if("+requiredPort.getName+"->port){"
      context b requiredPort.getName+"->port->push(*msg);"
      context b "}" 
      context b "}"
    }
    
    //GENERATE Provided PORT METHOD
    ct.getProvided.foreach{ providedPort =>
      context b "void "+providedPort.getName+"_pport(kmessage * msg){"
      
      providedPort.getRef match {
        case mpt: MessagePortType => {
            providedPort.getMappings.find(mapping => mapping.getServiceMethodName == "process") match {
              case Some(mapping) => {
                  clazz.getMethods.find(method => method.getName == mapping.getBeanMethodName) match {
                    case Some(method) => {
                        val localContext = new StringBuffer
                        method.invoke(instance, localContext)
                        context b localContext.toString
                      }
                    case _@e => logger.error("method not found", e)
                  }
                }
              case None => logger.error("Process not found")
            }
          }
        case _@pt => logger.error("Not supported " + pt)
      }
      
      context b "}";
    }
    context b "};" //END CLASS
  }
  
}
