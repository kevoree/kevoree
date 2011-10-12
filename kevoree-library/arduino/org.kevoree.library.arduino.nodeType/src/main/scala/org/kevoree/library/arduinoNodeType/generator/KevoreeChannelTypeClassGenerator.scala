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
import org.slf4j.{LoggerFactory, Logger}

trait KevoreeChannelTypeClassGenerator extends KevoreeCAbstractGenerator with KevoreeReflectiveHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def generateChannelType(ct:ChannelType,bundleContext : BundleContext,nodeName:String) = {
    
    val instance = createStandaloneInstance(ct,bundleContext,nodeName)
    val clazz = instance.getClass
    
    //GENERATE CLASS HEADER
    context b "class "+ct.getName+" : public KevoreeType {"
    context b " public : "
    context b "QueueList<kmessage> * input;"
    context b "kbindings * bindings;"
     
    //GENERATE DICTIONARY VALUES POINTERS
    if(ct.getDictionaryType.isDefined){
      ct.getDictionaryType.get.getAttributes.foreach{ attribute =>
        if(attribute.getDatatype.startsWith("enum=")){
          val enumValues: String = attribute.getDatatype.replaceFirst("enum=", "")
          var maxLenght : Int = 0
          enumValues.split(",").foreach {
            value => maxLenght = scala.math.max(maxLenght,value.size)
          }
          context b "char "+attribute.getName+"["+maxLenght+"];"
        } else {
          context b "char "+attribute.getName+"[20];"
        }
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
            val localContext = new StringBuffer
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
            val localContext = new StringBuffer
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
          val localContext = new StringBuffer
          val message = new Message
          message.setContent(localContext)
          method.invoke(instance, message)
          context b localContext.toString
        }
      case None => logger.error("method dispatch not found")
    }
    context b "}"
    
    context b "};" //END CLASS
  }

  
}
