/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.security.SecureRandom
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.DatagramChannel
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder
import org.kevoree.framework.AbstractChannelFragment
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import scala.actors.TIMEOUT
import scala.collection.JavaConversions._

class GossiperActor(timeout : java.lang.Long,channel : NettyGossiperChannel,dataManager : DataManager, port : Int) extends actors.DaemonActor {

  private var gossiperRequestSender = new GossiperRequestSender(timeout, channel,dataManager,port)
  private var notificationRequestSender = new NotificationRequestSender(channel,port)
  private var gossiperRequestReceiver = new GossiperRequestReceiver(channel,dataManager,port, gossiperRequestSender)
  this.start
  
  /* PUBLIC PART */
  case class STOP_GOSSIPER()
  case class DO_GOSSIP(targetNodeName : String)
  case class NOTIFY_PEERS()
  
  def stop(){
    this ! STOP_GOSSIPER()
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
        case TIMEOUT => doGossip(selectPeer)
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
	var diceRoller = new SecureRandom();
	var peerIndex = diceRoller.nextInt(othersSize);
	channel.getOtherFragments.get(peerIndex).getNodeName;
  }
}
