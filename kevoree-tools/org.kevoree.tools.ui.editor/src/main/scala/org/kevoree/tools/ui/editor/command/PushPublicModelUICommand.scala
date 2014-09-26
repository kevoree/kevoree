package org.kevoree.tools.ui.editor.command

/**
 * Created by duke on 6/28/14.
 */

import java.nio.charset.Charset
import java.util.Random
import javax.swing.JOptionPane

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttMessage}
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.slf4j.LoggerFactory

class PushPublicModelUICommand extends Command {

  var kernel: KevoreeUIKernel = null

  var merge = false

  def setKernel(k: KevoreeUIKernel) = kernel = k

  val jsonSaver = new org.kevoree.pmodeling.api.json.JSONModelSerializer();

  var logger = LoggerFactory.getLogger(this.getClass)

  def remoteMQTTPublic(groupName: String) = {
    try {

      val random = new Random()
      val client = new MqttClient("tcp://mqtt.kevoree.org:81", "keditor_" + random.nextInt(10000), new MemoryPersistence)
      client.connect()

      val payload = jsonSaver.serialize(kernel.getModelHandler.getActualModel)
      val mqttMessage = new MqttMessage()
      mqttMessage.setPayload(payload.getBytes(Charset.forName("UTF-8")))
      mqttMessage.setQos(0)
      mqttMessage.setRetained(true)

      client.publish("kev/" + groupName, mqttMessage)

      Thread.sleep(2000)
      client.disconnect(5000)
      client.close()


    } catch {
      case _@e => {
        e.printStackTrace()
        logger.debug("Pull failed from " + groupName)
        false
      }
    }
  }


  def execute(p: Object) = {
    try {
      val result = JOptionPane.showInputDialog("Public group name", LoadPublicModelUICommand.lastRemoteNodeAddress)
      if (result != null && result != "") {
        LoadPublicModelUICommand.lastRemoteNodeAddress = result
        remoteMQTTPublic(LoadPublicModelUICommand.lastRemoteNodeAddress)
        true
      }
    } catch {
      case _@e => {
        logger.error("Bad Input , ip@port needed")
        false
      }
    }


  }

}
