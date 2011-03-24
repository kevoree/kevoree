/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
import org.kevoree.library.gossip.Gossip.UpdatedValueNotification
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import scala.collection.JavaConversions._

class NotificationRequestSender(channelFragment : NettyGossiperChannel, port : Int) extends actors.DaemonActor {

  
  // define attributes used to define channel to send notification message
  var factoryNotificationMessage =  new NioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrapNotificationMessage = new ConnectionlessBootstrap(factoryNotificationMessage)
  bootstrapNotificationMessage.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        return Channels.pipeline(
          new ProtobufEncoder(),
          new ProtobufDecoder(Message.getDefaultInstance()),
          new CloseChannelManager)
      }
    }
  )
  
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
	react {
	  //reactWithin(timeout.longValue){
	  case STOP_GOSSIPER() => {
		  channels.foreach{channel=>
			channel.close
		  }
		  this.exit
		}
	  case NOTIFY_PEERS() => {
		  channels.foreach{channel=>
			channel.close
		  }
		  channels = List[org.jboss.netty.channel.socket.DatagramChannel]()
		  doNotifyPeers()
		}
	}
  }
  
  private var channels = List[org.jboss.netty.channel.socket.DatagramChannel]()
  
  
  private def doNotifyPeers() ={
	channelFragment.getAllPeers.foreach { fragment =>
	  
	  var channel = bootstrapNotificationMessage.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
	  var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
	  messageBuilder.setContentClass(classOf[UpdatedValueNotification].getName).setContent(UpdatedValueNotification.newBuilder.build.toByteString)
	
	  channel.write(messageBuilder.build, new InetSocketAddress(channelFragment.getAddress(fragment), port));
	  channels = channels ++ List(channel)
	}
	
  }
}
