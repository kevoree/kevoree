package org.kevoree.android.bluetooth

import actors.DaemonActor
import android.bluetooth.BluetoothDevice

import org.kevoree.android.framework.helper.UIServiceHandler
import eu.powet.android.rfcomm.{BluetoothEvent, BluetoothEventListener, Rfcomm, IRfcomm}
import org.kevoree.android.framework.service.KevoreeAndroidService

import org.kevoree.framework.message.Message



/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 23/03/12
 * Time: 10:52
 */

class BluetoothManager extends DaemonActor
{
  private val self = this
  private var uiService: KevoreeAndroidService = null
  var bluetooth : IRfcomm  = null
  val awaybluetooth = new BluetoothAwayNode(this)

  private case class STOP()
  private case class SEND(msg: org.kevoree.framework.message.Message)


  def act() {
    loop {
      receive {
        case SEND(msg) =>
        {
          try
          {
            if(bluetooth.isconnected()){
              bluetooth.write(msg.getContent().toString.getBytes)
            }
            else
            {
              awaybluetooth.send_timeout(msg)
            }
          } catch {
            case e : Exception =>
              awaybluetooth.send_timeout(msg)
          }
        }

      }

    }
  }


  def sendMessage(msg : Message){
        this !? SEND(msg)
  }


  def make (ctx : IRfcomm)
  {
    bluetooth = ctx
    start()
    awaybluetooth.start()
  }



  def stop () {
    this ! STOP()
  }


}