package org.kevoree.channels
import actors.DaemonActor
import org.kevoree.framework.message.Message
import java.io.ObjectOutputStream;
import javax.microedition.io.{StreamConnection, Connector}
import javax.bluetooth._
/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 27/10/11
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */

class RFClientActor() extends DaemonActor {

  val discovery = new BluetoothDiscovery
  val awaybluetooth = new BluetoothAwayNode(this)


  def sendMessage(devicename : String,msg : Message){

    discovery.getRemoteDevice(devicename) match {
      case Some(device)=> {
        this !? SEND(device,msg)

      }
      case None => println("Not found")
    }

  }

  private case class STOP()
  private case class SEND(device : RemoteDevice,message : Message)

  def act() {
    loop {

      react{

        case STOP()=>    this.exit()
        case SEND(device,message)=>  {
          try {

          val conn = Connector.open("btspp://"+device.getBluetoothAddress+":1");
          val    out = new ObjectOutputStream(conn.asInstanceOf[StreamConnection].openOutputStream());

          out.writeObject(message);
          out.flush();
          conn.close()

          } catch {
            case e : Exception =>
                    awaybluetooth.send_timeout(device.getBluetoothAddress,message)
            reply()
          }
        }
      }


    }
  }

  def stop () {
    this ! STOP()
  }

    awaybluetooth.start()
}