package org.kevoree.channels

import actors.{TIMEOUT, DaemonActor}
import javax.bluetooth.RemoteDevice
import org.kevoree.framework.message.Message
import java.io.ObjectOutputStream
import javax.microedition.io.{StreamConnection, Connector}

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 28/10/11
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */

class BluetoothAwayNode(var clientactor : RFClientActor) extends  DaemonActor {

  val discovery = new BluetoothDiscovery

  def send_timeout(devicename : String,msg : Message){

    discovery.getRemoteDevice(devicename) match {
      case Some(device)=> {
        this ! SEND_TIMEOUT(device,msg)

      }
      case None => println("Not found")
    }

  }
  case class STOP ()

  private case class SEND_TIMEOUT(device : RemoteDevice,message : Message)


  def act() {

    loop {

      react{
        case STOP() =>   stop()
        case SEND_TIMEOUT(device,message) =>
        {
          try
          {
            Thread.sleep(1)
            val conn = Connector.open("btspp://"+device.getBluetoothAddress+":1");
            val    out = new ObjectOutputStream(conn.asInstanceOf[StreamConnection].openOutputStream());
            out.writeObject(message);
            out.flush();
            conn.close()

          } catch {

            case e : Exception =>
                   clientactor.sendMessage(device.getBluetoothAddress,message)
          }

        }
      }
    }

    def stop () {
      this ! STOP()
    }







  }

}