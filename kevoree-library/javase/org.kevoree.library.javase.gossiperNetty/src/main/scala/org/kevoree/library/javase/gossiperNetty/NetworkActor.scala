package org.kevoree.library.javase.gossiperNetty

import actors.DaemonActor
import java.net.InetSocketAddress
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message
import org.jboss.netty.channel.Channel

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:44
 */

abstract class NetworkActor extends DaemonActor {

  case class STOP()
  case class SEND_MESSAGE(o: Message, address: InetSocketAddress)
  case class SEND_MESSAGE_TO_CHANNEL(o : Message, channel : Channel, address : InetSocketAddress)
  
  def stop() {
    this !? STOP()
  }

  def sendMessage(o: Message, address: InetSocketAddress) {
    this ! SEND_MESSAGE(o, address)
  }

  def sendMessage(o : Message, channel : Channel, address : InetSocketAddress) {
    this ! SEND_MESSAGE_TO_CHANNEL(o, channel, address)
  }

  def act () {
    loop {
      react {
        case STOP() => stopInternal();reply();this.exit()
        case SEND_MESSAGE(o, address) => sendMessageInternal(o, address)
        case SEND_MESSAGE_TO_CHANNEL(o, channel, address) => sendMessageToChannelInternal(o, channel, address)

      }
    }
  }

  protected def stopInternal()
  protected def sendMessageInternal(o: Message, address: InetSocketAddress)
  protected def sendMessageToChannelInternal(o: Message, channel : Channel, address : InetSocketAddress)


}