/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.framework.port

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.kevoree.framework.{KevoreeChannelFragment, KevoreeActor, KevoreePort}
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.message.FragmentUnbindMessage

trait KevoreeRequiredThreadPort extends KevoreePort with Runnable {

  def getName: String

  def getInOut: Boolean

  private var isBound: Boolean = false

  override def getIsBound = isBound

  var reader: Thread = null

  private var isPaused = true

  var delegate: Option[KevoreeChannelFragment] = None
  val queue = new java.util.concurrent.LinkedBlockingDeque[Any]()

  def isInPause = isPaused

  def !(o: Any) {
    queue.add(o)
  }

  def !?(o: Any): Any = {
    throw new Exception("Bad Message Port Usages")
  }


  def processAdminMsg(o: Any): Boolean = {
    //TODO PAUSE THREAD
    o match {
      case bindmsg: FragmentBindMessage => {
        bind(bindmsg)
        true
      }
      case unbindmsg: FragmentUnbindMessage => {
        unbind(unbindmsg)
        true
      }
    }
  }


  def run() {
    while (true) {
      //TO CLEAN STOP
      try {
        val obj = queue.take()
        if (obj != null) {
          internal_process(obj)
        }

      } catch {
        case _@e => e.printStackTrace()
      }
    }
  }


  def startPort() {
    if (reader == null) {
      reader = new Thread(this)
      reader.start()
    }
    isPaused = false
  }

  def stop() {
    reader.interrupt()
    reader = null
  }

  def pause() {
    stop()
    isPaused = true
  }

  def forceStop() {
    stop()
  }

  def resume() {
    if (reader == null) {
      reader = new Thread(this)
      reader.start()
    }
    isPaused = false
  }


  private def bind(bindmsg: FragmentBindMessage) = {
    delegate = Some(bindmsg.getProxy)
    isBound = true
  }

  private def unbind(unbindmsg: FragmentUnbindMessage) = {
    delegate = None
    isBound = false
  }

  def internal_process(msg: Any): Any = msg match {
    case bindmsg: FragmentBindMessage => {
      bind(bindmsg)
      true
    }
    case unbindmsg: FragmentUnbindMessage => {
      unbind(unbindmsg)
      true
    }
    /* other kind of message send */
    case _@msg => {
      delegate match {
        case None =>
          /*react {
          case bindmsg: FragmentBindMessage => {
            bind(bindmsg);
            reply(true)
          }
          case STOP_ACTOR(f) => pauseState = false; stopRequest(f)
          } */
          println("Message Lost TODO retention of msg")
          false
        case Some(d) => {
          if (getInOut) {
            try {
              (d !? msg)
            } catch {
              case _@e => println("error sending message  ", e)
            }
          } else {
            try {
              d ! msg
              true
            } catch {
              case _@e =>
                println("error sending message  ", e)
                false
            }
          }
        }
      }
    }

  }
}
