/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory
import org.jboss.netty.handler.codec.string.StringDecoder
import org.jboss.netty.handler.codec.string.StringEncoder
import org.jboss.netty.util.CharsetUtil
import scala.actors.DaemonActor
import scala.actors.TIMEOUT

class GossiperChannelClient(timeout:Long,adr:InetSocketAddress) extends SimpleChannelUpstreamHandler with DaemonActor {

  case class RETURN_MSG(content : AnyRef)
  case class CALL_MSG(content : AnyRef)
  
  /* CONSTRUCTOR */
  var factory =  new OioDatagramChannelFactory(Executors.newCachedThreadPool())
  var bootstrap = new ConnectionlessBootstrap(factory)
  var self = this
  bootstrap.setPipelineFactory(new ChannelPipelineFactory(){
      override def getPipeline() : ChannelPipeline = {
        return Channels.pipeline(
          new StringEncoder(CharsetUtil.ISO_8859_1),
          new StringDecoder(CharsetUtil.ISO_8859_1),
          self)
      }
    }
  )
  var channel = bootstrap.bind(new InetSocketAddress(0));
  this.start
  
  def call(content : AnyRef) : AnyRef = {
    (this !? CALL_MSG(content)).asInstanceOf[AnyRef]
  }
  
  
  def act{
    react {
      case CALL_MSG(content) => {
          channel.write(content, adr);reply("res")
      }
    }
    println("OK WAITING FOR TIMEOUT")
    reactWithin(timeout){
      case RETURN_MSG(e) => reply(e);exit
      case TIMEOUT => println("TIMEOUT");reply(null);channel.close().awaitUninterruptibly();exit
    }
  }
  
  override def messageReceived(ctx:ChannelHandlerContext, e:MessageEvent)={
    this ! (RETURN_MSG(e.getMessage))
    e.getChannel().close();
  }
  
  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent)={
    //NOOP
    e.getCause().printStackTrace();
    e.getChannel().close();
  }
  
  
}
