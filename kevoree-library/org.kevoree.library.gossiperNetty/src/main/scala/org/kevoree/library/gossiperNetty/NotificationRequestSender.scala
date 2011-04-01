package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.DatagramChannel
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import org.kevoree.library.gossip.Gossip.UpdatedValueNotification
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import scala.collection.JavaConversions._

class NotificationRequestSender(channelFragment : NettyGossipAbstractElement) extends actors.DaemonActor {

  
  // define attributes used to define channel to send notification message
  var factoryNotificationMessage =  new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrapNotificationMessage = new ConnectionlessBootstrap(factoryNotificationMessage)
  bootstrapNotificationMessage.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        var p : ChannelPipeline = Channels.pipeline()
		//p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
		p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()))
		//p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
		p.addLast("protobufEncoder", new ProtobufEncoder())
		return p
      }
    }
  )
  
  //private var channels : ChannelGroup = new DefaultChannelGroup
  private var channel = bootstrapNotificationMessage.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
  
  this.start
  
  /* PUBLIC PART */
  case class STOP_GOSSIPER()
  case class NOTIFY_PEERS()
  
  def stop(){
    this ! STOP_GOSSIPER()
  }
  
  def notifyPeersAction() ={
	this ! NOTIFY_PEERS()
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
	loop {
	  react {
		//reactWithin(timeout.longValue){
		case STOP_GOSSIPER() => {
			//println("stop gossiper")
			channel.close.awaitUninterruptibly
			bootstrapNotificationMessage.releaseExternalResources
			this.exit
		  }
		case NOTIFY_PEERS() => {
			//channels.close.awaitUninterruptibly
			doNotifyPeers()
		  }
	  }
	}
  }
  
  
  private def doNotifyPeers() ={
	channelFragment.getAllPeers.foreach { peer =>
	  var messageBuilder : Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
	  messageBuilder.setContentClass(classOf[UpdatedValueNotification].getName).setContent(UpdatedValueNotification.newBuilder.build.toByteString)
	  //println("sending notification ...")
	  //println(channelFragment.getAddress(peer) + ":" + channelFragment.parsePortNumber(peer))
	  channel.write(messageBuilder.build, new InetSocketAddress(channelFragment.getAddress(peer), channelFragment.parsePortNumber(peer)));
	  //println("notification send ...")
	  //channels.add(channel)
	}
	
  }
}
