package org.kevoree.channels

import javax.bluetooth.LocalDevice
import com.intel.bluetooth.RemoteDeviceHelper
import javax.microedition.io.{StreamConnection, Connector}
import java.io.{ObjectOutputStream, PrintStream}
import org.kevoree.framework.message.Message

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/09/11
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */

object Tester2 extends App {

  val discovery = new BluetoothDiscovery

  discovery.getRemoteDevice("POWET") match {
    case Some(device)=> {

      val conn = Connector.open("btspp://"+device.getBluetoothAddress+":1");

      //  val sender = new PrintStream(conn.asInstanceOf[StreamConnection].openOutputStream())
      // sender.println("hello")

      val    out = new ObjectOutputStream(conn.asInstanceOf[StreamConnection].openOutputStream());

      out.writeObject(new Message());
      conn.close()

    }
    case None => println("Not found")
  }


}