/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.UUID
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.DatagramChannel
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder
import org.kevoree.extra.marshalling.RichString
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.library.gossip.Gossip.UUIDDataRequest
import org.kevoree.library.gossip.Gossip.UUIDVectorClockRequest
import org.kevoree.library.gossip.Gossip.VectorClockUUID
import org.kevoree.library.gossip.Gossip.VectorClockUUIDs
import org.kevoree.library.gossip.Gossip.VectorClockUUIDsRequest
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.kevoree.library.version.Version.ClockEntry
import org.kevoree.library.version.Version.VectorClock
import org.kevoree.library.gossip.Gossip.VersionedModel
import org.kevoree.library.gossiper.version.Occured
import scala.collection.JavaConversions._

class GossiperRequestSender(timeout : java.lang.Long,channelFragment : NettyGossiperChannel,dataManager : DataManager, port : Int) extends actors.DaemonActor {

  // define attributes used to define channel to send gossip request
  var factory =  new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrap = new ConnectionlessBootstrap(factory)
  var self = this
  bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        return Channels.pipeline(
          new ProtobufEncoder(),
          new ProtobufDecoder(Message.getDefaultInstance()),
          new GossiperRequestSenderHandler(self))
      }
    }
  )
  
  this.start
  
  private var channel : Channel = null
  private var peerName : String = null
  
  /* PUBLIC PART */
  case class STOP_GOSSIPER()
  case class INIT_GOSSIP(peer : String)
  case class INIT_SECOND_STEP(message : Message, address : SocketAddress, channel : Channel)
  case class INIT_LAST_STEP(message : Message, address : SocketAddress, channel : Channel)
  case class END_GOSSIP(message : Message)
  
  def stop(){
    this ! STOP_GOSSIPER()
  }
  
  def initGossipAction(peer : String) ={
	this ! INIT_GOSSIP(peer)
  }
  
  def initSecondStepAction(message : Message, address : SocketAddress, channel : Channel) ={
	this ! INIT_SECOND_STEP(message, address,channel)
  }
  
  def initLastStepAction(message : Message, address : SocketAddress, channel : Channel) ={
	this ! INIT_LAST_STEP(message, address,channel)
  }
  
  def endGossipAction(message : Message) ={
	this ! END_GOSSIP(message)
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
    loop {
	  react {
		//reactWithin(timeout.longValue){
        case STOP_GOSSIPER() => {
			channel.close
			this.exit
		  }
        case INIT_GOSSIP(peer) => {
			channel.close
			initGossip(peer)
		  }
		case INIT_SECOND_STEP(message, address,channel) => initSecondStep(message, address,channel)
		case INIT_LAST_STEP(message, address,channel) => initLastStep(message, address,channel)
		case END_GOSSIP(message) => endGossip(message)
	  }
	}
	  
  }
  
  private def initGossip(peer : String) ={
	
	if (peer != null && peer != "") {
	  println("launch Gossip with " + peer)
	  var remoteChannelFragment : KevoreeChannelFragment= null
	  channelFragment.getOtherFragments.find(c => c.getNodeName == peer) match {
		case Some(channel) => remoteChannelFragment = channel
		case None => 
	  }
	  if (remoteChannelFragment != null) {
		channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
		var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
		messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName).setContent(VectorClockUUIDsRequest.newBuilder.build.toByteString)
	
		channel.write(messageBuilder.build, new InetSocketAddress(channelFragment.getAddress(peer), port));
	 
	  } 
	}
  }
  
  private def initSecondStep(message : Message, address : SocketAddress, removeChannel : Channel) ={
	var remoteChannelFragment : KevoreeChannelFragment= null
	channelFragment.getOtherFragments.find(c => c.getName == message.getDestChannelName) match {
	  case Some(channel) => remoteChannelFragment = channel
	  case None => 
	}
	if (remoteChannelFragment != null && message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) {
	 
	  var remoteVectorClockUUIDs = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VectorClockUUIDs])
	  if(remoteVectorClockUUIDs!=null){
        /* check for new uuid values*/
        remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach{vectorClockUUID=>
		  var uuid = UUID.fromString(vectorClockUUID.getUuid)
          if(dataManager.GetUUIDVectorClock(uuid)==null){
            dataManager.setData(uuid, 
								Tuple2[VectorClock,org.kevoree.framework.message.Message](VectorClock.newBuilder.setTimestamp(System.currentTimeMillis).build,null))
          } 
        }
        /* check for deleted uuid values */
        var localUUIDs = dataManager.getUUIDVectorClocks
        localUUIDs.keySet.foreach{key=>
          if (!remoteVectorClockUUIDs.getVectorClockUUIDsList.contains(key)) {
			if(dataManager.getUUIDVectorClock(key).getEntiesList.exists(e=> e.getNodeID == message.getDestChannelName)){
			  //ALREADY SEEN VECTOR CLOCK - GARBAGE IT
			  dataManager.removeData(key)
			} else {
			  //NOOP - UNCOMPLETE VECTOR CLOCK
			}
		  }	 
		}
	  }
	  
	  //FOREACH UUIDs
	  var vectorClockUUIDs = dataManager.getUUIDVectorClocks
	  vectorClockUUIDs.keySet.foreach{local_UUID=>
		//var remoteVectorClock = group.getUUIDVectorClockFromPeer(targetNodeName, local_UUID)
		var vectorClockUUIDRequest = UUIDVectorClockRequest.newBuilder.setUuid(local_UUID.toString).build
		var messageRequest = Message.newBuilder.setDestChannelName(channelFragment.getName).setContentClass(classOf[UUIDVectorClockRequest].getName).setContent(vectorClockUUIDRequest.toByteString).build
		  
		//var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
		channel = removeChannel
		channel.write(messageRequest, address);
	  }  
	}
  }
  
  
  private def initLastStep(message : Message, address : SocketAddress, removeChannel : Channel) ={
	var remoteChannelFragment : KevoreeChannelFragment= null
	channelFragment.getOtherFragments.find(c => c.getName == message.getDestChannelName) match {
	  case Some(channel) => remoteChannelFragment = channel
	  case None => 
	}
	if (remoteChannelFragment != null && message.getContentClass.equals(classOf[VectorClockUUID].getName)) {
	  var remoteVectorClockUUID = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VectorClockUUID])
	  var uuid = UUID.fromString(remoteVectorClockUUID.getUuid)
	  var remoteVectorClock = remoteVectorClockUUID.getVector
	  var occured = VersionUtils.compare(dataManager.getUUIDVectorClock(uuid), remoteVectorClock)
	  occured match {
		case Occured.AFTER=>
		  removeChannel.close
		  channel = null
		case Occured.BEFORE=> {
			//updateValue(message.getDestChannelName,uuid,remoteVectorClock)
			//var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
			var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
			messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
			channel = removeChannel
			channel.write(messageBuilder.build, address);
		  }
		case Occured.CONCURRENTLY=> {
			//updateValue(message.getDestChannelName,uuid,remoteVectorClock)
			//var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
			var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
			messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
			channel = removeChannel
			channel.write(messageBuilder.build, address);
		  }
		case _ =>
	  }
	}
  }
  
  private def endGossip(message : Message) ={
	if (message.getContentClass.equals(classOf[VersionedModel].getName)) {
	  var versionedModel = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VersionedModel])
	  var uuid = versionedModel.getUuid
	  var vectorClock = versionedModel.getVector
	  var data = RichString(versionedModel.getModel.toStringUtf8).fromJSON(classOf[org.kevoree.framework.message.Message])
	 
	  dataManager.setData(UUID.fromString(uuid), Tuple2[VectorClock,org.kevoree.framework.message.Message](vectorClock,data))
	  channelFragment.localDelivery(data) 
	  
	  // UPDATE clock
	  vectorClock.getEntiesList.find(p=> p.getNodeID == channelFragment.getNodeName) match {
		case Some(p)=> //NOOP
		case None => {
			var newenties = ClockEntry.newBuilder.setNodeID(channelFragment.getNodeName).setTimestamp(System.currentTimeMillis).setVersion(1).build
			vectorClock = VectorClock.newBuilder(vectorClock).addEnties(newenties).setTimestamp(System.currentTimeMillis).build
		  }
	  }
	  
	  var newMerged = dataManager.mergeClock(UUID.fromString(uuid), vectorClock)
	  println("msg merged ")
	  /*implicit def vectorDebug(vc : VectorClock) = VectorClockAspect(vc) 
	   newMerged.printDebug*/
    
	  //CHECK FOR GARBAGE
	  if(newMerged.getEnties(0).getNodeID.equals(channelFragment.getNodeName)){
		var allPresent = channelFragment.getAllPeers.forall(peer=>{
			newMerged.getEntiesList.exists(e=> e.getNodeID == peer && e.getVersion > 0)
		  })
		if(allPresent){
		  //THIS NODE IS MASTER ON THE MSG
		  //ALL REMOTE NODE IN MY !PRESENT! M@R has rec a copy
		  //DELETING
		  //
		  println("Garbage ="+uuid)
		  dataManager.removeData(UUID.fromString(uuid))
		}
	  }
	}
  }
}
