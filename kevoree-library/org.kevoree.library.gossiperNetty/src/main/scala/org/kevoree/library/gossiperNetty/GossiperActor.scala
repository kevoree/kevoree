/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.security.SecureRandom
import scala.actors.TIMEOUT
import scala.collection.JavaConversions._

class GossiperActor(timeout : java.lang.Long,channel : NettyGossiperChannel,dataManager : DataManager, port : Int) extends actors.DaemonActor {

  private var gossiperRequestSender = new GossiperRequestSender(timeout, channel,dataManager)
  private var notificationRequestSender = new NotificationRequestSender(channel)
  private var gossiperRequestReceiver = new GossiperRequestReceiver(channel,dataManager,port, gossiperRequestSender)
  this.start
  
  /* PUBLIC PART */
  case class STOP_GOSSIPER()
  case class DO_GOSSIP(targetNodeName : String)
  case class NOTIFY_PEERS()
  
  def stop(){
    this !? STOP_GOSSIPER()
  }

  
  def scheduleGossip(nodeName : String)={
    this ! DO_GOSSIP(nodeName)
  }
  
  def notifyPeers()={
    this ! NOTIFY_PEERS()
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
    loop {
      reactWithin(timeout.longValue){
        case STOP_GOSSIPER() => {
			gossiperRequestSender.stop
			gossiperRequestReceiver.stop
			notificationRequestSender.stop
			this.exit
		  }
        case DO_GOSSIP(targetNodeName) => doGossip(targetNodeName)
        case NOTIFY_PEERS() => notificationRequestSender.notifyPeersAction
        case TIMEOUT => {
			var peer = selectPeer
			if (!peer.equals("")) {
			  doGossip(selectPeer)
			}
		  }
	  }
	}
	  
  }

  
  private def doGossip(targetNodeName : String) ={
	
	if (targetNodeName != null && targetNodeName != "") {
	  println("launch Gossip")
	  gossiperRequestSender.initGossipAction(targetNodeName)
	  
	}
  }
  
  /*private def doNotifyPeers() ={
   channel.getOtherFragments.foreach{channelFragment =>
   notificationRequestSender.notifyPeerAction(channelFragment.getNodeName)  
   }
   }*/
  
  def selectPeer() : String ={
	var othersSize = channel.getOtherFragments().size();
	if (othersSize > 0) {
	  var diceRoller = new SecureRandom();
	  var peerIndex = diceRoller.nextInt(othersSize);
	  channel.getOtherFragments.get(peerIndex).getNodeName;
	} else {
	  ""
	}
  }
}
