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
import org.kevoree.extra.marshalling.RichString
import org.kevoree.library.gossip.Gossip.UUIDDataRequest
import org.kevoree.library.gossip.Gossip.VectorClockUUIDs
import org.kevoree.library.gossip.Gossip.VectorClockUUIDsRequest
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.kevoree.library.version.Version.ClockEntry
import org.kevoree.library.version.Version.VectorClock
import org.kevoree.library.gossip.Gossip.VersionedModel
import org.kevoree.library.gossiper.version.Occured

import scala.collection.JavaConversions._
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.string.{StringDecoder, StringEncoder}

class GossiperRequestSender[T](timeout : java.lang.Long,channelFragment : NettyGossipAbstractElement,dataManager : DataManager[T], fullUDP : java.lang.Boolean,garbage : Boolean,clazz : Class[_]) extends actors.DaemonActor {

  // define attributes used to define channel to send gossip request
  var factory =  new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrap = new ConnectionlessBootstrap(factory)
  var self = this
  bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        val p : ChannelPipeline = Channels.pipeline()
		//p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
		//p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
		//p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
		//p.addLast("protobufEncoder", new ProtobufEncoder())
			p.addLast("StringEncoder", new StringEncoder(CharsetUtil.UTF_8))
			p.addLast("StringDecoder", new StringDecoder(CharsetUtil.UTF_8))
		p.addLast("handler", new GossiperRequestSenderHandler(self))
		return p
      }
    }
  )
  private var channel : Channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
  
  private var askForDataTCPActor = new AskForDataTCPActor(channelFragment, self)
  
  this.start
  
  private var peerName : String = null
  
  /* PUBLIC PART */
  case class STOP_GOSSIPER()
  case class INIT_GOSSIP(peer : String)
  case class INIT_SECOND_STEP(message : Message, address : SocketAddress/*, channel : Channel*/)
  //case class INIT_LAST_STEP(message : Message, address : SocketAddress, channel : Channel)
  case class END_GOSSIP(message : Message)
  
  def stop(){
    this ! STOP_GOSSIPER()
  }
  
  def initGossipAction(peer : String) ={
	this ! INIT_GOSSIP(peer)
  }
  
  def initSecondStepAction(message : Message, address : SocketAddress/*, channel : Channel*/) ={
	this ! INIT_SECOND_STEP(message, address/*,channel*/)
  }
  
  /*def initLastStepAction(message : Message, address : SocketAddress, channel : Channel) ={
   this ! INIT_LAST_STEP(message, address,channel)
   }*/
  
  def endGossipAction(message : Message) ={
	this ! END_GOSSIP(message)
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
	loop {
	  react {
		//reactWithin(timeout.longValue){
		case STOP_GOSSIPER() => {
			//println("stop gossiper")
			askForDataTCPActor.stop
			channel.close.awaitUninterruptibly
			bootstrap.releaseExternalResources
			this.exit
		  }
		case INIT_GOSSIP(peer) => {
			/*if (channel != null) { 
			 channel.close.awaitUninterruptibly
			 }*/
			initGossip(peer)
		  }
		case INIT_SECOND_STEP(message, address/*,channel*/) => initSecondStep(message, address/*,channel*/)
		case END_GOSSIP(message) => endGossip(message)
	  }
	}
	  
  }
  
  private def initGossip(peer : String) ={
	/*var channelFuture = bootstrap.connect(new InetSocketAddress(channelFragment.getAddress(peer), channelFragment.parsePortNumber(peer))).asInstanceOf[ChannelFuture]
	 channel = channelFuture.awaitUninterruptibly().getChannel()*/
	
	if (peer != null && peer != "") {
	  val messageBuilder : Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
	  messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName).setContent(VectorClockUUIDsRequest.newBuilder.build.toByteString)
	  //println("initGossip = " + channelFragment.getAddress(peer) + ":" + channelFragment.parsePortNumber(peer))
	  channel.write(messageBuilder.build.toByteString.toStringUtf8,new InetSocketAddress(channelFragment.getAddress(peer), channelFragment.parsePortNumber(peer)));
	  //println("initGossip write")
	 
	} 
  }
  
  private def initSecondStep(message : Message, address : SocketAddress/*, removeChannel : Channel*/) ={
	//println(message.getContentClass)
	if (message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) {
	 
	  val remoteVectorClockUUIDs = VectorClockUUIDs.parseFrom(message.getContent)
	  if(remoteVectorClockUUIDs!=null){
		/* check for new uuid values*/
		remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach{vectorClockUUID=>
		  val uuid = UUID.fromString(vectorClockUUID.getUuid)
		  if(dataManager.getUUIDVectorClock(uuid)==null){
			println("add empty local vectorClock with the uuid if it is not already defined")
			dataManager.setData(uuid, 
								Tuple2[VectorClock,T](VectorClock.newBuilder.setTimestamp(System.currentTimeMillis).build,null.asInstanceOf[T]))
		  } 
		}
		if (garbage) {
		  /* check for deleted uuid values */
		  val localUUIDs = dataManager.getUUIDVectorClocks
		  localUUIDs.keySet.foreach{key=>
			if (!remoteVectorClockUUIDs.getVectorClockUUIDsList.contains(key)) {
			  if(dataManager.getUUIDVectorClock(key).getEntiesList.exists(e=> e.getNodeID == message.getDestName)){
				//ALREADY SEEN VECTOR CLOCK - GARBAGE IT
				println("ALREADY SEEN VECTOR CLOCK - GARBAGE IT")
				dataManager.removeData(key)
			  } /*else {
				 //NOOP - UNCOMPLETE VECTOR CLOCK
				 }*/
			}	 
		  }
		}
	  }
	  
	  //FOREACH UUIDs
	  remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach {
		remoteVectorClockUUID =>
		
		val uuid = UUID.fromString(remoteVectorClockUUID.getUuid)
		val remoteVectorClock = remoteVectorClockUUID.getVector
		val occured = VersionUtils.compare(dataManager.getUUIDVectorClock(uuid), remoteVectorClock)
		occured match {
		  case Occured.AFTER=> {}
		  case Occured.BEFORE=> {
			  //updateValue(message.getDestChannelName,uuid,remoteVectorClock)
			  //var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
			  askForData(uuid,message.getDestNodeName,address)
			  /*var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
			   messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
			   channel.write(messageBuilder.build, address);*/
			 
			   //println("initSecondStep write")
			   }
			   case Occured.CONCURRENTLY=> {
					//updateValue(message.getDestChannelName,uuid,remoteVectorClock)
					//var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
					askForData(uuid,message.getDestNodeName,address)
					/*var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
					 messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
					 channel.write(messageBuilder.build, address);*/
					//println("initSecondStep write")
				  }
				case _ => println("unexpected match into initSecondStep")
			  }
			}
		} 
	  }
  
	  private def askForData(uuid : UUID, remoteNodeName : String, address : SocketAddress) ={
		val messageBuilder : Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
		messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
		if (fullUDP.booleanValue) {
		  channel.write(messageBuilder.build, address)
		} else {
		  /*println("TCP sending ...")
		   // FIXME maybe we launch too many data request and channel will be destroyed before the end of the communication
		   var channelFuture = bootstrapTCP.connect(new InetSocketAddress(channelFragment.getAddress(remoteNodeName),channelFragment.parsePortNumber(remoteNodeName))).asInstanceOf[ChannelFuture]
		   var channel = channelFuture.awaitUninterruptibly.getChannel
		   if (!channelFuture.isSuccess()) {
		   channelFuture.getCause().printStackTrace
		   bootstrapTCP.releaseExternalResources
		   } else {
		   var future = channel.write(messageBuilder.build) 
		   //future.awaitUninterruptibly
		   //channel.getCloseFuture.awaitUninterruptibly
		   //future.addListener(ChannelFutureListener.CLOSE)
		   //channel.close.awaitUninterruptibly
		   println("TCP sent")
		   }*/
		  askForDataTCPActor.askForDataAction(uuid,remoteNodeName)
		}
	  }
  
	  private def endGossip(message : Message) ={
		println("endGossip")
		if (message.getContentClass.equals(classOf[VersionedModel].getName)) {
		  println("VersionModel")
		  val versionedModel = VersionedModel.parseFrom(message.getContent)
		  val uuid = versionedModel.getUuid
		  var vectorClock = versionedModel.getVector
		  val data : T = RichString(versionedModel.getModel.toStringUtf8).fromJSON(clazz).asInstanceOf[T]
	 
		  dataManager.setData(UUID.fromString(uuid), Tuple2[VectorClock,T](vectorClock,data))
		  channelFragment.localNotification(data) 
	  
		  // UPDATE clock
		  vectorClock.getEntiesList.find(p=> p.getNodeID == channelFragment.getNodeName) match {
			case Some(p)=> //NOOP
			case None => {
				val newenties = ClockEntry.newBuilder.setNodeID(channelFragment.getNodeName).setTimestamp(System.currentTimeMillis).setVersion(1).build
				vectorClock = VectorClock.newBuilder(vectorClock).addEnties(newenties).setTimestamp(System.currentTimeMillis).build
			  }
		  }
	  
		  val newMerged = dataManager.mergeClock(UUID.fromString(uuid), vectorClock)
    
		  //CHECK FOR GARBAGE
		  if (garbage) {
			if(newMerged.getEnties(0).getNodeID.equals(channelFragment.getNodeName)){
			  val allPresent = channelFragment.getAllPeers.forall(peer=>{
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
		//channel.close.awaitUninterruptibly
	  }
	}
