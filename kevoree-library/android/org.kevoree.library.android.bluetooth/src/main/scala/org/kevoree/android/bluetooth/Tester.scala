package org.kevoree.android.bluetooth

import actors.DaemonActor
import org.kevoree.android.framework.service.KevoreeAndroidService
import android.bluetooth.BluetoothDevice
import org.kevoree.android.framework.helper.UIServiceHandler
import eu.powet.android.rfcomm.{BluetoothEvent, BluetoothEventListener, Rfcomm, IRfcomm}

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 23/03/12
 * Time: 14:03
 */


class BluetoothManager2 extends DaemonActor
{
 private val self = this
 private var uiService: KevoreeAndroidService = null
 var bluetooth : IRfcomm  = null

 private case class SCANNING(address: String)
 private case class END_SCANNING()
 private case class FOUND_DEVICE(device: BluetoothDevice)
 private case class DISCONNECTED()

 def getDevices(address: String): Option[BluetoothDevice] =
 {
    val result = this !? SCANNING(address)
    result.asInstanceOf[Option[BluetoothDevice]]
 }


 def act() {
    loop {
     receive {
       case SCANNING(address) =>
       {
         val mysender = sender
         bluetooth.discovering()
         var loop = true
         while (loop) {
           receive {
             case FOUND_DEVICE(device) => {
               if(device.getAddress != address){
                 loop =true
                 bluetooth.discovering()
               }
               else
               {
                 loop = false
                 println("FOUND "+device.getName+" "+device.getAddress)
                 mysender  ! Some(device)
               }
             }
           }
         }
       }

       case DISCONNECTED() => {

       }

     }

    }
 }


 def make()
 {
    uiService = UIServiceHandler.getUIService
    bluetooth = new Rfcomm(uiService.getRootActivity);

    bluetooth.addEventListener(new BluetoothEventListener {
     def incomingDataEvent(evt: BluetoothEvent): Unit = {


     }

     def discoveryFinished(evt: BluetoothEvent): Unit = {
       import scala.collection.JavaConversions._
       for (device <- bluetooth.getDevices)
       {
         self ! FOUND_DEVICE(device)
       }
     }

     def disconnected: Unit = {
       self ! DISCONNECTED()
     }
    })

    start()
 }

 make();


}
class Tester {

}