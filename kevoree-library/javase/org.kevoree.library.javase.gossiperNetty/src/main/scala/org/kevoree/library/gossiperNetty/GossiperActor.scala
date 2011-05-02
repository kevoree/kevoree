/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.security.SecureRandom
import scala.actors.TIMEOUT
import scala.collection.JavaConversions._

class GossiperActor(timeout: java.lang.Long, channel: NettyGossipAbstractElement, dataManager: DataManager, port: Int, fullUDP: java.lang.Boolean, garbage: Boolean,serializer : Serializer, selector : PeerSelector) extends actors.DaemonActor {

	private var gossiperRequestSender = new GossiperRequestSender(timeout, channel, dataManager, fullUDP, garbage,serializer)
	private var notificationRequestSender = new NotificationRequestSender(channel)
	private var gossiperRequestReceiver = new GossiperRequestReceiver(channel, dataManager, port, gossiperRequestSender, fullUDP, serializer)

  this.start()

	/* PUBLIC PART */
	case class STOP_GOSSIPER()

	case class DO_GOSSIP(targetNodeName: String)

	case class NOTIFY_PEERS()

	def stop() {
		this ! STOP_GOSSIPER()
	}

	def scheduleGossip(nodeName: String) {
		this ! DO_GOSSIP(nodeName)
	}

	def notifyPeers() {
		this ! NOTIFY_PEERS()
	}

	/* PRIVATE PROCESS PART */
	def act() {
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
					val peer = selector.selectPeer(channel.getName)//channel.selectPeer
					if (!peer.equals("")) {
						doGossip(peer)
					}
				}
			}
		}
	}

	private def doGossip(targetNodeName: String) = {
		if (targetNodeName != null && targetNodeName != "") {
			//println("launch Gossip")
			gossiperRequestSender.initGossipAction(targetNodeName)
		}
	}
}
