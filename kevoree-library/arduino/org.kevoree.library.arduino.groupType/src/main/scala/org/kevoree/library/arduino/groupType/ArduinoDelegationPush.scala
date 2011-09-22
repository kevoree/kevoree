package org.kevoree.library.arduino.groupType

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import scala.collection.JavaConversions._
import org.slf4j.{Logger, LoggerFactory}
import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper
import org.osgi.framework.Bundle

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 13:55
 */

class ArduinoDelegationPush(handler: KevoreeModelHandlerService, groupName: String, bundle: Bundle) {
  protected var logger: Logger = LoggerFactory.getLogger(this.getClass)

  def deploy() {
    val model = handler.getLastModel
    model.getGroups.find(g => g.getName == groupName) match {
      case Some(group) => {
        group.getSubNodes.filter(sub => sub.getTypeDefinition.getName.toLowerCase.contains("arduino")).foreach {
          subNode =>
            try {
              val nodeTypeHelper = new NodeTypeBootstrapHelper
              val nodeType = nodeTypeHelper.bootstrapNodeType(model, subNode.getName, bundle.getBundleContext)
              nodeType match {
                case Some(gNodeType) => {
                  gNodeType.push(subNode.getName, model)
                }
                case None => logger.warn("Can't bootstrap NodeType")
              }
            }  catch {
              case _ @ e => logger.error("Can deploy model to node "+subNode.getName,e)
            }

        }
      }
      case None => logger.warn("Group Not found")
    }
  }

}