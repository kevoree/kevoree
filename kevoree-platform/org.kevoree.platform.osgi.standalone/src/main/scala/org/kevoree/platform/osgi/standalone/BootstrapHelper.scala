package org.kevoree.platform.osgi.standalone

import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/10/11
 * Time: 13:41
 * To change this template use File | Settings | File Templates.
 */

object BootstrapHelper {

  val logger = LoggerFactory.getLogger(this.getClass)

  def initModelInstance(model: ContainerRoot, defType: String) {

    val nodeName = System.getProperty("node.name")
    if (!model.getNodes.exists(n => n.getName == nodeName)) {
      //CREATE DEFAULT
      model.getTypeDefinitions.find(td => td.getName == defType) match {
        case Some(typeDefFound) => {
          logger.warn("Init default node instance for name "+nodeName)
          val node = KevoreeFactory.createContainerNode
          node.setName(nodeName)
          node.setTypeDefinition(typeDefFound)
          model.addNodes(node)
        }
        case None => logger.error("Default type not found for name " + defType)
      }


    }
  }


}