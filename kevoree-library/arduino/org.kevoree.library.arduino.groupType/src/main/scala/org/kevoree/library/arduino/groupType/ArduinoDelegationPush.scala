package org.kevoree.library.arduino.groupType

import org.slf4j.{Logger, LoggerFactory}
import reflect.BeanProperty
import org.kevoree.{KevoreeFactory, ContainerRoot}
import java.lang.String
import org.kevoree.api.service.core.handler.{KevoreeModelHandlerService}

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 13:55
 */

class ArduinoDelegationPush(handler: KevoreeModelHandlerService, groupName: String,bs : org.kevoree.api.Bootstraper) {
  protected var logger: Logger = LoggerFactory.getLogger(this.getClass)
  @BeanProperty
  var model: ContainerRoot = _

  def deployAll() {

    if (model == null) {
      model = handler.getLastModel
    }
    model.getGroups.find(g => g.getName == groupName) match {
      case Some(group) => {
        group.getSubNodes.filter(sub => sub.getTypeDefinition.getName.toLowerCase.contains("arduino")).foreach {
          subNode =>
            deployNode(subNode.getName)
        }
      }
      case None => logger.warn("Group Not found")
    }
  }

  def deployNode(targetNodeName: String) {
    if (model == null) {
      model = handler.getLastModel
    }
    try {
      val nodeType = bs.bootstrapNodeType(model, targetNodeName,handler,null)
      nodeType match {
        case Some(gNodeType) => {
          model.getGroups.find(g => g.getName == groupName) match {
            case Some(group) => {
              val dictionary = group.getDictionary.getOrElse({
                val newdic = KevoreeFactory.createDictionary; group.setDictionary(Some(newdic)); newdic
              })

              val serialPort = org.kevoree.framework.KevoreeFragmentPropertyHelper.getPropertyFromFragmentGroup(group.eContainer.asInstanceOf[ContainerRoot],group.getName,"serialport",targetNodeName)

            //  val att = dictionary.getValues.find(value => value.getAttribute.getName == "serialport" && value.getTargetNode == targetNodeName).getOrElse(null)
              gNodeType.getClass.getMethods.find(method => method.getName == "push") match {
                case Some(method) => {
                  method.invoke(gNodeType, targetNodeName, model, serialPort)
                }
                case None => logger.error("No push method in group for name " + groupName)
              }


            }
            case None => logger.error("Group not found for name " + groupName)
          }
        }
        case None => logger.warn("Can't bootstrap NodeType")
      }
    } catch {
      case _@e => logger.error("Can deploy model to node " + targetNodeName, e)
    }
  }


}