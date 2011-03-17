/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler

class GossiperChannelHandler extends SimpleChannelUpstreamHandler {
  

  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent)={
    println("Hello "+e.getMessage)
    e.getChannel.write("result")
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent)={
    //NOOP
  }
  
}
