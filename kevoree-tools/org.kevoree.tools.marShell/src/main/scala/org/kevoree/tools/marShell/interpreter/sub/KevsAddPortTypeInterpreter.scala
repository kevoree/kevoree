package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.{TypeDefinition, PortType, ComponentType, KevoreeFactory}
import org.kevoree.tools.marShell.ast.{AddPortTypeStatment, CreateChannelTypeStatment}
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.slf4j.LoggerFactory

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 28/09/12
 * Time: 09:30
 */
case class KevsAddPortTypeInterpreter(self: AddPortTypeStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    var success : Boolean = false
    context.model.getTypeDefinitions.filter(ct=> ct.isInstanceOf[ComponentType]).find(tdef => tdef.getName == self.componentTypeName) match {
      case Some(e) =>
      {
        val portTypeRef = KevoreeFactory.createPortTypeRef
        portTypeRef.setName(self.portTypeName)

        if (self.optional.isDefined){
          portTypeRef.setOptional(true)
        } else {
          portTypeRef.setOptional(false)
        }
        // add Provided and  Required
        self.in match {
          case true =>
            e.asInstanceOf[ComponentType].addProvided(portTypeRef)
          case false =>
            e.asInstanceOf[ComponentType].addRequired(portTypeRef)
        }
        // set manuel generics Class Message
        if(self.typeport == "Message"){ self.className = Some("org.kevoree.framework.MessagePort")}
        // find Class in model
        context.model.getTypeDefinitions.filter(ct => ct.isInstanceOf[PortType]).find(t => t.getName.trim == self.className.get ) match {
          case Some(p) =>
            portTypeRef.setRef(p.asInstanceOf[PortType])
            success = true
          case None =>
            success = false
            logger.error("The port service can't be associated with the interface => " + self.className+" is not found")
        }
      }
      case None => {
        logger.error("The componentTypeName does not exist with name => " + self.componentTypeName)
        success = false
      }
    }
    success
  }
}
