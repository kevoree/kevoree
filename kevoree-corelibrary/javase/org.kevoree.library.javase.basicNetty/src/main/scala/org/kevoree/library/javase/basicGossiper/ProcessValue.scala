package org.kevoree.library.javase.basicGossiper

import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import java.util.UUID
import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message
import org.kevoree.library.basicGossiper.protocol.gossip.Gossip._
import org.kevoree.library.basicGossiper.protocol.version.Version.{ClockEntry, VectorClock}

import scala.collection.JavaConversions._
import actors.{Actor, DaemonActor}
import com.google.protobuf.ByteString
import org.kevoree.library.javase.NetworkSender

class ProcessValue(instance: GossiperComponent, alwaysAskData: Boolean, sender: NetworkSender,
                   dataManager: DataManager, serializer: Serializer, doGarbage: Boolean) extends Actor {

  implicit def vectorDebug(vc: VectorClock) = VectorClockAspect(vc)

  private val logger = LoggerFactory.getLogger(classOf[ProcessValue])

  case class STOP()

  case class NOTIFY_PEERS()

  case class INIT_GOSSIP(peer: String)


  case class RECEIVE_REQUEST(message: Message, channel: GossiperGossiperConnection /*, address: InetSocketAddress*/)

  def stop() {
    this ! STOP()
  }

  def notifyPeers() {
    this ! NOTIFY_PEERS()
  }

  def initGossip(peer: String) {
    this ! INIT_GOSSIP(peer)
  }

  def receiveRequest(message: Message, channel: GossiperGossiperConnection /*, address: InetSocketAddress*/) {
    this ! RECEIVE_REQUEST(message, channel /*, address*/)
  }

  def act() {
    loop {
      react {
        case STOP() => stopInternal()
        case NOTIFY_PEERS() => notifyPeersInternal()
        case INIT_GOSSIP(peer) => initGossipInternal(peer)
        case RECEIVE_REQUEST(message, channel /*, address*/) => {
          val messageToReply = buildResponse(message)
          if (messageToReply != null) {
            channel.write(messageToReply.toByteArray)
          }
        }
      }
    }
  }

  private def stopInternal() {
    this.exit()
  }

  private def notifyPeersInternal() {
    instance.getAllPeers.foreach {
      peer =>
        if (!peer.equals(instance.getNodeName)) {
          logger.debug("send notification to " + peer)
          val messageBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName).setDestNodeName(instance.getNodeName)
          messageBuilder.setContentClass(classOf[UpdatedValueNotification].getName).setContent(UpdatedValueNotification.newBuilder.build.toByteString)
          val address = instance.getAddress(peer)
          //          addresses.foreach {
          //            ipAddress =>
          sender.sendMessage(messageBuilder.build, new InetSocketAddress(address, instance.parsePortNumber(peer)))
          //          }
        }
    }
  }

  private def initGossipInternal(peer: String) {
    if (peer != null && peer != "") {
      val address = instance.getAddress(peer)
      /* addresses.foreach {
         ipAddress =>*/
      val inetAddress = new InetSocketAddress(address, instance.parsePortNumber(peer))
      if (alwaysAskData) {
        dataManager.getUUIDVectorClocks.keySet().foreach {
          uuid =>
            askForData(uuid, instance.getNodeName, inetAddress)
        }
      } else {
        val messageBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName).setDestNodeName(instance.getNodeName)
        messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName).setContent(VectorClockUUIDsRequest.newBuilder.build.toByteString)
        sender.sendMessage(messageBuilder.build, inetAddress)
      }
      //      }
    }
  }

  private def processMetadataInternal(message: Message) {
    if (message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) {
      val remoteVectorClockUUIDs = VectorClockUUIDs.parseFrom(message.getContent)
      if (remoteVectorClockUUIDs != null) {
        /* check for new uuid values*/
        remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach {
          vectorClockUUID =>
            val uuid = UUID.fromString(vectorClockUUID.getUuid)

            if (dataManager.getUUIDVectorClock(uuid) == null) {
              logger.debug("add empty local vectorClock with the uuid if it is not already defined")
              dataManager.setData(uuid, Tuple2[VectorClock, Message](VectorClock.newBuilder.setTimestamp(System.currentTimeMillis).build, Message.newBuilder().buildPartial()),
                message.getDestNodeName)
            }
        }

        var uuids = List[UUID]()
        remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach {
          remoteVectorClockUUID =>
            uuids = uuids ++ List(UUID.fromString(remoteVectorClockUUID.getUuid))
        }
        dataManager.checkForGarbage(uuids, message.getDestNodeName)
      }
      //FOREACH UUIDs
      remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach {
        remoteVectorClockUUID =>

          val uuid = UUID.fromString(remoteVectorClockUUID.getUuid)
          val remoteVectorClock = remoteVectorClockUUID.getVector

          dataManager.getUUIDVectorClock(uuid).printDebug()
          remoteVectorClock.printDebug()
          val occured = VersionUtils.compare(dataManager.getUUIDVectorClock(uuid), remoteVectorClock)
          occured match {
            case Occured.AFTER => {
              logger.debug("VectorClocks comparison into GossiperRequestSender give us: AFTER")
            }
            case Occured.BEFORE => {
              logger.debug("VectorClocks comparison into GossiperRequestSender give us: BEFORE")
              val address = instance.getAddress(message.getDestNodeName)
              //              addresses.foreach {
              //                ipAddress =>
              askForData(uuid, message.getDestNodeName, new InetSocketAddress(address, instance.parsePortNumber(message.getDestNodeName)))
              //              }
            }
            case Occured.CONCURRENTLY => {
              logger.debug("VectorClocks comparison into GossiperRequestSender give us: CONCURRENTLY")
              val address = instance.getAddress(message.getDestNodeName)
              //              addresses.foreach {
              //                ipAddress =>
              askForData(uuid, message.getDestNodeName, new InetSocketAddress(address, instance.parsePortNumber(message.getDestNodeName)))
              //              }
            }
            case _ => logger.error("unexpected match into initSecondStep")
          }
      }
    }
  }

  private def endGossipInternal(message: Message) {
    if (message.getContentClass.equals(classOf[VersionedModel].getName)) {
      val versionedModel = VersionedModel.parseFrom(message.getContent)
      val uuid = versionedModel.getUuid
      var vectorClock = versionedModel.getVector

      val data = serializer.deserialize(versionedModel.getModel.toByteArray)

      if (data != null) {
        var sendOnLocal = false
        if (dataManager.getData(UUID.fromString(uuid)) == null) {
          sendOnLocal = true
        }
        if (dataManager.setData(UUID.fromString(uuid), (vectorClock, data), message.getDestNodeName)) {
          if (sendOnLocal) {
            instance.localNotification(data)
          }

          // UPDATE clock
          vectorClock.getEntiesList.find(p => p.getNodeID == instance.getNodeName) match {
            case Some(p) => //NOOP
            case None => {
              logger.debug("add entries for the local node.")
              val newenties = ClockEntry.newBuilder.setNodeID(instance.getNodeName) /*.setTimestamp(System.currentTimeMillis)*/ .setVersion(1).build
              vectorClock = VectorClock.newBuilder(vectorClock).addEnties(newenties).setTimestamp(System.currentTimeMillis).build
            }
          }
        }
      }
    }
  }

  private def askForData(uuid: UUID, remoteNodeName: String, address: InetSocketAddress) {
    val messageBuilder: Message.Builder = Message.newBuilder
      .setDestName(instance.getName).setDestNodeName(instance.getNodeName)
      .setContentClass(classOf[UUIDDataRequest].getName)
      .setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
    sender.sendMessage(messageBuilder.build, address)
  }


  private def buildResponse(message: Message): Message = {
    var responseBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName)
      .setDestNodeName(instance.getNodeName)
    message.getContentClass match {
      case s: String if (message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) => {
        processMetadataInternal(message)
        null
      }
      case s: String if (message.getContentClass.equals(classOf[VersionedModel].getName)) => {
        endGossipInternal(message)
        null
      }
      case s: String if (s == classOf[VectorClockUUIDsRequest].getName) => {
        logger.debug("VectorClockUUIDsRequest request received")
        val uuidVectorClocks = dataManager.getUUIDVectorClocks
        logger.debug("local uuids " + uuidVectorClocks.keySet().mkString(","))
        var vectorClockUUIDsBuilder = VectorClockUUIDs.newBuilder
        var resultMessage: Message = null
        uuidVectorClocks.keySet.foreach {
          uuid: UUID =>
            vectorClockUUIDsBuilder.addVectorClockUUIDs(VectorClockUUID.newBuilder.setUuid(uuid.toString).setVector(uuidVectorClocks.get(uuid)).build)
            if (vectorClockUUIDsBuilder.getVectorClockUUIDsCount == 1) {
              // it is possible to increase the number of vectorClockUUID on each message
              responseBuilder = Message.newBuilder.setDestName(instance.getName).setDestNodeName(instance.getNodeName)
              val modelBytes = vectorClockUUIDsBuilder.build.toByteString
              responseBuilder.setContentClass(classOf[VectorClockUUIDs].getName).setContent(modelBytes)
              logger.debug("send vectorclock for " + uuid + " to " + message.getDestNodeName)
              resultMessage = responseBuilder.build()
              vectorClockUUIDsBuilder = VectorClockUUIDs.newBuilder
            }
        }
        if (uuidVectorClocks.size() == 0) {
          //vectorClockUUIDsBuilder.addVectorClockUUIDs(VectorClockUUID.newBuilder.build)
          // it is possible to increase the number of vectorClockUUID on each message
          responseBuilder = Message.newBuilder.setDestName(instance.getName).setDestNodeName(instance.getNodeName)
          val modelBytes = vectorClockUUIDsBuilder.build.toByteString
          responseBuilder.setContentClass(classOf[VectorClockUUIDs].getName).setContent(modelBytes)
          resultMessage = responseBuilder.build()
        }
        resultMessage
      }
      case s: String if (s == classOf[UUIDDataRequest].getName) => {
        logger.debug("UUIDDataRequest received")
        val uuidDataRequest = UUIDDataRequest.parseFrom(message.getContent)
        val data = dataManager.getData(UUID.fromString(uuidDataRequest.getUuid))
        logger.debug("before serializing data : {}", data)
        val bytes: Array[Byte] = serializer.serialize(data._2)
        logger.debug("after serializing data")
        if (bytes != null) {
          val modelBytes = ByteString.copyFrom(bytes)
          val modelBytes2 = VersionedModel.newBuilder.setUuid(uuidDataRequest.getUuid).setVector(data._1).setModel(modelBytes).build.toByteString
          responseBuilder.setContentClass(classOf[VersionedModel].getName).setContent(modelBytes2)
          responseBuilder.build()
        } else {
          logger.warn("Serialization failed !")
          null
        }

      }
      case s: String if (s == classOf[UpdatedValueNotification].getName) => {
        logger.debug("notification received from " + message.getDestNodeName)
        initGossip(message.getDestNodeName)
        null
      }
    }
  }


}