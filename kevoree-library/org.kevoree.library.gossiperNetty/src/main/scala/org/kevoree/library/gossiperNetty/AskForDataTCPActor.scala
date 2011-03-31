/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder
import org.kevoree.library.gossip.Gossip.UUIDDataRequest
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import scala.collection.JavaConversions._

class AskForDataTCPActor(channelFragment : NettyGossiperChannel, requestSender : GossiperRequestSender) extends actors.DaemonActor {
  var factoryTCP =  new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool())
  var bootstrapTCP = new ClientBootstrap(factoryTCP)
  bootstrapTCP.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        var p : ChannelPipeline = Channels.pipeline()
		p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
		p.addLast("protobufEncoder", new ProtobufEncoder())
		p.addLast("handler", new DataReceiverHandler(requestSender))
		return p
      }
    }
  )
  bootstrapTCP.setOption("tcpNoDelay", true)
  
  private var channelGroup = new DefaultChannelGroup
  
  this.start
  
  /* PUBLIC PART */
  case class STOP()
  case class ASK_FOR_DATA(uuid : UUID, remoteNodeName : String)
  
  def stop(){
    this ! STOP()
  }
  
  def askForDataAction(uuid : UUID, remoteNodeName : String) ={
	this ! ASK_FOR_DATA(uuid, remoteNodeName)
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
	loop {
	  react {
		//reactWithin(timeout.longValue){
		case STOP => {
			channelGroup.close.awaitUninterruptibly
			//println("stop gossiper")
			//channel.close.awaitUninterruptibly
			//bootstrap.releaseExternalResources
			this.exit
		  }
		case ASK_FOR_DATA(uuid, remoteNodeName) => {
			if (channelGroup.size > 10) {
			  println("too many opened channels : " + channelGroup.size)
			  closeUnusedChannels
			  println("some channels may be closed : " + channelGroup.size)
			}
			askForData(uuid, remoteNodeName)
		  }
	  }
	}
	  
  }
  
  def closeUnusedChannels() ={
	channelGroup.foreach{
	  channel : Channel =>
	  println("channel must be closed")
	  channel.close.awaitUninterruptibly
	  println("channel are closed")
	}
  }
  
  
  
  def askForData(uuid : UUID, remoteNodeName : String) ={
	var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
	messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString) 
	println("TCP sending ...")
	// FIXME maybe we launch too many data request and channel will be destroyed before the end of the communication
	var channelFuture = bootstrapTCP.connect(new InetSocketAddress(channelFragment.getAddress(remoteNodeName),channelFragment.parsePortNumber(remoteNodeName))).asInstanceOf[ChannelFuture]
	var channel = channelFuture.awaitUninterruptibly.getChannel
	if (!channelFuture.isSuccess()) {
	  channelFuture.getCause().printStackTrace
	  bootstrapTCP.releaseExternalResources
	} else {
	  /*var future = */channel.write(messageBuilder.build) 
	  //future.awaitUninterruptibly
	  //channel.getCloseFuture.awaitUninterruptibly
	  channelGroup.add(channel)
	  //future.addListener(ChannelFutureListener.CLOSE)
	  //channel.close.awaitUninterruptibly
	  println("TCP sent")
	}
  }
}
