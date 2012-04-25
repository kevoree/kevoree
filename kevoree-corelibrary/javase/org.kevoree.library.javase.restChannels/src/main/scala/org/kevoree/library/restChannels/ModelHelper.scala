package org.kevoree.library.restChannels

import org.kevoree.framework.KevoreePlatformHelper
import org.kevoree.ContainerRoot

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/10/11
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */

object ModelHelper {
  def buildURL(modelService: org.kevoree.api.service.core.handler.KevoreeModelHandlerService, remoteNodeName: String, remoteChannelName : String): String = {
    var ip: String = KevoreePlatformHelper.getProperty(modelService.getLastModel, remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    if (ip == null || (ip == "")) {
      ip = "127.0.0.1"
    }
    var port: String = ""
    import scala.collection.JavaConversions._
    for (node <- modelService.getLastModel.getNodes) {
      if (node.getName == remoteNodeName) {
        import scala.collection.JavaConversions._
        node.getDictionary.map {
          dic =>
            dic.getValues.foreach { value =>
              if (value.getAttribute.getName == "port") {
                port = value.getValue
              }
            }
        }
      }
    }
    if (port == null || (port == "")) {
      port = "8000"
    }
    return "http://" + ip + ":" + port + "/channels/" + remoteChannelName
  }

}