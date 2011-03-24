/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler

class CloseChannelManager extends SimpleChannelUpstreamHandler {
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent)={
    e.getChannel().close();
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent)={
    //NOOP
    e.getCause().printStackTrace();
    e.getChannel().close();
  }
}
