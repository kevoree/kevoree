package org.kevoree.channels

import actors.DaemonActor
import javax.bluetooth._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/09/11
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */

class BluetoothDiscovery extends DaemonActor {

  def getRemoteDevice(remoteName: String): Option[RemoteDevice] = {
    val result = this !? DISCOVERY(remoteName)
    println("res="+result)
    result.asInstanceOf[Option[RemoteDevice]]
  }

  private case class DISCOVERY(targetName: String)

  private case class ENDDISCOVERY()

  private case class REMOTE_FOUND(btDevice: RemoteDevice)

  def act() {
    loop {
      receive {
        case DISCOVERY(targetName) => {
          val firstSender = sender
          LocalDevice.getLocalDevice.getDiscoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener)
          var loop = true
          while (loop) {
            receive {
              case ENDDISCOVERY() => {
                loop=false;firstSender ! None
              }
              case REMOTE_FOUND(btDevice) => {
                if (btDevice.getFriendlyName(false) != targetName) {
                  loop =true
                } else {
                  loop = false
                   firstSender ! Some(btDevice)
                  println(btDevice.getFriendlyName(false))
                }
              }
            }
          }
          println("End loop")
        }
        case REMOTE_FOUND(btDevice) =>
        case ENDDISCOVERY() => println("ignore end") //IGNORE
      }
    }
  }

  private val self = this

  private val listener: DiscoveryListener = new DiscoveryListener() {
    def deviceDiscovered(btDevice: RemoteDevice, cod: DeviceClass) {
      self ! REMOTE_FOUND(btDevice)
    }

    def inquiryCompleted(discType: Int): Unit = {
      LocalDevice.getLocalDevice.getDiscoveryAgent.cancelInquiry(listener); self ! ENDDISCOVERY()
    }

    def serviceSearchCompleted(transID: Int, respCode: Int) {}

    def servicesDiscovered(transID: Int, servRecord: Array[ServiceRecord]) {}
  };

  start()

}