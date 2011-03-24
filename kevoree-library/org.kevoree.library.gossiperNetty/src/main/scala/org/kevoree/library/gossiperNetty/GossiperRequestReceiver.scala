/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.util.UUID
import java.util.concurrent.Executors
import com.google.protobuf.ByteString
import java.net.InetSocketAddress
import java.net.SocketAddress
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.channel.socket.DatagramChannel
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory
import org.kevoree.extra.marshalling.RichJSONObject
import org.kevoree.framework.AbstractChannelFragment
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.library.gossip.Gossip
import scala.concurrent.TIMEOUT
import scala.collection.JavaConversions._

class GossiperRequestReceiver(channelFragment : NettyGossiperChannel,dataManager : DataManager, port : Int, gossiperRequestSender : GossiperRequestSender) extends actors.DaemonActor {

  // define attributes used to define channel to listen request
  var factoryForRequest =  new OioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrapForRequest = new ConnectionlessBootstrap(factory)
  var self = this
  bootstrapForRequest.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        return Channels.pipeline(
          new ProtobufEncoder(),
          new ProtobufDecoder(Message.getDefaultInstance()),
          new GossiperRequestReceiverHandler(self))
      }
    }
  )
  bootstrapForRequest.bind(new InetSocketAddress(port));
  
  // define attibutes used to define channel and to send data when someone ask for it
  var factory =  new OioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrap = new ConnectionlessBootstrap(factory)
  //var self = this
  bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        return Channels.pipeline(
          new ProtobufEncoder(),
          new ProtobufDecoder(Message.getDefaultInstance()),
          new CloseChannelManager)
      }
    }
  )
 
  this.start
  
  
  case class Reply(message : Message, address : SocketAddress)
  case class RETURN_MSG()
  case class STOP_GOSSIPER()
  
  def stop(){
    this ! STOP_GOSSIPER()
  }
  
  def reply(message : Message, address : SocketAddress) ={
	this ! Reply(message, address)
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
	loop {
	  react {
		case STOP_GOSSIPER() => this.exit
		case Reply(message, address) => doGossip(message, address)
	  }
	}
  }
  
  private def doGossip(message : Message, address : SocketAddress) /*: Channel*/ ={
	var remoteChannelFragment : KevoreeChannelFragment= null
	channelFragment.getOtherFragments.find(c => c.getName == message.getDestChannelName) match {
	  case Some(channel) => remoteChannelFragment = channel
	  case None => 
	}
	if (remoteChannelFragment != null) {
	  var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel];
	  var responseBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
	  if (message.getContentClass.equals(classOf[Gossip.VectorClockUUIDsRequest].getName)) {
		var vectorClockUUIDsBuilder = Gossip.VectorClockUUIDs.newBuilder
		var uuidVectorClocks = dataManager.getUUIDVectorClocks
		uuidVectorClocks.keySet.foreach {uuid : UUID =>
		  vectorClockUUIDsBuilder.addVectorClockUUIDs(Gossip.VectorClockUUID.newBuilder.setUuid(uuid.toString).setVector(uuidVectorClocks.get(uuid)).build)
		}
		
		var modelBytes = vectorClockUUIDsBuilder.build.toByteString
		responseBuilder.setContentClass(classOf[Gossip.VectorClockUUIDs].getName).setContent(modelBytes)
		channel.write(responseBuilder.build, address);
	  } else if (message.getContentClass.equals(classOf[Gossip.UUIDVectorClockRequest].getName)) {
		var uuidVectorClockRequest = message.asInstanceOf[Gossip.UUIDVectorClockRequest]
		var vectorClock =dataManager.getUUIDVectorClock(UUID.fromString(uuidVectorClockRequest.getUuid))
		
		var modelBytes = Gossip.VectorClockUUID.newBuilder.setUuid(uuidVectorClockRequest.getUuid).setVector(vectorClock).build.toByteString
		responseBuilder.setContentClass(classOf[Gossip.VectorClockUUID].getName).setContent(modelBytes)
		channel.write(responseBuilder.build, address);
	  } else if (message.getContentClass.equals(classOf[Gossip.UUIDDataRequest].getName)) {
		var uuidDataRequest = message.asInstanceOf[Gossip.UUIDDataRequest]
		var data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))
		var localObjJSON = new RichJSONObject(data._2);
		var res = localObjJSON.toJSON;
		var modelBytes = ByteString.copyFromUtf8(res);
		
		modelBytes = Gossip.VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1).setModel(modelBytes).build.toByteString
		responseBuilder.setContentClass(classOf[Gossip.VectorClockUUIDs].getName).setContent(modelBytes)
		channel.write(responseBuilder.build, address);
	  } else if (message.getContentClass.equals(classOf[Gossip.UpdatedValueNotification].getName)) {
		// TODO send an event to GossiperClientActor to launch a gossip with message.getDestChannelName
		message.getDestChannelName
		channelFragment.getOtherFragments.find (fragment => fragment.getName == message.getDestChannelName) match {
		  case Some(c) => gossiperRequestSender.initGossipAction(c.getNodeName)
		  case None =>
		}
		//gossiperRequestSender.initGossipAction(peer)
	  }
	}
  }
}
