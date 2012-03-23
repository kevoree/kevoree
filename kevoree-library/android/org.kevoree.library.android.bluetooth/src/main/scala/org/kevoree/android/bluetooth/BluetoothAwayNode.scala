package org.kevoree.android.bluetooth

import actors.DaemonActor

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 23/03/12
 * Time: 13:52
 */

class BluetoothAwayNode(val manager : BluetoothManager) extends  DaemonActor {

    def send_timeout(msg : org.kevoree.framework.message.Message){
          this ! SEND_TIMEOUT(msg)
    }
    case class STOP ()
    private case class SEND_TIMEOUT(message : org.kevoree.framework.message.Message)

    def act() {

      loop {

        react{
          case STOP() =>   stop()
          case SEND_TIMEOUT(message) =>
          {
              Thread.sleep(1)
              manager.sendMessage(message)
          }
        }
      }

      def stop () {
        this ! STOP()
      }
    }


  def stop () {
    this ! STOP()
  }



}