/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import scala.actors.TIMEOUT

class GossiperActor (timeout: java.lang.Long, channel: NettyGossipAbstractElement, dataManager: DataManager, port: Int,
  fullUDP: java.lang.Boolean, garbage: Boolean, serializer: Serializer, selector: PeerSelector, alwaysAskModel: Boolean)
  extends actors.DaemonActor {

  protected var gossiperRequestSender = new
      GossiperRequestSender(timeout, channel, dataManager, fullUDP, garbage, serializer, alwaysAskModel)
  protected var notificationRequestSender = new NotificationRequestSender(channel)
  protected var gossiperRequestReceiver = new
      GossiperRequestReceiver(channel, dataManager, port, gossiperRequestSender, fullUDP, serializer)

  //this.start()


  /* PUBLIC PART */
  override def start () = {
    gossiperRequestReceiver.start()
    gossiperRequestSender.start()
    notificationRequestSender.start()
    super.start()
    this
  }

  case class STOP_GOSSIPER ()

  case class DO_GOSSIP (targetNodeName: String)

  case class NOTIFY_PEERS ()

  def stop () {
    this ! STOP_GOSSIPER()
  }

  def scheduleGossip (nodeName: String) {
    this ! DO_GOSSIP(nodeName)
  }

  def notifyPeers () {
    this ! NOTIFY_PEERS()
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      reactWithin(timeout.longValue) {
        case STOP_GOSSIPER() => {
          gossiperRequestSender.stop()
          gossiperRequestReceiver.stop()
          notificationRequestSender.stop()
          this.exit()
        }
        case DO_GOSSIP(targetNodeName) => doGossip(targetNodeName)
        case NOTIFY_PEERS() => notificationRequestSender.notifyPeersAction()
        case TIMEOUT => {
          val peer = selector.selectPeer(channel.getName) //channel.selectPeer
          if (!peer.equals("")) {
            doGossip(peer)
          }
        }
      }
    }
  }

  private def doGossip (targetNodeName: String) {
    if (targetNodeName != null && targetNodeName != "") {
      gossiperRequestSender.initGossipAction(targetNodeName)
    }
  }
}
