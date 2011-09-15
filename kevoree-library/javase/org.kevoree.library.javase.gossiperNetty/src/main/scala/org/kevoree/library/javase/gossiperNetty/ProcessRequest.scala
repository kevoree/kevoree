package org.kevoree.library.javase.gossiperNetty

import actors.DaemonActor
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message
import org.jboss.netty.channel.Channel
import java.util.UUID
import com.google.protobuf.ByteString
import org.kevoree.library.gossiperNetty.protocol.gossip.Gossip._
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 14:27
 */

class ProcessRequest (instance: GossiperComponent, dataManager: DataManager, serializer: Serializer,
  process: ProcessValue, protocolSelector: NetworkProtocolSelector) extends DaemonActor {
  private val logger = LoggerFactory.getLogger(classOf[ProcessRequest])

  case class STOP ()

  case class RECEIVE_REQUEST (message: Message, channel: Channel, address: InetSocketAddress)

  def stop () {
    this ! STOP()
  }

  def receiveRequest (message: Message, channel: Channel, address: InetSocketAddress) {
    this ! RECEIVE_REQUEST(message, channel, address)
  }

  def act () {
    loop {
      react {
        case STOP() => stopInternal()
        case RECEIVE_REQUEST(message, channel, address) => receiveRequestInternal(message, channel, address)
      }
    }
  }

  def stopInternal () {
    this.exit()
  }

  def receiveRequestInternal (message: Message, channel: Channel, address: InetSocketAddress) {
    var responseBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName)
      .setDestNodeName(instance.getNodeName)
    message.getContentClass match {
      case s: String if (s == classOf[VectorClockUUIDsRequest].getName) => {
        logger.debug("VectorClockUUIDsRequest request received")
        val uuidVectorClocks = dataManager.getUUIDVectorClocks()
        logger.debug("local uuids " + uuidVectorClocks.keySet().mkString(","))
        var vectorClockUUIDsBuilder = VectorClockUUIDs.newBuilder
        uuidVectorClocks.keySet.foreach {
          uuid: UUID =>
            vectorClockUUIDsBuilder.addVectorClockUUIDs(VectorClockUUID.newBuilder.setUuid(uuid.toString)
              .setVector(uuidVectorClocks.get(uuid)).build)
            if (vectorClockUUIDsBuilder.getVectorClockUUIDsCount == 1) {
              // it is possible to increase the number of vectorClockUUID on each message
              responseBuilder = Message.newBuilder.setDestName(instance.getName).setDestNodeName(instance.getNodeName)
              val modelBytes = vectorClockUUIDsBuilder.build.toByteString
              responseBuilder.setContentClass(classOf[VectorClockUUIDs].getName).setContent(modelBytes)
              logger.debug("send vectorclock for " + uuid + " to " + message.getDestNodeName)
              protocolSelector.selectForMetaData().sendMessage(responseBuilder.build(), channel, address)
              vectorClockUUIDsBuilder = VectorClockUUIDs.newBuilder
            }
        }
        if (uuidVectorClocks.size() == 0) {
          //vectorClockUUIDsBuilder.addVectorClockUUIDs(VectorClockUUID.newBuilder.build)
          // it is possible to increase the number of vectorClockUUID on each message
          responseBuilder = Message.newBuilder.setDestName(instance.getName)
            .setDestNodeName(instance.getNodeName)
          val modelBytes = vectorClockUUIDsBuilder.build.toByteString
          responseBuilder.setContentClass(classOf[VectorClockUUIDs].getName).setContent(modelBytes)

          protocolSelector.selectForMetaData().sendMessage(responseBuilder.build(), channel, address)
        }


      }
      case s: String if (s == classOf[UUIDDataRequest].getName) => {
        logger.debug("UUIDDataRequest received")
        val uuidDataRequest = UUIDDataRequest.parseFrom(message.getContent)
        val data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))
        //        logger.debug("before serializing data")
        val bytes: Array[Byte] = serializer.serialize(data._2);
        //        logger.debug("after serializing data")
        if (bytes != null) {
          val modelBytes = ByteString.copyFrom(bytes)

          val modelBytes2 = VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1)
            .setModel(modelBytes).build.toByteString
          responseBuilder.setContentClass(classOf[VersionedModel].getName).setContent(modelBytes2)

          protocolSelector.selectForMetaData().sendMessage(responseBuilder.build(), channel, address);
        } else {
          logger.warn("Serialization failed !")
        }

      }
      case s: String if (s == classOf[UpdatedValueNotification].getName) => {
        logger.debug("notification received from " + channel.getRemoteAddress)
        process.initGossip(message.getDestNodeName)
      }
    }
  }
}