package org.kevoree.library.javase.basicGossiper

import actors.{Actor, TIMEOUT}
import org.slf4j.LoggerFactory


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:24
 */

class GossiperActor (instance: GossiperComponent, timeout: Long, selector: PeerSelector, process: ProcessValue) extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass.getName)

  case class STOP ()

  case class NOTIFY_PEERS ()

  case class DO_GOSSIP (peer : String)

  def stop () {
    this ! STOP()
  }

  def doGossip (peer : String) {
    this ! DO_GOSSIP(peer)
  }

  def notifyPeers () {
    this ! NOTIFY_PEERS()
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      reactWithin(timeout.longValue) {
        case STOP() => stopInternal()
        case DO_GOSSIP(peer) => doGossipInternal(peer) // initialize a gossip
        case NOTIFY_PEERS() => notifyPeersInternal() // notify all connected peers that updates are available on this local node
        case TIMEOUT => pull() // periddically ask for update on the network
      }
    }
  }

  private def stopInternal () {
    this.exit()
  }

  private def doGossipInternal (peer: String) {
    process.initGossip(peer)
  }

  private def notifyPeersInternal () {
    process.notifyPeers()
  }

  private def pull () {
    val peer = selector.selectPeer(instance.getName)
    if (!peer.equals("")) {
      logger.debug("start pulling")
      process.initGossip(peer)
    }
  }

}