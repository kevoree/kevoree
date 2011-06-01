/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.kevoree.framework.message.Message
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._
import org.kevoree.annotation.{Generate => KGenerate}

trait KevoreeChannelTypeClassGenerator extends KevoreeCAbstractGenerator with KevoreeReflectiveHelper {

  def generateChannelType(ct:ChannelType,bundleContext : BundleContext) = {
    
    val instance = createStandaloneInstance(ct,bundleContext)
    val clazz = instance.getClass
    
    //GENERATE CLASS HEADER
    context b "class "+ct.getName+" : public KevoreeType {"
    context b " public : "
    context b "QueueList<kmessage> * input;"
    context b "kbindings * bindings;"
     
    //GENERATE DICTIONARY VALUES POINTERS
    if(ct.getDictionaryType != null){
      ct.getDictionaryType.getAttributes.foreach{ attribute =>
        context b "char "+attribute.getName+"[20];"
      }
    }

    context b "void init(){" //GENERATE INIT METHOD
    context b "input = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));"
    context b "if(input){"
    context b "   memset(input, 0, sizeof(QueueList<kmessage>));"
    context b "}"
    context b "bindings = (kbindings *) malloc(sizeof(kbindings));"
    context b "if(bindings){"
    context b "   memset(bindings, 0, sizeof(kbindings));"
    context b "}"

    context b "}" //END INIT METHOD
    
    context b "void destroy(){" //GENERATE DESTROY METHOD
    //USER DESTROY
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "classdestroy"){
            var localContext = new StringBuffer
            method.invoke(instance, localContext)
            context b localContext.toString
          }
        }
      }
    }
    context b "free(input);"
    context b "bindings->destroy();"
    context b "free(bindings);"
    context b "}" //END DESTROY METHOD
    
    
    
    
    context b "void runInstance(){" //GENERATE SPECIFIQUE RUN METHOD
    context b "if(!input->isEmpty()){"
    context b "kmessage * msg = &(input->pop());"
    context b ct.getName+"::dispatch(msg);"
    context b "}"
    // CALL PERIODIC CODE
    
    clazz.getMethods.foreach {method =>
      method.getAnnotations.foreach {annotation =>
        if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
          val generateAnnotation = annotation.asInstanceOf[KGenerate]
          if(generateAnnotation.value == "periodic"){
            var localContext = new StringBuffer
            method.invoke(instance, localContext)
            context b localContext.toString
          }
        }
      }
    }
  
    context b "}" //END RUN METHOD

    //GENERATE DISPATCH METHOD
    context b "void dispatch(kmessage * msg){"
    clazz.getMethods.find(method => method.getName == "dispatch") match {
      case Some(method) => {
          var localContext = new StringBuffer
          var message = new Message
          message.setContent(localContext)
          method.invoke(instance, message)
          context b localContext.toString
        }
      case None => println("method dispatch not found")
    }
    context b "}"
    
    /*
     ct.getRequired.foreach{ requiredPort =>
     context b "void "+requiredPort.getName+"_rport(kmessage * msg){"
     context b "if("+requiredPort.getName+"){" 
     context b requiredPort.getName+"->push(*msg);"
     context b "}" 
     context b "}"
     }*/
    
    //GENERATE Provided PORT METHOD
    /*
     ct.getProvided.foreach{ providedPort =>
     context b "void "+providedPort.getName+"_pport(kmessage * msg){"
      
     providedPort.getRef match {
     case mpt: MessagePortType => {
     providedPort.getMappings.find(mapping => mapping.getServiceMethodName == "process") match {
     case Some(mapping) => {
     clazz.getMethods.find(method => method.getName == mapping.getBeanMethodName) match {
     case Some(method) => {
     var localContext = new StringBuffer
     method.invoke(instance, localContext)
     context b localContext.toString
     }
     case _ => println("method not found")
     }
     }
     case None => println("Process not found")
     }
     }
     case _@pt => println("Not supported " + pt)
     }
      
     context b "}";
      
     }*/

    //context b "char * instanceName;" //GENERATE INSTANCE ATTRIBUTES
    context b "};" //END CLASS
  }
  
  
  /*
   *                 val typeDef = ci.getTypeDefinition.asInstanceOf[ComponentType]
   ci.getProvided.foreach {
   provided =>
   provided.getPortTypeRef.getRef match {
   case mpt: MessagePortType => {
   provided.getPortTypeRef.getMappings.find(mapping => mapping.getServiceMethodName == "process") match {
   case Some(mapping) => {
   clazz.getMethods.find(method => method.getName == mapping.getBeanMethodName) match {
   case Some(method) => {
   contextMap.get("body").get.append(generateMessageProvidedPort(ci, provided.getPortTypeRef, mpt, reflectiveInstance, method))
   }
   case _ => println("method not found")
   }
   }
   case None => println("Process not found")
   }
   }
   case _@pt => println("Not supported " + pt)
   }
   }
   ci.getRequired.foreach {
   required =>
   contextMap.get("body").get.append(generateMessageRequiredPort(ci, required.getPortTypeRef, required))
   }
   * 
   */
  
}
