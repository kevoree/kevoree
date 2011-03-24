/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.kevoree.library.gossip.Gossip.VectorClockUUID
import org.kevoree.library.gossip.Gossip.VectorClockUUIDs
import org.kevoree.library.gossip.Gossip.VersionedModel
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message

class GossiperRequestSenderHandler(gossiperRequestSender : GossiperRequestSender) extends SimpleChannelUpstreamHandler {
  
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent)={
	var message = e.getMessage.asInstanceOf[Message]
	if (message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) {
	  //var vectorClockUUIDs = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VectorClockUUIDs])
	  gossiperRequestSender.initSecondStepAction(message, e.getRemoteAddress, e.getChannel)
	} else if (message.getContentClass.equals(classOf[VectorClockUUID].getName)) {
	  //var vectorClockUUID = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VectorClockUUID])
	  gossiperRequestSender.initLastStepAction(message, e.getRemoteAddress, e.getChannel)
	} else if (message.getContentClass.equals(classOf[VersionedModel].getName)) {
	  //var versionModel = RichString(message.getContent.toStringUtf8).fromJSON(classOf[VersionedModel])
	  gossiperRequestSender.endGossipAction(message)
	  e.getChannel.close
	}
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent)={
    //NOOP
	e.getCause().printStackTrace();
	e.getChannel.close
  }
}
