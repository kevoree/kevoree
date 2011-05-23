package org.kevoree.experiment.library.gossiperNetty

import java.util.UUID
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder._
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit
import java.net.InetSocketAddress
import org.kevoree.library.gossiperNetty.version.Gossip.{VersionedModel, UUIDDataRequest}
import org.kevoree.library.gossiperNetty.{GossiperRequestSender, NettyGossipAbstractElement, AskForDataTCPActor}
import com.twitter.finagle.builder.ClientBuilder

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/05/11
 * Time: 16:33
 */

class LogAskForDataTCPActor (channelFragment: NettyGossipAbstractElement, requestSender: GossiperRequestSender)
  extends AskForDataTCPActor(channelFragment, requestSender) {
  private val logger = LoggerFactory.getLogger(classOf[LogAskForDataTCPActor])

  override def askForData (uuid: UUID, remoteNodeName: String) {
    logger.debug("before to send data, we need to test if the node is available")

    val messageBuilder: Message.Builder = Message.newBuilder.setDestName(channelFragment.getName)
      .setDestNodeName(channelFragment.getNodeName)
    messageBuilder.setContentClass(classOf[UUIDDataRequest].getName)
      .setContent(UUIDDataRequest.newBuilder.setUuid(uuid.toString).build.toByteString)
    var networkIsDown = false
    var targetNodeName = ""
    FailureSimulation.failureOutNode.foreach {
      nodeName =>
        logger.debug(nodeName + " is one of the node available from here")
        if (nodeName.equals(remoteNodeName)) {
          networkIsDown = true
          targetNodeName = nodeName
          logger.debug("the message won't be sent because the node is register as a node failure")
        }
    }
    if (!networkIsDown) {
      logger.debug("TCP sending ... :-)")

      val client: Service[Message, Message] = ClientBuilder()
        .codec(ModelCodec)
        .requestTimeout(Duration.fromTimeUnit(3000, TimeUnit.MILLISECONDS))
        .hosts(new InetSocketAddress(channelFragment.getAddress(remoteNodeName),
                                      channelFragment.parsePortNumber(remoteNodeName)))
        .hostConnectionLimit(1)
        .build()

      logger.debug("client build ! ")

      client(messageBuilder.build) onSuccess {
        result =>
          println("Received result asynchronously: " + result)
          if (result.getContentClass.equals(classOf[VersionedModel].getName)) {
            requestSender.endGossipAction(result)
          }

      } onFailure {
        error =>
          logger.warn("warn TCP error ", error)
      } ensure {
        // All done! Close TCP connection(s):
        client.release()
      }
    } else {
      logger.debug("message is not sent because the link with " + targetNodeName + " is broken")
    }


  }
}