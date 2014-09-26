package org.kevoree.tools.ui.editor.command

/**
 * Created by duke on 6/28/14.
 */

import java.util.Random
import java.util.concurrent.{Exchanger, TimeUnit}
import javax.swing.JOptionPane

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttMessage}
import org.kevoree.ContainerRoot
import org.kevoree.factory.DefaultKevoreeFactory
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.slf4j.LoggerFactory

object LoadPublicModelUICommand {
  var lastRemoteNodeAddress: String = "sync"
}

class LoadPublicModelUICommand extends Command {

  var kernel: KevoreeUIKernel = null

  var merge = false

  def setKernel(k: KevoreeUIKernel) = kernel = k

  val jsonLoader = new org.kevoree.pmodeling.api.json.JSONModelLoader(new DefaultKevoreeFactory());

  private val lcommand = new LoadModelCommand();
  private val mergeCommand = new MergeModelCommand();

  var logger = LoggerFactory.getLogger(this.getClass)

  def remoteMQTTPublic(groupName: String) = {
    try {

      val random = new Random()
      val client = new MqttClient("tcp://mqtt.kevoree.org:81", "keditor_" + random.nextInt(10000), new MemoryPersistence)
      client.connect()
      val exchanger = new Exchanger[ContainerRoot]()
      client.setCallback(new MqttCallback {
        override def deliveryComplete(p1: IMqttDeliveryToken): Unit = {

        }

        override def messageArrived(p1: String, p2: MqttMessage): Unit = {
          val payload = new String(p2.getPayload);
          val nameIndice = payload.indexOf("#")
          if (nameIndice != -1) {
            val modelPayload = payload.substring(nameIndice + 1)
            val loaded = jsonLoader.loadModelFromString(modelPayload)
            if (loaded != null && loaded.size() > 0) {
              exchanger.exchange(loaded.get(0).asInstanceOf[ContainerRoot])
            }
          }
        }

        override def connectionLost(p1: Throwable): Unit = {
        }
      });
      val topicName = "kev/" + groupName;
      client.subscribe(topicName);

      val mqttPullMessage = new MqttMessage();
      mqttPullMessage.setPayload("pull".getBytes("UTF-8"));
      mqttPullMessage.setRetained(false);
      client.publish(topicName, mqttPullMessage)

      val recModel = exchanger.exchange(null, 5000, TimeUnit.MILLISECONDS)
      try {
        client.disconnect(2000)
        client.close()
      } catch {
        case e: Exception =>
      }

      if (recModel != null) {
        lcommand.setKernel(kernel)
        lcommand.execute(recModel)
      }

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
