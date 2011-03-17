/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.handler.codec.string.StringDecoder
import org.jboss.netty.handler.codec.string.StringEncoder
import org.jboss.netty.util.CharsetUtil

object GossiperChannelServer extends ChannelPipelineFactory{

  var channel : Channel = null
  var pool : java.util.concurrent.ExecutorService = null
  var bootstrap :ConnectionlessBootstrap = null
  
  def startOrUpdate(port : Int) = {
    if(channel == null){
      pool = Executors.newCachedThreadPool()
      var factory = new NioDatagramChannelFactory(pool)
      bootstrap = new ConnectionlessBootstrap(factory);
      bootstrap.setPipelineFactory(this)
      channel = bootstrap.bind(new InetSocketAddress(port))
    }
  }
  
  override def getPipeline() : ChannelPipeline = {
    return Channels.pipeline(
      new StringEncoder(CharsetUtil.ISO_8859_1),
      new StringDecoder(CharsetUtil.ISO_8859_1),
      new GossiperChannelHandler());
  }
  
  def stop()={
    channel.close.awaitUninterruptibly(2000)
    bootstrap.releaseExternalResources
  }
  
}
