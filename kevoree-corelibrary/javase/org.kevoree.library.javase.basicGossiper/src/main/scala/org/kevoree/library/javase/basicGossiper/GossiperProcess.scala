package org.kevoree.library.javase.basicGossiper

import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import java.util.UUID
import org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message
import org.kevoree.library.basicGossiper.protocol.gossip.Gossip._
import org.kevoree.library.basicGossiper.protocol.version.Version.{ClockEntry, VectorClock}

import scala.collection.JavaConversions._
import actors.{Actor}
import com.google.protobuf.ByteString
import org.kevoree.library.javase.NetworkSender
import scala.Some
import scala.Tuple2

class GossiperProcess(instance: GossiperComponent, alwaysAskData: Boolean, sender: NetworkSender,
                      dataManager: DataManager, serializer: Serializer, doGarbage: Boolean) extends Actor {

  implicit def vectorDebug(vc: VectorClock) = VectorClockAspect(vc)

  private val logger = LoggerFactory.getLogger(classOf[GossiperProcess])

  case class STOP()

  case class NOTIFY_PEERS()

  case class INIT_GOSSIP(peer: String)

  case class RECEIVE_REQUEST(message: Message)

  def stop() {
    this ! STOP()
  }

  def notifyPeers() {
    this ! NOTIFY_PEERS()
  }

  def initGossip(peer: String) {
    this ! INIT_GOSSIP(peer)
  }

  def receiveRequest(message: Message) {
    this ! RECEIVE_REQUEST(message)
  }

  private val VersionedModelClazz = classOf[VersionedModel].getName
  private val VectorClockUUIDsClazz = classOf[VectorClockUUIDs].getName
  private val UpdatedValueNotificationClazz = classOf[UpdatedValueNotification].getName
  private val UUIDDataRequestClazz = classOf[UUIDDataRequest].getName
  private val VectorClockUUIDsRequestClazz = classOf[VectorClockUUIDsRequest].getName

  def buildAddr(message: Message): InetSocketAddress = {
    val ip = instance.getAddress(message.getDestNodeName)
    new InetSocketAddress(ip, instance.parsePortNumber(message.getDestNodeName))
  }

  def act() {
    loop {
      react {
        case STOP() => stopInternal()
        case NOTIFY_PEERS() => notifyPeersInternal()
        case INIT_GOSSIP(peer) => {
          val inetAddress = new InetSocketAddress(instance.getAddress(peer), instance.parsePortNumber(peer))
          sender.sendMessageUnreliable(createVectorClockUUIDsRequest(), inetAddress)
        }
        case RECEIVE_REQUEST(message) => {
          message.getContentClass match {
            case VersionedModelClazz => {
              endGossipInternal(message)
            }
            case VectorClockUUIDsClazz => {
              processMetadataInternal(VectorClockUUIDs.parseFrom(message.getContent),message)
            }
            case UpdatedValueNotificationClazz => {
              logger.debug("notification received from " + message.getDestNodeName)
              initGossip(message.getDestNodeName)
            }
            case UUIDDataRequestClazz => {
              logger.debug("UUIDDataRequest received")
              sender.sendMessage(buildData(message), buildAddr(message))
            }
            case VectorClockUUIDsRequestClazz => {
              sender.sendMessage(buildVectorClockUUIDs(message), buildAddr(message))
            }
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
          sender.sendMessageUnreliable(messageBuilder.build, new InetSocketAddress(address, instance.parsePortNumber(peer)))
        }
    }
  }

  private def createVectorClockUUIDsRequest(): Message = {
    val messageBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName).setDestNodeName(instance.getNodeName)
    messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName).setContent(VectorClockUUIDsRequest.newBuilder.build.toByteString).build()
  }

  private def processMetadataInternal(remoteVectorClockUUIDs: VectorClockUUIDs,message:Message) {
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
            askForData(uuid, message.getDestNodeName, new InetSocketAddress(address, instance.parsePortNumber(message.getDestNodeName)))
          }
          case Occured.CONCURRENTLY => {
            logger.debug("VectorClocks comparison into GossiperRequestSender give us: CONCURRENTLY")
            val address = instance.getAddress(message.getDestNodeName)
            askForData(uuid, message.getDestNodeName, new InetSocketAddress(address, instance.parsePortNumber(message.getDestNodeName)))
          }
          case _ => logger.error("unexpected match into initSecondStep")
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
    sender.sendMessageUnreliable(messageBuilder.build, address)
  }

  private def buildData(message: Message): Message = {
    val responseBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName)
      .setDestNodeName(instance.getNodeName)
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


  private def buildVectorClockUUIDs(message: Message): Message = {
    var responseBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName)
      .setDestNodeName(instance.getNodeName)
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


}