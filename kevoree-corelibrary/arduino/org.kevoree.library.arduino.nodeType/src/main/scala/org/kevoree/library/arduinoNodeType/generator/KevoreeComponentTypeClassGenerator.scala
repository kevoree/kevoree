/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.kevoree.annotation.{Generate => KGenerate}
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent
import org.kevoree.framework.AbstractNodeType
import scala.collection.JavaConversions._

trait KevoreeComponentTypeClassGenerator extends KevoreeCAbstractGenerator with KevoreeReflectiveHelper with KevoreeInstanceGenerator {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def generateComponentType(ct: ComponentType, nodeName: String,nodeTypeInstance:AbstractNodeType) = {

    val instance = createStandaloneInstance(ct, nodeName,nodeTypeInstance)
    if (instance.isInstanceOf[AbstractArduinoComponent]) {
      instance.asInstanceOf[AbstractArduinoComponent].setGenerator(context.getGenerator)
    }

    val clazz = instance.getClass

    //GENERATE CLASS HEADER
    context b "class " + ct.getName + " : public KevoreeType {"
    context b " public : "

    var nextExecutionMustBeInit = false
    if (ct.getDictionaryType!=null) {
      if (ct.getDictionaryType.getAttributes.exists(att => att.getName == "period")) {
        context b "unsigned long nextExecution;"
        nextExecutionMustBeInit = true;
      }
    }

    //INVOKE CLASS HEADER
    recCallAnnotedMethod(instance, "classheader", clazz, context)
    //INVOKE GLOABL HEADER
    recCallAnnotedMethod(instance, "header", clazz, context, headers = true)

    ct.getProvided.foreach {
      providedPort => //GENERATE PROVIDED PORT QUEUES
        context b "QueueList<kmessage> * " + providedPort.getName + ";"
    }
    ct.getRequired.foreach {
      requiredPort =>
        context b "kbinding * " + requiredPort.getName + ";" //GENERATE REQUIRED PORT QUEUES
    }

    //GENERATE DICTIONARY VALUES POINTERS

    generateDic(ct)

    context b "void updated_p(){"
    recCallAnnotedMethod(instance, "update", clazz, context)
    context b "}"

    context b "void init(){" //GENERATE INIT METHOD
    ct.getProvided.foreach {
      providedPort =>
        context b providedPort.getName + " = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));"
        context b "if(" + providedPort.getName + "){"
        context b "   memset(" + providedPort.getName + ", 0, sizeof(QueueList<kmessage>));"
        context b "}"
    }
    ct.getRequired.foreach {
      requiredPort =>
        context b requiredPort.getName + " = (kbinding*) malloc(sizeof(kbinding));"
        context b "if(" + requiredPort.getName + "){"
        context b "   memset(" + requiredPort.getName + ", 0, sizeof(kbinding));"
        context b "}"
    }

    //USER INIT
    recCallAnnotedMethod(instance, "classinit", clazz, context)

    if (nextExecutionMustBeInit) {
      context b "nextExecution = millis();"
    }
    context b "}" //END INIT METHOD


    context b "void destroy(){" //GENERATE DESTROY METHOD
    ct.getProvided.foreach {
      providedPort =>
        context b "free(" + providedPort.getName + ");"
    }
    ct.getRequired.foreach {
      requiredPort =>
        context b "free(" + requiredPort.getName + ");"
    }

    //USER DESTROY
    recCallAnnotedMethod(instance, "classdestroy", clazz, context)


    context b "}" //END DESTROY METHOD





    context b "void runInstance(){" //GENERATE SPECIFIQUE RUN METHOD
    ct.getProvided.foreach {
      providedPort =>
        context b "if(!" + providedPort.getName + "->isEmpty()){"
        context b "kmessage * msg = &(" + providedPort.getName + "->pop());"
        context b ct.getName + "::" + providedPort.getName + "_pport(msg);"
        context b "}"
    }
    // CALL PERIODIC CODE
    recCallAnnotedMethod(instance, "periodic", clazz, context)

    if (nextExecutionMustBeInit) {
      context b "nextExecution += period;"
    }

    context b "}" //END RUN METHOD

    ct.getRequired.foreach {
      requiredPort =>
        context b "void " + requiredPort.getName + "_rport(kmessage * msg){"
        context b "if(" + requiredPort.getName + "->port){"
        context b requiredPort.getName + "->port->push(*msg);"
        context b "}"
        context b "}"
    }

    //GENERATE Provided PORT METHOD
    ct.getProvided.foreach {
      providedPort =>
        context b "void " + providedPort.getName + "_pport(kmessage * msg){"

        providedPort.getRef match {
          case mpt: MessagePortType => {
            providedPort.getMappings.find(mapping => mapping.getServiceMethodName == "process") match {
              case Some(mapping) => {
                clazz.getMethods.find(method => method.getName == mapping.getBeanMethodName) match {
                  case Some(method) => {
                    context.getGenerator.setPortName(providedPort.getName)
                    method.invoke(instance, context.getGenerator)
                    context b context.getGenerator.getContent
                    context.getGenerator.razGen()
                    context.getGenerator.setPortName("")
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
