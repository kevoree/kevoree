package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.library.gossiperNetty._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/05/11
 * Time: 13:56
 */

class LogGossiperActor (timeout: java.lang.Long, channel: NettyGossipAbstractElement, dataManager: DataManager,
  port: Int, fullUDP: java.lang.Boolean, garbage: Boolean, serializer: Serializer, selector: PeerSelector, alwaysAskModel: Boolean, peerSelector : StrictGroupPeerSelector)
  extends GossiperActor(timeout, channel, dataManager, port, fullUDP, false, serializer, selector, alwaysAskModel) {

  gossiperRequestSender = new
      LogGossiperRequestSender(timeout, channel, dataManager, fullUDP, garbage, serializer, alwaysAskModel, peerSelector)
  notificationRequestSender = new LogNotificationRequestSender(channel, peerSelector)
  gossiperRequestReceiver = new
      LogGossiperRequestReceiver(channel, dataManager, port, gossiperRequestSender, fullUDP, serializer, peerSelector)

}