/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.kevoree.framework.message.Message
import scala.collection.JavaConversions._
import org.kevoree.annotation.{Generate => KGenerate}
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.framework.AbstractNodeType

trait KevoreeChannelTypeClassGenerator extends KevoreeCAbstractGenerator with KevoreeReflectiveHelper with KevoreeInstanceGenerator {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def generateChannelType(ct:ChannelType,nodeName:String,anodeType : AbstractNodeType) = {
    
    val instance = createStandaloneInstance(ct,nodeName,anodeType)
    val clazz = instance.getClass
    
    //GENERATE CLASS HEADER
    context b "class "+ct.getName+" : public KevoreeType {"
    context b " public : "
    context b "QueueList<kmessage> * input;"
    context b "kbindings * bindings;"
     
    //GENERATE DICTIONARY VALUES POINTERS
    generateDic(ct)

    context b "void updated_p(){"
    recCallAnnotedMethod(instance, "update", clazz, context)
    context b "}"


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
    recCallAnnotedMethod(instance,"classdestroy",clazz,context)

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

    recCallAnnotedMethod(instance,"periodic",clazz,context)

  
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
