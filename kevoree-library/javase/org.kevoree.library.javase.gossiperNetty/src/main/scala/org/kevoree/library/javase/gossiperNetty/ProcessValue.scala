package org.kevoree.library.javase.gossiperNetty

import actors.DaemonActor
import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import java.util.UUID
import org.kevoree.library.gossiperNetty.protocol.message.KevoreeMessage.Message
import org.kevoree.library.gossiperNetty.protocol.gossip.Gossip._
import org.kevoree.library.gossiperNetty.protocol.version.Version.{ClockEntry, VectorClock}

import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:24
 */

class ProcessValue (instance: GossiperComponent, alwaysAskData: Boolean, protocolSelector: NetworkProtocolSelector,
  dataManager: DataManager, serializer: Serializer, doGarbage: Boolean) extends DaemonActor {


  implicit def vectorDebug (vc: VectorClock) = VectorClockAspect(vc)

  private val logger = LoggerFactory.getLogger(classOf[ProcessValue])

  case class STOP ()

  case class NOTIFY_PEERS ()

  case class INIT_GOSSIP (peer: String)

  case class RECEIVE_VALUE (message: Message)

  /*case class PROCESS_METADATA (message: Message)

  case class END_GOSSIP (message: Message)*/

  def stop () {
    this ! STOP()
  }

  def notifyPeers () {
    this ! NOTIFY_PEERS()
  }

  def initGossip (peer: String) {
    this ! INIT_GOSSIP(peer)
  }

  def receiveValue (message: Message) {
    this ! RECEIVE_VALUE(message)
  }

  def act () {
    loop {
      react {
        case STOP() => stopInternal()
        case NOTIFY_PEERS() => notifyPeersInternal()
        case INIT_GOSSIP(peer) => initGossipInternal(peer)
        case RECEIVE_VALUE(message) => receiveValueInternal(message)
      }
    }
  }

  private def stopInternal () {
    this.exit()
  }

  private def notifyPeersInternal () {
    instance.getAllPeers.foreach {
      peer =>
        if (!peer.equals(instance.getNodeName)) {
          logger.debug("send notification to " + peer)
          val messageBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName)
            .setDestNodeName(instance.getNodeName)
          messageBuilder.setContentClass(classOf[UpdatedValueNotification].getName)
            .setContent(UpdatedValueNotification.newBuilder.build.toByteString)
          protocolSelector.selectForMetaData().sendMessage(messageBuilder.build, new
              InetSocketAddress(instance.getAddress(peer), instance.parsePortNumber(peer)))
        }
    }
  }

  private def initGossipInternal (peer: String) {
    if (peer != null && peer != "") {
      val address = new
          InetSocketAddress(instance.getAddress(peer), instance.parsePortNumber(peer))
      if (alwaysAskData) {
        dataManager.getUUIDVectorClocks().keySet().foreach {
          uuid =>
            askForData(uuid, instance.getNodeName, address)
        }
      } else {
        val messageBuilder: Message.Builder = Message.newBuilder.setDestName(instance.getName)
          .setDestNodeName(instance.getNodeName)
        messageBuilder.setContentClass(classOf[VectorClockUUIDsRequest].getName)
          .setContent(VectorClockUUIDsRequest.newBuilder.build.toByteString)
        protocolSelector.selectForMetaData().sendMessage(messageBuilder.build, address)
      }
    }
  }

  private def receiveValueInternal (message: Message) {
    message.getContentClass match {
      case s: String if (message.getContentClass.equals(classOf[VectorClockUUIDs].getName)) => {
        processMetadataInternal(message)
      }
      case s: String if (message.getContentClass.equals(classOf[VersionedModel].getName)) => {
        endGossipInternal(message)
      }
    }
  }

  private def processMetadataInternal (message: Message) {
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
              askForData(uuid, message.getDestNodeName, new
                  InetSocketAddress(instance.getAddress(message.getDestNodeName),
                                     instance.parsePortNumber(message.getDestNodeName)))
            }
            case Occured.CONCURRENTLY => {
              logger.debug("VectorClocks comparison into GossiperRequestSender give us: CONCURRENTLY")
              askForData(uuid, message.getDestNodeName, new
                  InetSocketAddress(instance.getAddress(message.getDestNodeName),
                                     instance.parsePortNumber(message.getDestNodeName)))
            }
            case _ => logger.error("unexpected match into initSecondStep")
          }
      }
    }
  }

  private def endGossipInternal (message: Message) {
    if (message.getContentClass.equals(classOf[VersionedModel].getName)) {
      val versionedModel = VersionedModel.parseFrom(message.getContent)
      val uuid = versionedModel.getUuid
      var vectorClock = versionedModel.getVector

      val data = serializer.deserialize(versionedModel.getModel.toByteArray)

      if (data != null) {
        if (dataManager.setData(UUID.fromString(uuid), (vectorClock, data), message.getDestNodeName)) {
          instance.localNotification(data)

          // UPDATE clock
          vectorClock.getEntiesList.find(p => p.getNodeID == instance.getNodeName) match {
            case Some(p) => //NOOP
            case None => {
              logger.debug("add entries for the local node.")
              val newenties = ClockEntry.newBuilder.setNodeID(instance.getNodeName)
                /*.setTimestamp(System.currentTimeMillis)*/ .setVersion(1).build
              vectorClock = VectorClock.newBuilder(vectorClock).addEnties(newenties)
                .setTimestamp(System.currentTimeMillis).build
            }
          }
        }
        //        newMerged.printDebug()

        /*//CHECK FOR GARBAGE
        if (doGarbage) {
          if (newMerged.getEnties(0).getNodeID.equals(instance.getNodeName)) {
            val allPresent = instance.getAllPeers.forall(peer => {
              newMerged.getEntiesList.exists(e => e.getNodeID == peer && e.getVersion > 0)
            })
            if (allPresent) {
              //THIS NODE IS MASTER ON THE MSG
              //ALL REMOTE NODE IN MY !PRESENT! M@R has rec a copy
              //DELETING
              //
              logger.debug("Garbage =" + uuid)
              dataManager.removeData(UUID.fromString(uuid))
            }
          }
        }*/
      }
    }
  }

  private def askForData (uuid: UUID, remoteNodeName: String, address: InetSocketAddress) {
    val messageBuilder: Message.Builder = Message.newBuilder
      .setDestName(instance.getName).setDestNodeName(instance.getNodeName)
      .setContentClass(classOf[UUIDDataRequest].getName)
      .setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)

    protocolSelector.selectForData().sendMessage(messageBuilder.build, address)
  }
}