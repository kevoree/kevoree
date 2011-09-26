package org.kevoree.channels

import javax.bluetooth._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/09/11
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  var remotes : List[RemoteDevice] = List()


  val localDevice = LocalDevice.getLocalDevice
  println("Address: " + localDevice.getBluetoothAddress());
  println("Name: " + localDevice.getFriendlyName());




  val lock = new Object();

  val listener = new DiscoveryListener() {
    def deviceDiscovered(btDevice:RemoteDevice , cod:DeviceClass) {
      System.out.println("Device " + btDevice.getBluetoothAddress + " found");
      remotes = remotes ++ List(btDevice)
      try {
        System.out.println("     name " + btDevice.getFriendlyName(false));
      } catch
      {
        case _ @ e =>
      }
    }
    def inquiryCompleted( discType:Int) {
      System.out.println("Device Inquiry completed!");
      synchronized {
        notifyAll();
      }
    }
    def serviceSearchCompleted(transID:Int, respCode:Int) {}
    def servicesDiscovered(transID:Int, servRecord:Array[ServiceRecord] ) {}
  };

   synchronized {
            val started = LocalDevice.getLocalDevice.getDiscoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);
            if (started) {
                println("wait for device inquiry to complete...");
                wait();
                println(remotes.size +  " device(s) found");
            }
        }


}