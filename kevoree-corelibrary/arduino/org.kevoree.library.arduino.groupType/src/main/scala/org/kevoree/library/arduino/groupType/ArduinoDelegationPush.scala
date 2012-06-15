package org.kevoree.library.arduino.groupType

import org.slf4j.{Logger, LoggerFactory}
import reflect.BeanProperty
import java.lang.String
import org.kevoree.api.service.core.handler.{KevoreeModelHandlerService}
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.extra.kserial.Utils.KHelpers
import org.kevoree.{Instance, KevoreeFactory, ContainerRoot}

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 13:55
 */

class ArduinoDelegationPush(handler: KevoreeModelHandlerService, groupName: String,bs : org.kevoree.api.Bootstraper,kevSFact : KevScriptEngineFactory) {
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


  private def setProperty (model: ContainerRoot, instance: Instance, name: String, key: String, isFragment: Boolean = false, nodeNameForFragment: String = "",value : Object) = {
    instance.getDictionary match {
      case None => {
        None
      }
      case Some(dictionary) => {
        dictionary.getValues.find(dictionaryAttribute =>
          dictionaryAttribute.getAttribute.getName == key &&
            ((isFragment && dictionaryAttribute.getTargetNode.isDefined && dictionaryAttribute.getTargetNode.get.getName == nodeNameForFragment) || !isFragment)) match {
          case None =>   // todo
          case Some(dictionaryAttribute) => dictionaryAttribute.setValue(value.toString)
        }
      }
    }
  }


  def deployNode(targetNodeName: String) {
    if (model == null) {
      model = handler.getLastModel
    }
    try {
      val nodeType = bs.bootstrapNodeType(model, targetNodeName,handler,kevSFact)
      nodeType match {
        case Some(gNodeType) => {
          model.getGroups.find(g => g.getName == groupName) match {
            case Some(group) => {
              val dictionary = group.getDictionary.getOrElse({
                val newdic = KevoreeFactory.createDictionary; group.setDictionary(Some(newdic)); newdic
              })

              var serialPort = org.kevoree.framework.KevoreeFragmentPropertyHelper.getPropertyFromFragmentGroup(group.eContainer.asInstanceOf[ContainerRoot],group.getName,"serialport",targetNodeName)
              if(serialPort != null && serialPort == "*")
              {
                val ports = KHelpers.getPortIdentifiers
                if(ports.size() > 0){
                  serialPort = ports.get(0)
                }
              }else
              {
                  // update model
                  model.getHubs.foreach(channel =>
                  {
                    setProperty(model, channel, channel.getName, "serialport", true, targetNodeName,serialPort.replace("/",";"))
                  }
                  )

              }



            //  val att = dictionary.getValues.find(value => value.getAttribute.getName == "serialport" && value.getTargetNode == targetNodeName).getOrElse(null)
              gNodeType.startNode()
              gNodeType.getClass.getMethods.find(method => method.getName == "push") match {
                case Some(method) => {
                  method.invoke(gNodeType, targetNodeName, model, serialPort)
                }
                case None => logger.error("No push method in group for name " + groupName)
              }
              gNodeType.stopNode()


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