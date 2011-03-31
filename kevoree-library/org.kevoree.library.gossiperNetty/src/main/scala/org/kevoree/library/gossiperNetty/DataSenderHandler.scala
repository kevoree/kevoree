/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import com.google.protobuf.ByteString
import java.util.UUID
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.extra.marshalling.RichJSONObject
import org.kevoree.library.gossip.Gossip
import org.kevoree.library.gossip.Gossip.VersionedModel
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message

class DataSenderHandler(channelFragment : NettyGossiperChannel,dataManager : DataManager) extends SimpleChannelUpstreamHandler{
  
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent)={
	println("message received")
	var message = e.getMessage.asInstanceOf[Message]
	if (message.getContentClass.equals(classOf[Gossip.UUIDDataRequest].getName))  {
	  var uuidDataRequest = Gossip.UUIDDataRequest.parseFrom(message.getContent)
	  var data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))
	  var localObjJSON = new RichJSONObject(data._2);
	  var res = localObjJSON.toJSON;
	  var modelBytes = ByteString.copyFromUtf8(res);
		
	  modelBytes = Gossip.VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1).setModel(modelBytes).build.toByteString
	  var responseBuilder : Message.Builder = Message.newBuilder.setDestChannelName(message.getDestChannelName).setDestNodeName(channelFragment.getNodeName)
	  responseBuilder.setContentClass(classOf[Gossip.VersionedModel].getName).setContent(modelBytes)
	  e.getChannel.write(responseBuilder.build)
	  println("response sent")
	}
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent)={
    //NOOP
	println("Exception GossiperRequestReceiverHandler")
	e.getCause().printStackTrace();
	e.getChannel.close.awaitUninterruptibly
  }
}
