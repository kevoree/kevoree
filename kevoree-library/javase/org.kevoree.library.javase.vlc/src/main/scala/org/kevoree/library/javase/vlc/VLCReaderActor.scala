package org.kevoree.library.javase.vlc

import actors.DaemonActor
import channel.VLCChannel
import scala.collection.JavaConversions._
import java.util.HashMap
import uk.co.caprica.vlcj.player.direct.{RenderCallback, DirectMediaPlayer}
import com.sun.jna.Memory
import org.kevoree.framework.message.{Message, StdKevoreeMessage}
import java.net.{Socket, ServerSocket}
import java.io.ObjectInputStream
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 08/11/11
 * Time: 13:57
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class VLCReaderActor (channel: VLCChannel) extends DaemonActor {
  private val logger: Logger = LoggerFactory.getLogger(classOf[VLCReaderActor])

  case class STOP ()

  case class RECEIVE_DATA (message: Message)

  case class RECEIVE_CONFIGURATION (client: Socket)

  var vlcs = new HashMap[String, DirectMediaPlayer]()
  var alive = true

  new Thread() {
    override def run () {
      val server = new ServerSocket(channel.parsePortNumber(channel.getNodeName))
      var client: Socket = null
      var i = 0
      while (alive && i < channel.getOtherFragments.size()) {
        client = server.accept();
        receiveConfiguration(client)
        i += 1
      }
    }
  }.start()

  start()

  def stop () {
    this ! STOP()
  }

  def receiveData (message: Message) {
    this ! RECEIVE_DATA(message)
  }

  def receiveConfiguration (client: Socket) {
    this ! RECEIVE_CONFIGURATION(client)
  }

  def act () {
    loop {
      react {
        case STOP() => stopInternals()
        case RECEIVE_DATA(message) => receiveDataInternals(message)
        case RECEIVE_CONFIGURATION(client) => receiveConfigurationInternals(client)
      }
    }
  }

  private def buildPlayer (nodeName: String, stdKevMessage: StdKevoreeMessage): DirectMediaPlayer = {

    if (!stdKevMessage.getValue("bytes").isEmpty && !stdKevMessage.getValue("width").isEmpty &&
      !stdKevMessage.getValue("height").isEmpty && !stdKevMessage.getValue("chroma").isEmpty &&
      !stdKevMessage.getValue("fps").isEmpty) {
      val mediaPlayer = MediaPlayerHelper.getInstance.getFactory(channel.getName + "_" + nodeName)
        .newDirectMediaPlayer(stdKevMessage.getValue("width").get.asInstanceOf[Int],
                               stdKevMessage.getValue("height").get.asInstanceOf[Int],
                               new OwnRenderCallback(stdKevMessage.getValue("width").get.asInstanceOf[Int],
                                                      stdKevMessage.getValue("height").get.asInstanceOf[Int],
                                                      nodeName,
                                                      stdKevMessage.getValue("chroma").get.asInstanceOf[String],
                                                      stdKevMessage.getValue("fps").get
                                                        .asInstanceOf[Int]))

      val options: Array[String] = Array[String](":demux=rawvideo",
                                                  ":rawvid-fps=" + stdKevMessage.getValue("fps").get.asInstanceOf[Int],
                                                  "rawvid-chroma=" +
                                                    stdKevMessage.getValue("chroma").get.asInstanceOf[String],
                                                  "rawvid-width=" +
                                                    stdKevMessage.getValue("width").get.asInstanceOf[Int],
                                                  "rawvid-height=" +
                                                    stdKevMessage.getValue("fps").get.asInstanceOf[Int])

      var media: String = ""
      if (channel.getDictionary.get("PROTOCOL") == "HTTP") {
        media = "http://" + channel.getAddress(nodeName) + ":" + channel.parsePortNumber(nodeName)
      } else if (channel.getDictionary.get("PROTOCOL") == "RTP") {
        media = "rtp://@" + channel.getAddress(nodeName) + ":" + channel.parsePortNumber(nodeName)
      } else if (channel.getDictionary.get("PROTOCOL") == "RTSP") {
        media = "rtsp://@" + channel.getAddress(nodeName) + ":" + channel.parsePortNumber(nodeName) + "/" +
          channel.getName + "_" + nodeName
      }
      mediaPlayer
        .playMedia(media, ":demux=rawvideo", ":rawvid-fps=" + stdKevMessage.getValue("fps").get.asInstanceOf[Int],
                    "rawvid-chroma=" + stdKevMessage.getValue("chroma").get.asInstanceOf[String],
                    "rawvid-width=" + stdKevMessage.getValue("width").get.asInstanceOf[Int],
                    "rawvid-height=" + stdKevMessage.getValue("fps").get.asInstanceOf[Int])

      mediaPlayer
    } else {
      logger.warn("unable to find required information to configure channel")
      null
    }


  }

  private def stopInternals () {
    alive = false
    vlcs.keySet().foreach {
      key =>
        val v = vlcs.get(key)
        v.stop()
        v.release()
        MediaPlayerHelper.getInstance.releaseKey(key)
    }
    this.exit()
  }

  private def receiveDataInternals (message: Message) {
    channel.forward(message)
  }

  private def receiveConfigurationInternals (client: Socket) {
    val stream = new ObjectInputStream(client.getInputStream)
    // find nodeName
    val nodeName = stream.readUTF()
    // read configuration
    val stdKevMessage = stream.readObject().asInstanceOf[StdKevoreeMessage]
    // launch vlc instance
    vlcs += channel.getName + "_" + nodeName -> buildPlayer(nodeName, stdKevMessage)
    client.close()
  }

  private final class OwnRenderCallback (width: Int, height: Int, nodeName: String, chroma: String, fps: Int)
    extends RenderCallback {
    val bytes = new Array[Byte](width * height * 4)

    def display (memory: Memory) {
      memory.read(0, bytes, 0, width * height * 4)

      val message = new Message()

      val kevMessage = new StdKevoreeMessage()
      kevMessage.putValue("bytes", bytes)
      kevMessage.putValue("width", width.asInstanceOf[AnyRef])
      kevMessage.putValue("height", height.asInstanceOf[AnyRef])
      kevMessage.putValue("chroma", chroma)
      kevMessage.putValue("fps", fps.asInstanceOf[AnyRef])

      message.setContent(kevMessage)
      message.setContentClass(kevMessage.getClass.getName)
      message.setDestChannelName(channel.getName)
      message.setDestNodeName(nodeName)


      receiveData(message)
    }
  }

}