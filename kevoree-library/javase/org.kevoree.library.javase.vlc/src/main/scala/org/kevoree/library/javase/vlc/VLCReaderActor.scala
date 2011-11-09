package org.kevoree.library.javase.vlc

import actors.DaemonActor
import channel.VLCChannel
import scala.collection.JavaConversions._
import java.util.HashMap
import uk.co.caprica.vlcj.player.direct.{RenderCallback, DirectMediaPlayer}
import com.sun.jna.Memory
import org.kevoree.framework.message.{Message, StdKevoreeMessage}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 08/11/11
 * Time: 13:57
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class VLCReaderActor (channel: VLCChannel) extends DaemonActor {

  case class STOP ()

  case class RECEIVE_DATA (message: Message)

  case class RECEIVE_CONFIGURATION ()

  var vlcs = new HashMap[String, DirectMediaPlayer]()

  channel.getOtherFragments.foreach {
    fragment => {


    }
  }

  def stop () {
    this ! STOP()
  }

  def receiveData (message: Message) {
    this ! RECEIVE_DATA(message)
  }

  def act () {
    loop {
      react {
        case STOP() => stopInternals()
        case RECEIVE_DATA(message) => receiveDataInternals(message)
        case RECEIVE_CONFIGURATION() => receiveConfigurationInternals()
      }
    }
  }

  private def buildPlayer (nodeName: String): DirectMediaPlayer = {
    val mediaPlayer = MediaPlayerHelper.getInstance.getFactory(channel.getName + "_" + nodeName)
      .newDirectMediaPlayer(0, 0, new OwnRenderCallback(0, 0, nodeName, "RV32", 0)) // TODO replace 0

    var media: String = ""
    if (channel.getDictionary.get("PROTOCOL") == "HTTP") {
      media = "http://" + channel.getAddress(nodeName) + ":" + channel.parsePortNumber(nodeName)
    } else if (channel.getDictionary.get("PROTOCOL") == "RTP") {
      media = "rtp://@" + channel.getAddress(nodeName) + ":" + channel.parsePortNumber(nodeName)
    } else if (channel.getDictionary.get("PROTOCOL") == "RTSP") {
      media = "rtsp://@" + channel.getAddress(nodeName) + ":" + channel.parsePortNumber(nodeName) + "/" +
        channel.getName + "_" + nodeName
    }
    mediaPlayer.playMedia(media)

    mediaPlayer

  }

  private def stopInternals () {
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

  private def receiveConfigurationInternals(nodeName : String, message : StdKevoreeMessage) {
    vlcs += channel.getName + "_" + nodeName -> buildPlayer(nodeName)
  }

  private final class OwnRenderCallback (width: Int, height: Int, nodeName: String, chroma: String, fps: Int)
    extends RenderCallback {
    this.width = width
    this.height = height
    val bytes = new Array[Byte](width * height * 4)

    def display (memory: Memory) {
      memory.read(0, bytes, 0, width * height * 4)

      val message = new Message()

      val kevMessage = new StdKevoreeMessage()
      kevMessage.putValue("bytes", bytes)
      kevMessage.putValue("width", width + "")
      kevMessage.putValue("height", height + "")
      kevMessage.putValue("chroma", chroma)
      kevMessage.putValue("fps", fps + "")

      message.setContent(kevMessage)
      message.setContentClass(kevMessage.getClass.getName)
      message.setDestChannelName(channel.getName)
      message.setDestNodeName(nodeName)


      receiveData(message)
    }
  }

}