package org.kevoree.experiment.library.gossiperNetty

import scala.collection.JavaConversions._
import java.net.{InetAddress, InetSocketAddress}
import org.slf4j.LoggerFactory
import org.kevoree.library.gossiperNetty.{NettyGossipAbstractElement, NotificationRequestSender}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/05/11
 * Time: 14:14
 */

class LogNotificationRequestSender (channelFragment: NettyGossipAbstractElement) extends NotificationRequestSender (channelFragment: NettyGossipAbstractElement) {
  private val logger = LoggerFactory.getLogger(classOf[LogNotificationRequestSender])

  override protected def writeMessage (o: Object, address: InetSocketAddress) {
    var networkIsDown = false
    var targetNodeName = ""
    FailureSimulation.failureOutNode.foreach {
      nodeName =>
        logger.debug(nodeName + " is one of the node available from here")
        if (channelFragment.parsePortNumber(nodeName).equals(address.getPort) &&
          isEquals(channelFragment.getAddress(nodeName), address.getAddress)) {
          networkIsDown = true
          targetNodeName = nodeName
          logger.debug("the message won't be sent because the node is register as a node failure")
        }
    }
    if (!networkIsDown) {
      logger.debug("message is sent by LogNotification.")
      channel.write(o, address)
    } else {
      logger.debug("message is not sent because the link with " + targetNodeName + " is broken")
    }
  }

  private def isEquals (address: String, inetAddress: InetAddress): Boolean = {
    address.equals(inetAddress.getHostName) ||
      address.equals(inetAddress.getHostAddress)
  }
}