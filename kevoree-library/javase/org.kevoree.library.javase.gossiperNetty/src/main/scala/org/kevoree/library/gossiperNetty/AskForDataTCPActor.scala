/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.util.UUID
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import org.jboss.netty.handler.codec.compression.{ZlibDecoder, ZlibEncoder, ZlibWrapper}
import org.jboss.netty.handler.codec.protobuf.{ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder, ProtobufEncoder}
import org.jboss.netty.channel._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.{ClientCodec, Codec, Service}
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit
import version.Gossip.{VersionedModel, UUIDDataRequest}

class AskForDataTCPActor(channelFragment: NettyGossipAbstractElement, requestSender: GossiperRequestSender) extends actors.DaemonActor {

  object ModelCodec extends Codec[Message, Message] {

    override def clientCodec = new ClientCodec[Message, Message] {
      def pipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val p = Channels.pipeline()
          p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
          p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
          p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder)
          p.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance))
          p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
          p.addLast("protobufEncoder", new ProtobufEncoder)
          p.addLast("handler", new DataReceiverHandler(requestSender))
          p
        }
      }
    }
  }

  private var logger = LoggerFactory.getLogger(classOf[AskForDataTCPActor])
  this.start()

  /* PUBLIC PART */
  case class STOP()

  case class ASK_FOR_DATA(uuid: UUID, remoteNodeName: String)

  def stop() {
    this ! STOP()
  }

  def askForDataAction(uuid: UUID, remoteNodeName: String) {
    this ! ASK_FOR_DATA(uuid, remoteNodeName)
  }

  /* PRIVATE PROCESS PART */
  def act() {
    loop {
      react {
        case STOP => {
          this.exit()
        }
        case ASK_FOR_DATA(uuid, remoteNodeName) => {
          askForData(uuid, remoteNodeName)
        }
      }
    }
  }

  def askForData(uuid: UUID, remoteNodeName: String) {

    val messageBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName).setDestNodeName(channelFragment.getNodeName)
    messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
    logger.debug("TCP sending ... :-)")

    val client: Service[Message, Message] = ClientBuilder()
      .codec(ModelCodec)
      .requestTimeout(Duration.fromTimeUnit(3000, TimeUnit.MILLISECONDS))
      .hosts(new InetSocketAddress(channelFragment.getAddress(remoteNodeName), channelFragment.parsePortNumber(remoteNodeName)))
      .hostConnectionLimit(1)
      .build()

    client(messageBuilder.build) onSuccess {
      result =>
        println("Received result asynchronously: " + result)
        if (result.getContentClass.equals(classOf[VersionedModel].getName)) {
          requestSender.endGossipAction(result)
        }

    } onFailure {
      error =>
        logger.warn("warn TCP error ",error)
    } ensure {
      // All done! Close TCP connection(s):
      client.release()
    }
  }
}
