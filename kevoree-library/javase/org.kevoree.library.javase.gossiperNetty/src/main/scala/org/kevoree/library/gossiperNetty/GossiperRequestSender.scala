package org.kevoree.library.gossiperNetty

import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.UUID
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.socket.DatagramChannel
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message

import scala.collection.JavaConversions._
import org.jboss.netty.handler.codec.protobuf.{ProtobufEncoder, ProtobufVarint32LengthFieldPrepender, ProtobufDecoder, ProtobufVarint32FrameDecoder}
import org.slf4j.LoggerFactory
import version.Gossip.{VersionedModel, UUIDDataRequest, VectorClockUUIDs, VectorClockUUIDsRequest}
import version.Version.{ClockEntry, VectorClock}
import org.jboss.netty.channel._

class GossiperRequestSender (timeout: java.lang.Long, channelFragment: NettyGossipAbstractElement,
  dataManager: DataManager, fullUDP: java.lang.Boolean, garbage: Boolean, serializer: Serializer,
  alwaysAskModel: Boolean)
  extends actors.DaemonActor {

  // define attributes used to define channel to send gossip request
  var factory = new NioDatagramChannelFactory (Executors.newCachedThreadPool ())
  var bootstrap = new ConnectionlessBootstrap (factory)
  var self = this
  bootstrap.setPipelineFactory (new ChannelPipelineFactory () {
    override def getPipeline: ChannelPipeline = {
      val p: ChannelPipeline = Channels.pipeline ()
      //p.addLast("deflater", new ZlibEncoder(ZlibWrapper.ZLIB))
      //p.addLast("inflater", new ZlibDecoder(ZlibWrapper.ZLIB))
      p.addLast ("frameDecoder", new ProtobufVarint32FrameDecoder)
      p.addLast ("protobufDecoder", new ProtobufDecoder (Message.getDefaultInstance))
      p.addLast ("frameEncoder", new ProtobufVarint32LengthFieldPrepender)
      p.addLast ("protobufEncoder", new ProtobufEncoder)

      p.addLast ("handler", new GossiperRequestSenderHandler (self))
      p
    }
  }
                               )
  protected val channel: Channel = bootstrap.bind (new InetSocketAddress (0)).asInstanceOf[DatagramChannel]
  private val logger = LoggerFactory.getLogger (classOf[GossiperRequestSender])

  private val askForDataTCPActor = new AskForDataTCPActor (channelFragment, self)


  /* PUBLIC PART */
  case class STOP_GOSSIPER ()

  case class INIT_GOSSIP (peer: String)

  case class INIT_SECOND_STEP (message: Message, address: SocketAddress /*, channel : Channel*/)

  //case class INIT_LAST_STEP(message : Message, address : SocketAddress, channel : Channel)
  case class END_GOSSIP (message: Message)

  def stop () {
    this ! STOP_GOSSIPER ()
  }

  def initGossipAction (peer: String) {
    this ! INIT_GOSSIP (peer)
  }

  def initSecondStepAction (message: Message, address: SocketAddress /*, channel : Channel*/) {
    this ! INIT_SECOND_STEP (message, address /*,channel*/)
  }

  /*def initLastStepAction(message : Message, address : SocketAddress, channel : Channel) ={
         this ! INIT_LAST_STEP(message, address,channel)
         }*/

  def endGossipAction (message: Message) {
    this ! END_GOSSIP (message)
  }

  /* PRIVATE PROCESS PART */
  def act () {
    loop {
      react {
        //reactWithin(timeout.longValue){
        case STOP_GOSSIPER () => {
          askForDataTCPActor.stop ()
          //channel.close.awaitUninterruptibly // TODO do not block on actor
          channel.close ().addListener (ChannelFutureListener.CLOSE)
          bootstrap.releaseExternalResources ()
          this.exit ()
        }
        case INIT_GOSSIP (peer) => {
          initGossip (peer)
        }
        case INIT_SECOND_STEP (message, address /*,channel*/) => initSecondStep (message, address /*,channel*/)
        case END_GOSSIP (message) => endGossip (message)
      }
    }
  }

  private def initGossip (peer: String) = {
    if (peer != null && peer != "") {
      val address = new
            InetSocketAddress (channelFragment.getAddress (peer), channelFragment.parsePortNumber (peer))
      if (alwaysAskModel) {
        dataManager.getUUIDVectorClocks().keySet().foreach {
          uuid =>
            askForData (uuid, channelFragment.getNodeName, address)
        }
      } else {
        val messageBuilder: Message.Builder = Message.newBuilder.setDestName (channelFragment.getName)
          .setDestNodeName (channelFragment.getNodeName)
        messageBuilder.setContentClass (classOf[VectorClockUUIDsRequest].getName)
          .setContent (VectorClockUUIDsRequest.newBuilder.build.toByteString)
        //channel.write (messageBuilder.build, address);
        writeMessage(messageBuilder.build, address)
      }
    }
  }

  implicit def vectorDebug (vc: VectorClock) = VectorClockAspect (vc)

  private def initSecondStep (message: Message, address: SocketAddress /*, removeChannel : Channel*/) {

    if (message.getContentClass.equals (classOf[VectorClockUUIDs].getName)) {

      val remoteVectorClockUUIDs = VectorClockUUIDs.parseFrom (message.getContent)
      if (remoteVectorClockUUIDs != null) {
        /* check for new uuid values*/
        remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach {
          vectorClockUUID =>
            val uuid = UUID.fromString (vectorClockUUID.getUuid)
            if (dataManager.getUUIDVectorClock (uuid) == null) {
              logger.debug ("add empty local vectorClock with the uuid if it is not already defined")
              dataManager.setData (uuid,
                                    Tuple2[VectorClock, Any] (VectorClock.newBuilder
                                      .setTimestamp (System.currentTimeMillis).build, null))
            }
        }
        if (garbage) {
          /* check for deleted uuid values */
          val localUUIDs = dataManager.getUUIDVectorClocks ()
          localUUIDs.keySet.foreach {
            key =>
              if (!remoteVectorClockUUIDs.getVectorClockUUIDsList.contains (key)) {
                if (dataManager.getUUIDVectorClock (key).getEntiesList
                  .exists (e => e.getNodeID == message.getDestName)) {
                  //ALREADY SEEN VECTOR CLOCK - GARBAGE IT
                  logger.debug ("ALREADY SEEN VECTOR CLOCK - GARBAGE IT")
                  dataManager.removeData (key)
                }
              }
          }
        }
      }

      //FOREACH UUIDs
      remoteVectorClockUUIDs.getVectorClockUUIDsList.foreach {
        remoteVectorClockUUID =>

          val uuid = UUID.fromString (remoteVectorClockUUID.getUuid)
          val remoteVectorClock = remoteVectorClockUUID.getVector

          dataManager.getUUIDVectorClock (uuid).printDebug ()
          remoteVectorClock.printDebug ()
          val occured = VersionUtils.compare (dataManager.getUUIDVectorClock (uuid), remoteVectorClock)
          occured match {
            case Occured.AFTER => {
              logger.debug ("VectorClocks comparison into GossiperRequestSender give us: AFTER")
            }
            case Occured.BEFORE => {
              logger.debug ("VectorClocks comparison into GossiperRequestSender give us: BEFORE")
              //updateValue(message.getDestChannelName,uuid,remoteVectorClock)
              //var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
              askForData (uuid, message.getDestNodeName, address)
              /*var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
                                                   messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
                                                   channel.write(messageBuilder.build, address);*/

              //println("initSecondStep write")
            }
            case Occured.CONCURRENTLY => {
              logger.debug ("VectorClocks comparison into GossiperRequestSender give us: CONCURRENTLY")
              //updateValue(message.getDestChannelName,uuid,remoteVectorClock)
              //var channel = bootstrap.bind(new InetSocketAddress(0)).asInstanceOf[DatagramChannel]
              askForData (uuid, message.getDestNodeName, address)
              /*var messageBuilder : Message.Builder = Message.newBuilder.setDestChannelName(channelFragment.getName)
                                                     messageBuilder.setContentClass(classOf[UUIDDataRequest].getName).setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
                                                     channel.write(messageBuilder.build, address);*/
              //println("initSecondStep write")
            }
            case _ => logger.error ("unexpected match into initSecondStep")
          }
      }
    }
  }

  private def askForData (uuid: UUID, remoteNodeName: String, address: SocketAddress) = {
    val messageBuilder: Message.Builder = Message.newBuilder.setDestName (channelFragment.getName)
      .setDestNodeName (channelFragment.getNodeName)
    messageBuilder.setContentClass (classOf[UUIDDataRequest].getName)
      .setContent (UUIDDataRequest.newBuilder.setUuid (uuid.toString).build.toByteString)
    if (fullUDP.booleanValue) {
      //channel.write (messageBuilder.build, address)
      writeMessage(messageBuilder.build, address)
    } else {
      askForDataTCPActor.askForDataAction (uuid, remoteNodeName)
    }
  }

  protected def writeMessage(o : Object, address : SocketAddress) {
    channel.write (o, address)
  }

  private def endGossip (message: Message) {
    //println("endGossip")
    if (message.getContentClass.equals (classOf[VersionedModel].getName)) {
      //println("VersionModel")
      val versionedModel = VersionedModel.parseFrom (message.getContent)
      val uuid = versionedModel.getUuid
      var vectorClock = versionedModel.getVector

      val data = serializer.deserialize (versionedModel.getModel.toByteArray)

      if (data != null) {
        // TODO include selector to define properties to choose the peer
        dataManager.setData (UUID.fromString (uuid), Tuple2[VectorClock, Any] (vectorClock, data))
        channelFragment.localNotification (data)

        // UPDATE clock
        vectorClock.getEntiesList.find (p => p.getNodeID == channelFragment.getNodeName) match {
          case Some (p) => //NOOP
          case None => {
            val newenties = ClockEntry.newBuilder.setNodeID (channelFragment.getNodeName)
              .setTimestamp (System.currentTimeMillis).setVersion (1).build
            vectorClock = VectorClock.newBuilder (vectorClock).addEnties (newenties)
              .setTimestamp (System.currentTimeMillis).build
          }
        }

        val newMerged = dataManager.mergeClock (UUID.fromString (uuid), vectorClock, message.getDestNodeName)

        //CHECK FOR GARBAGE
        if (garbage) {
          if (newMerged.getEnties (0).getNodeID.equals (channelFragment.getNodeName)) {
            val allPresent = channelFragment.getAllPeers.forall (peer => {
              newMerged.getEntiesList.exists (e => e.getNodeID == peer && e.getVersion > 0)
            })
            if (allPresent) {
              //THIS NODE IS MASTER ON THE MSG
              //ALL REMOTE NODE IN MY !PRESENT! M@R has rec a copy
              //DELETING
              //
              logger.debug ("Garbage =" + uuid)
              dataManager.removeData (UUID.fromString (uuid))
            }
          }
        }
      }
    }
  }
}
