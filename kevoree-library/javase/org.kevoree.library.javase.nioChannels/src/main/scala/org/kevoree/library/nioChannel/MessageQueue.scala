package org.kevoree.library.nioChannel

import actors.{DaemonActor, Actor}
import org.kevoree.framework.message.Message
import org.jboss.netty.channel.Channel
import org.jboss.netty.bootstrap.ClientBootstrap
import java.net.InetSocketAddress


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */

class MessageQueue(bootstrap: ClientBootstrap) extends DaemonActor {

  case class PUT_MESSAGE(host: String, port: Int, msg: Message)

  case class PUT_CHANNEL(host: String, port: Int, channel: Channel)

  case class INVALID_CHANNEL(host: String, port: Int)
  
  case class FLUSH_CHANNEL()

  case class STOP()

  def stop() = {
    this ! STOP()
  }
  
  def flushChannel() = { this ! FLUSH_CHANNEL()  }

  def putChannel(host: String, port: Int, channel: Channel) = {
    this ! PUT_CHANNEL(host, port, channel)
  }

  def invalidChannel(host: String, port: Int) = {
    this ! INVALID_CHANNEL(host, port)
  }

  def putMsg(host: String, port: Int, msg: Message) = {
    this ! PUT_MESSAGE(host, port, msg)
  }

  //def popMsg(host: String, port: String) : Message = { (this !? GET_MESSAGE(host,port)).asInstanceOf[Message]   }

  private val cache = new scala.collection.mutable.HashMap[String, List[Message]]()

  private val cacheChannel = new scala.collection.mutable.HashMap[String, Channel]()
  private val cacheChannelActor = new scala.collection.mutable.HashMap[String, MessageListener]()

  private def buildKey(host: String, port: Int): String = {
    host + ":" + port
  }

  def act() {
    loop {
      react {
        case FLUSH_CHANNEL()=> {
          cacheChannelActor.foreach{ t =>
            t._2.stopProcess
          }
          cacheChannelActor.clear()
          cacheChannel.foreach{ t =>
            t._2.close()//.awaitUninterruptibly(200)
          }
          cacheChannel.clear()
        }
        case STOP() => exit()
        case INVALID_CHANNEL(host, port) => {
          if (cacheChannel.contains(buildKey(host, port))) {
            cacheChannel.remove(buildKey(host, port))
            cacheChannelActor.get(buildKey(host, port)).get.stopProcess
            cacheChannelActor.remove(buildKey(host, port))
          }
        }
        case PUT_CHANNEL(host, port, channel) => {
          cacheChannel.put(buildKey(host, port), channel)
          val localActor = new MessageListener(host, port, channel, this)
          localActor.start()
          cacheChannelActor.put(buildKey(host, port), localActor)
          val msgL = cache.get(buildKey(host, port)).getOrElse(List())
          if (!msgL.isEmpty) {
            msgL.foreach {
              msg =>
                localActor ! msg
            }
            cache.put(buildKey(host, port), List())
          }
        }
        case PUT_MESSAGE(host, port, msg) => {
          if (cacheChannel.get(buildKey(host, port)).isEmpty) {
            val msgL = cache.get(buildKey(host, port)).getOrElse(List())
            cache.put(buildKey(host, port), msgL ++ List(msg))
            bootstrap.connect(new InetSocketAddress(host, port))
          } else {
            cacheChannelActor.get(buildKey(host, port)).get ! msg
          }
        }
      }
    }


  }
}